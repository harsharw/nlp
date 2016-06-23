package com.wishitventures.nlp.rules;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class GenderData {

  public static final String CENSUS_DATA_DIR = "/Users/ECA/Desktop/db_dump/people_names_census/";
  private static final Map<String, GenderClassification> BY_NAME = new HashMap<>();

  private static final GenderData instance = new GenderData();

  private GenderData(){}

  private void lazyLoadNameGenderData() throws IOException {
    if(!BY_NAME.isEmpty()) {
      return;
    }

    for(File file: FileUtils.listFiles(new File(CENSUS_DATA_DIR), new String[]{"txt"}, false)) {
      for(String line: FileUtils.readLines(file)) {
        String[] fields = StringUtils.split(line, ",");
        if(fields.length < 3) {
          continue;
        }
        String name = fields[0];
        int count = Integer.parseInt(fields[2]);
        if(!BY_NAME.containsKey(name)) {
          BY_NAME.put(name, new GenderClassification(name));
        }

        GenderClassification genderClassification = BY_NAME.get(name);
        String gender = fields[1];
        if("F".equalsIgnoreCase(gender)) {
          genderClassification.incrementFemale(count);
        } else if("M".equalsIgnoreCase(gender)) {
          genderClassification.incrementMale(count);
        }
      }
    }
  }

  public static final class GenderClassification {
    int male = 0;
    int female = 0;

    final String name;

    GenderClassification(String name) {
      this.name = name;
    }

    void incrementMale(int count) {
      male += count;
    }

    void incrementFemale(int count) {
      female += count;
    }

    double genderMaleProbability(String name) {
      if(female > male) {
        return -1.0d * female / (male + female);
      } else if(male > female) {
        return 1.0d * male / (male + female);
      }
      return 0.0d;
    }

    @Override
    public String toString() {
      return "GenderClassification{" +
          "male=" + male +
          ", female=" + female +
          ", name='" + name + '\'' +
          '}';
    }
  }

  public static GenderData getInstance() throws IOException {
    instance.lazyLoadNameGenderData();
    return instance;
  }

  public boolean contains(String name) {
    return BY_NAME.containsKey(name);
  }

  public double maleProbability(String name) {
    return BY_NAME.get(name).genderMaleProbability(name);
  }
}
