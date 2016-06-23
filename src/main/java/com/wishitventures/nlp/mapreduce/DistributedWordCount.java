package com.wishitventures.nlp.mapreduce;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import com.wishitventures.nlp.analytics.DistributedKeyWordExtractor;
import com.wishitventures.nlp.domain.Word;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class DistributedWordCount {

  public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    @Override
    public void map(Object key, Text value, Context context)
        throws IOException, InterruptedException {

      String content = StringUtils.substringAfter(value.toString(), ",");

      for (String piece : content.split("\\s+")) {
        String onlyLetters = Word.BaseWord.onlyLetters(piece);
        word.set(onlyLetters);
        context.write(word, one);
      }
    }
  }

  public static class IntSumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
    private IntWritable result = new IntWritable();

    @Override
    public void reduce(Text key, Iterable<IntWritable> values, Context context)
        throws IOException, InterruptedException {

      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      result.set(sum);
      context.write(key, result);
    }
  }

  private static void runWordsCount(String inputPath, String outputPath) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "word count");
    job.setJarByClass(DistributedWordCount.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(job, new Path(inputPath));
    FileOutputFormat.setOutputPath(job, new Path(outputPath));
    job.waitForCompletion(true);
  }

  public static void main(String[] args) throws Exception {
    /*
    runWordsCount(DistributedKeyWordExtractor.WORKING_DIR + "/raw_text/Book_Contents.csv",
       DistributedKeyWordExtractor.WORKING_DIR + "/allWordCounts/");  */

    BufferedReader reader = new BufferedReader(new FileReader(new File("/Users/ECA/AWS/WorkingDir/raw_text/Book_Contents.csv")));
    String l;

    while ((l = reader.readLine()) != null) {
      String asin = StringUtils.substringBefore(l, ",");
      String content = StringUtils.substringAfter(l, ",");

    }
  }

  private static void keywordScore() throws IOException {
    List<String> lines = FileUtils.readLines(new File("/Users/ECA/AWS/WorkingDir/allWordCounts/part-r-00000"));
    Map<String, Integer> allCounts = new HashMap<>();

    for(String line: lines) {
      String[] fields = StringUtils.split(line, "\t");
      if(fields.length == 2) {
        String word = (fields[0]).toLowerCase();
        int count = Integer.parseInt(fields[1]);
        allCounts.put(word, count);
      }
    }

    BufferedReader reader = new BufferedReader(new FileReader(new File("/Users/ECA/AWS/WorkingDir/raw_text/Book_Contents.csv")));
    Map<String, Map<String, Integer>> asinToWordCount = new HashMap<>();

    String l;
    while ((l = reader.readLine()) != null) {
      String asin = StringUtils.substringBefore(l, ",");
      String content = StringUtils.substringAfter(l, ",");
      if(!asinToWordCount.containsKey(asin)) {
        asinToWordCount.put(asin, new HashMap<>());
      }
      Map<String, Integer> wordCount = asinToWordCount.get(asin);

      for(String word: content.split("\\s+")) {
        String wordLC = (word).toLowerCase();
        int previous = 0;
        if(wordCount.containsKey(wordLC)) {
          previous = wordCount.get(wordLC);
        }
        wordCount.put(wordLC, previous + 1);
      }
    }

    for(String asin: asinToWordCount.keySet()) {
      List<WordScore> scores = new ArrayList<>();
      Map<String, Integer> wordCountForASIN = asinToWordCount.get(asin);
      for(String word: wordCountForASIN.keySet()) {
        String wordLC = (word).toLowerCase();
          if(wordCountForASIN.containsKey(wordLC)
              && allCounts.containsKey(wordLC)) {
          int count = wordCountForASIN.get(wordLC);
          Integer allCount = allCounts.get(wordLC);
          if(allCount != null) {
            double score = count / allCount.doubleValue();
            if(score > 0.0) {
              scores.add(new WordScore(wordLC, score));
            }
          }
        }
      }
      Collections.sort(scores);
      System.out.println(asin + " --> " + scores);
    }
  }

  private static final class WordScore implements Comparable<WordScore> {

    private final String wordStr;
    private final double score;

    private WordScore(String wordStr, double score) {
      this.wordStr = wordStr;
      this.score = score;
    }

    @Override
    public int compareTo(WordScore o) {
      if(this.score < o.score ) {
        return 1;
      } else if(this.score == o.score ) {
        return 0;
      } else {
        return -1;
      }
    }

    @Override
    public String toString() {
      return wordStr + "[" + score + "]";
    }
  }

}
