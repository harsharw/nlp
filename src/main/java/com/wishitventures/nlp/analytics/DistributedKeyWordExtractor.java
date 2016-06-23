package com.wishitventures.nlp.analytics;

import com.wishitventures.nlp.domain.EnglishWord;
import com.wishitventures.nlp.domain.Word;
import com.wishitventures.nlp.domain.WordScoresMap;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class DistributedKeyWordExtractor {

  public static final String WORKING_DIR = "/Users/ECA/AWS/WorkingDir";
  public static final String SEPARATE_ASINS = WORKING_DIR + "/separateASINs";
  private static final File DIR_ALL_WORD_COUNTS = new File(WORKING_DIR + "/wordcounts/part-r-00000");
  /*
  public static final String BASE_DIR = "/Users/ECA/Desktop/AWS_Search/Example_Items_For_Search";
  //new File(BASE_DIR + "/Output/All/part-r-00000");
  private static final String PATH_EACH_ITEM_WORD_COUNTS = BASE_DIR + "/Output/EachItem/*";
  private static final String OUTPUT_PATH_KEYWORDS = BASE_DIR + "/Output/KeyWords/";
  */

  public static void main(String[] args) throws Exception {
    for(File file: new File(SEPARATE_ASINS).listFiles()) {
      runWordsCount(file.getAbsolutePath() + "/*",
          WORKING_DIR + "/keywords/" + file.getName());
    }
  }

  public static class KeyWordExtractorMapper extends Mapper<Object, Text, Text, DoubleWritable> {

    private String fileName;
    private final WordScoresMap countsAllItemWords = new WordScoresMap("All");
    private final DoubleWritable scoreWritable = new DoubleWritable();

    @Override
    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
      String[] fields = value.toString().split("\\s+");
      String wordStr = fields[0];
      Word word = EnglishWord.fromStr(wordStr);
      WordScoresMap.WordScore count = new WordScoresMap.WordScore(word,
          Double.parseDouble(fields[1]));
      WordScoresMap.WordScore countAllItems = countsAllItemWords.score(word);
      double score = count.score() / countAllItems.score();
      scoreWritable.set(score);
      context.write(new Text(wordStr), scoreWritable);
    }

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
      setInputFileName(context);
      try {
        loadAllWordCountsMap();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    private void loadAllWordCountsMap() throws IOException {

      List<String> allLines = FileUtils.readLines(DIR_ALL_WORD_COUNTS);
      for (String line : allLines) {
        String[] wordCount = line.split("\\s+");
        String wordStr = wordCount[0];
        double count = Double.parseDouble(wordCount[1]);
        Word word = EnglishWord.fromStr(wordStr);
        countsAllItemWords.add(new WordScoresMap.WordScore(word, count));
      }
    }

    private void setInputFileName(Context context) {
      FileSplit fsFileSplit = (FileSplit) context.getInputSplit();
      fileName = fsFileSplit.getPath().getParent().getName();
    }
  }

  private static void runWordsCount(String inputPath, String outputPath) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "word count");
    job.setJarByClass(DistributedKeyWordExtractor.class);
    job.setMapperClass(KeyWordExtractorMapper.class);
    job.setNumReduceTasks(0);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(DoubleWritable.class);
    FileInputFormat.addInputPath(job, new Path(inputPath));
    FileOutputFormat.setOutputPath(job, new Path(outputPath));
    job.waitForCompletion(true);
  }
}
