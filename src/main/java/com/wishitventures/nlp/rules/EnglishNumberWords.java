package com.wishitventures.nlp.rules;

import java.util.Arrays;
import java.util.HashSet;

public final class EnglishNumberWords {

  private static final HashSet<String> tensNames = new HashSet<>(Arrays.asList(
      "ten",
      "twenty",
      "thirty",
      "forty",
      "fifty",
      "sixty",
      "seventy",
      "eighty",
      "ninety", "hundred", "thousand", "thousands", "million", "millions", "billion", "billions", "trillion",
      "trillions"));

  private static final HashSet<String> numNames =  new HashSet<>(Arrays.asList(new String[]{
      "one",
      "two",
      "three",
      "four",
      "five",
      "six",
      "seven",
      "eight",
      "nine",
      "ten",
      "eleven",
      "twelve",
      "thirteen",
      "fourteen",
      "fifteen",
      "sixteen",
      "seventeen",
      "eighteen",
      "nineteen"
  }));

  private static final HashSet<String> unitsOfTime = new HashSet<>(Arrays.asList(new String[]{
      "year", "years",
      "century", "centuries", "decade", "decades", "millennia"}));

  public static boolean isNumber(String word) {
    return numNames.contains(word) || tensNames.contains(word) || isNumeric(word);
  }

  public static boolean isUnitOfTime(String word) {
    return unitsOfTime.contains(word);
  }

  private static boolean isNumeric(String word) {
    boolean isNumeric = true;
    try{
      Long.parseLong(word.replace(",", ""));
    }catch (Exception e) {
      isNumeric = false;
    }
    return isNumeric;
  }
}
