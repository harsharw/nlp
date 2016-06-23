package com.wishitventures.nlp.rules;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class NumbersUtil {

  private static final Map<String, Integer> ONES = ones();
  private static final Map<String, Integer> TENS = tens();
  private static final Map<String, Long> MULTIPLIERS = multipliers();

  public static long numberFromWord(String word) {
    String[] words = StringUtils.split(word, " ");
    if(words.length == 1) {
      long direct = direct(word);
      if(direct > 0) return direct;

      if(ONES.containsKey(word)) {
        return ONES.get(word);
      } else if(TENS.containsKey(word)) {
        return TENS.get(word);
      }
    }

    long num = 0;
    for(int i = 0; i < words.length - 1; i++) {
      String digit = words[i];
      String digitAfter = words[i + 1];
      long direct = direct(digit);

      if(MULTIPLIERS.containsKey(digitAfter)) {
        if(direct > 0) {
          num += MULTIPLIERS.get(digitAfter) * direct;
        } else if(ONES.containsKey(digit)) {
          num += MULTIPLIERS.get(digitAfter) * ONES.get(digit);
        } else if(TENS.containsKey(digit)) {
          num += MULTIPLIERS.get(digitAfter) * TENS.get(digit);
        }
      }
    }
    return num;
  }

  private static Map<String, Long> multipliers() {
    Map<String, Long> multipliers = new HashMap<>();
    multipliers.put("hundred", 100L);
    multipliers.put("thousand", 1000L);
    multipliers.put("million", 1000000L);
    multipliers.put("billion", 1000000000L);
    return multipliers;
  }

  private static Map<String, Integer> ones() {
    Map<String, Integer> ones = new HashMap<>();
    ones.put("a", 1);
    ones.put("one", 1);
    ones.put("two", 2);
    ones.put("three", 3);
    ones.put("four", 4);
    ones.put("five", 5);
    ones.put("six", 6);
    ones.put("seven", 7);
    ones.put("eight", 8);
    ones.put("nine", 9);
    return ones;
  }

  private static long direct(String val) {
    long toReturn = -1;
    try {
      toReturn = Long.parseLong(val);
    } catch (Exception e) {
      //System.err.println(val);
    }
    return toReturn;
  }

  private static Map<String, Integer> tens() {
    Map<String, Integer> ones = new HashMap<>();
    ones.put("ten", 10);
    ones.put("eleven", 11);
    ones.put("twelve", 12);
    ones.put("thirteen", 13);
    ones.put("fourteen", 14);
    ones.put("fifteen", 15);
    ones.put("sixteen", 16);
    ones.put("seventeen", 17);
    ones.put("eighteen", 18);
    ones.put("nineteen", 19);
    ones.put("twenty", 20);
    ones.put("thirty", 30);
    ones.put("forty", 40);
    ones.put("fifty", 50);
    ones.put("sixty", 60);
    ones.put("seventy", 70);
    ones.put("eighty", 80);
    ones.put("ninety", 90);
    return ones;
  }
}
