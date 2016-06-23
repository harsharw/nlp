package com.wishitventures.nlp.domain;

import org.apache.commons.lang.StringUtils;

public class POSTaggedSemanticText {

  private final String taggedText;

  public POSTaggedSemanticText(String taggedText) {
    this.taggedText = taggedText;
  }

  public void parse() {
    String[] sentences = StringUtils.split(taggedText, ".");
    for(String sentence: sentences) {
      String[] words = StringUtils.split(sentence, " ");
      for(String word: words) {
        if(word.endsWith("NN")) {
          System.out.println(word);
        }
      }
    }
  }

}
