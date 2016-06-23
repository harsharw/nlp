package com.wishitventures.nlp.rules;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public final class Classifiers {

  public static final String OF = "of";

  public static final String ER_PATH = "/Users/ECA/Desktop/db_dump/EditorialReview_Content_04_30_16.tsv";
  public static final String ITEM_PROPS_OUT_DIR = "/Users/ECA/Desktop/db_dump/item_properties";
  public static final String ER_POS = "/Users/ECA/Desktop/db_dump/pos/ER_POS.tsv";
  public static final String NAMED_ENTITIES = "/Users/ECA/Desktop/db_dump/item_properties/named_entities/";
  public static final String GENDER_SCORES_BY_ID = NAMED_ENTITIES + "Gender_Scores_By_ID.txt";
  public static final String GENDER_INDICATOR = NAMED_ENTITIES + "Gender_Indicator_By_ID.txt";
  public static final String TIMELINE_SETTING_RAW = "/Users/ECA/Desktop/db_dump/item_properties/timeline_setting/Timeline_Setting.raw";
  public static final String TIMELINE_SETTING_DISPLAY = "/Users/ECA/Desktop/db_dump/item_properties/timeline_setting/Timeline_Setting_Display.raw";

  public static void main(String[] args) throws Exception {
    //tagPOS();
    writeClassifiers();
    //tagGenderPerNameInstance();
    //tagGenderPerNameInstance();
    //postProcess();

  }

  private static void postProcess() throws IOException {
    PrintWriter writer = new PrintWriter(new File(TIMELINE_SETTING_DISPLAY));
    for(String line: FileUtils.readLines(new File(TIMELINE_SETTING_RAW))) {
      String[] fields = StringUtils.split(line, "\t");
      String value = fields[1];

      String[] words = StringUtils.split(StringUtils.substringBefore(value, "in the future"), " ");
      String timeUnit = words[words.length - 1];

      if(words.length > 2) {
        String[] numbers = null;
        if("set".equalsIgnoreCase(words[0])) {
          numbers = Arrays.copyOfRange(words, 1, words.length - 1);
        } else {
          numbers = Arrays.copyOfRange(words, 0, words.length - 1);
        }

        String numbersStr = StringUtils.join(numbers, " ");
        long i = NumbersUtil.numberFromWord(numbersStr);
        writer.write(fields[0] + "\t" + i + "\n");
        writer.flush();
      }
    }
    writer.close();
  }

  private static void tagPOS() throws Exception {
    MaxentTagger tagger = new MaxentTagger("/Users/ECA/Desktop/Recommendation_Systems/Software/stanford-postagger-full-2015-12-09/models/english-caseless-left3words-distsim.tagger");
    PrintWriter writer = new PrintWriter(new File(ER_POS));

    for(String line: FileUtils.readLines(new File(ER_PATH))) {
      String[] fields = StringUtils.split(line, "\t");
      String tagged = tagger.tagString(fields[1]);
      writer.print(fields[0] + "\t" + tagged + "\n");
      writer.flush();
    }
    writer.close();
  }

  private static void tagGenderPerNameInstance() throws IOException {
    GenderData genderData = GenderData.getInstance();
    List<String> lines = FileUtils.readLines(new File(NAMED_ENTITIES + "Person.txt"));

    PrintWriter writer = new PrintWriter(new File(GENDER_SCORES_BY_ID));
    PrintWriter genderIndicator = new PrintWriter(new File(GENDER_INDICATOR));

    Map<String, Double> primaryGenderIndicator = new HashMap<>();

    for(String line: lines) {
      String[] fields = StringUtils.split(line, "\t");
      if(fields.length < 3) {
        continue;
      }

      String[] nameParts = StringUtils.split(fields[1], " ");
      String name = nameParts[0];
      String id = fields[0];

      if(!primaryGenderIndicator.containsKey(id)) {
        primaryGenderIndicator.put(id, 0.0d);
      }

      if(genderData.contains(name) && name.length() > 1) {
        double v = genderData.maleProbability(name);
        double previous = primaryGenderIndicator.get(id);
        primaryGenderIndicator.put(id, v + previous);

        writer.write(id + "\t" + name + "\t" + v + "\n");
        writer.flush();
      }
    }

    for(String id: primaryGenderIndicator.keySet()) {
      genderIndicator.write(id + "\t" + primaryGenderIndicator.get(id) + "\n");
      genderIndicator.flush();
    }

    writer.close();
  }

  private static void writeClassifiers() throws IOException {
    List<String> lines = FileUtils.readLines(new File(ER_PATH));
    PrintWriter entityAgeWriter = new PrintWriter(ITEM_PROPS_OUT_DIR + "/entity_age/ENTITY_AGE.raw");
    PrintWriter writerTimeLine = new PrintWriter(ITEM_PROPS_OUT_DIR + "/timeline_setting/Timeline_Setting.raw");
    PrintWriter writerNumPages = new PrintWriter(ITEM_PROPS_OUT_DIR + "/num_pages/Num_Pages.txt");

    for(String line: lines) {
      String[] fields = StringUtils.split(line, "\t");

      if(fields.length < 2) {
        continue;
      }
      String asin = fields[0];
      String content = fields[1];

      String age = age(content);
      if(age != null) {
        entityAgeWriter.print(asin + "\t" + age + "\n");
        entityAgeWriter.flush();
      }

      String inTheFuture = inTheFutureSetting(content);
      if(inTheFuture != null) {
        //writerTimeLine.print(asin + "\t" + inTheFuture + "\n");
        //writerTimeLine.flush();
      }

      long numPages = numberPages(fields[1]);

      if(numPages > 0) {
        //writerNumPages.write(fields[0] + "\t" + numPages + "\n");
        //writerNumPages.flush();
      }
    }
    //writerNumPages.close();
    entityAgeWriter.close();
    //writerTimeLine.close();
  }


  public static String inTheFutureSetting(String content) {
    String inTheFuture = "in the future";

    if(content.contains(inTheFuture) || content.contains("in the past")) {
      String before = StringUtils.substringBefore(content, inTheFuture);
      String[] wordsBefore = before.replace("--", " ").toLowerCase().split(" ");

      List<String> timeBefore = new ArrayList<>();

      for(int i = wordsBefore.length - 1; i >= 0; i--) {
        String current = wordsBefore[i].trim();

        if(i < wordsBefore.length - 1 && !EnglishNumberWords.isNumber(current)
            && !isTermination(current)) {
          break;
        }
        timeBefore.add(current);
      }
      Collections.reverse(timeBefore);

      if(wordsBefore.length > 1 &&
          EnglishNumberWords.isUnitOfTime(wordsBefore[wordsBefore.length - 1])) {
        String result = StringUtils.join(timeBefore, " ") + " " + inTheFuture;
        if(result.startsWith(OF)) {
          return StringUtils.substringAfter(result, OF);
        }

        return result;
      }
    }
    return null;
  }

  public static String age(String content) {
    String yearOld = "year-old";
    if(content.contains(yearOld)) {
      String before = StringUtils.substringBefore(content, yearOld);
      String after =  StringUtils.substringAfter(content, yearOld);

      String[] wordsBefore = before.replace("--", " ").toLowerCase().split(" ");
      String[] wordsAfter = after.replace("--", " ").toLowerCase().split(" ");

      List<String> timeBefore = new ArrayList<>();

      for(int i = wordsBefore.length - 1; i >= 0; i--) {
        String current = wordsBefore[i].trim();
        timeBefore.add(current);

        if(!EnglishNumberWords.isNumber(current)) {
          break;
        }
      }
      Collections.reverse(timeBefore);
      String joined = StringUtils.join(timeBefore, " ");
      if(wordsAfter.length > 1) {
        return joined +  yearOld + " " + wordsAfter[1].replace(",", "");
      } else {
        return joined +  yearOld;
      }
    }

    return null;
  }

  public static long numberPages(String content) {
    String pages = "pages";
    if(content.contains(pages)) {
      String before = StringUtils.substringBefore(content, pages);
      String[] wordsBefore = before.toLowerCase().split(" ");

      List<String> timeBefore = new ArrayList<>();

      for(int i = wordsBefore.length - 1; i >= 0; i--) {
        String current = wordsBefore[i].trim();

        if(!EnglishNumberWords.isNumber(current)) {
          break;
        }
        timeBefore.add(current);
      }
      Collections.reverse(timeBefore);
      if(!timeBefore.isEmpty()) {
        String cleaned = StringUtils.join(timeBefore, " ").replace(",", "");
        return NumbersUtil.numberFromWord(cleaned);
      }
    }

    return -1;
  }

  private static boolean isTermination(String word) {
    return word.equals("set") || word.equals(OF) || word.equals("a");
  }

}
