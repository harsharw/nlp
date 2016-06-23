package com.wishitventures.nlp.rules;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations;
import edu.stanford.nlp.hcoref.data.Mention;
import edu.stanford.nlp.ling.CoreAnnotations;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import static edu.stanford.nlp.dcoref.CorefCoreAnnotations.*;


public class Example {
  private static final Logger LOG = LoggerFactory.getLogger(Example.class);

  public static void main(String[] args) throws IOException {
    /*  // GC Overhead error
    Annotation document = new Annotation("Barack Obama was born in Hawaii.  He is the president.  Obama was elected in 2008.");
    Properties props = new Properties();
    props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,mention,coref");
    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
    pipeline.annotate(document);
    System.out.println("---");
    System.out.println("coref chains");
    for (CorefChain cc : document.get(CorefChainAnnotation.class).values()) {
      System.out.println("\t"+cc);
    }
    for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
      System.out.println("---");
      System.out.println("mentions");
      for (Mention m : sentence.get(edu.stanford.nlp.hcoref.CorefCoreAnnotations.CorefMentionsAnnotation.class)) {
        System.out.println("\t"+m);
      }
    } */

    new Example().basic();


  }


  public void basic() throws IOException {
    LOG.debug("Starting Stanford NLP");

    // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and
    Properties props = new Properties();
    boolean useRegexner = true;
    if (useRegexner) {
      props.put("annotators", "tokenize, ssplit, pos, lemma, ner, regexner");
      props.put("regexner.mapping", "/Users/ECA/2014_Projects/wishitventures_ebooksearch/nlp/src/main/resources/locations.txt");
    } else {
      props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
    }
    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);


    List tokens = new ArrayList<>();

    PrintWriter writer = new PrintWriter(new File(Classifiers.NAMED_ENTITIES + "named_entities_by_id.txt"));

    for (String line : FileUtils.readLines(new File(Classifiers.ER_PATH))) {
      String[] fields = StringUtils.split(line, "\t");

      if(fields.length < 2) {
        continue;
      }

      String content = fields[1];
      String asin =  fields[0];

      Annotation document = new Annotation(content);
      pipeline.annotate(document);

      List sentences = document.get(SentencesAnnotation.class);
      StringBuilder sb = new StringBuilder();

      for (Object sentence : sentences) {
        String prevNeToken = "O";
        String currNeToken = "O";
        boolean newToken = true;
        CoreMap sentence1 = (CoreMap)sentence;
        for (CoreLabel token : sentence1.get(TokensAnnotation.class)) {
          currNeToken = token.get(NamedEntityTagAnnotation.class);
          String word = token.get(TextAnnotation.class);
          // Strip out "O"s completely, makes code below easier to understand
          if (currNeToken.equals("O")) {
            // LOG.debug("Skipping '{}' classified as {}", word, currNeToken);
            if (!prevNeToken.equals("O") && (sb.length() > 0)) {
              handleEntity(prevNeToken, sb, tokens, asin, writer);
              newToken = true;
            }
            continue;
          }

          if (newToken) {
            prevNeToken = currNeToken;
            newToken = false;
            sb.append(word);
            continue;
          }

          if (currNeToken.equals(prevNeToken)) {
            sb.append(" " + word);
          } else {
            // We're done with the current entity - print it out and reset
            handleEntity(prevNeToken, sb, tokens, asin, writer);
            newToken = true;
          }
          prevNeToken = currNeToken;
        }
      }
      LOG.debug("We extracted {} tokens of interest from the input text", tokens.size());
    }
  }

  private void handleEntity(String inKey, StringBuilder inSb, List inTokens,
                            String asin, PrintWriter writer) {
    //System.out.println(inSb + " is a " + inKey);
    inTokens.add(new EmbeddedToken(inKey, inSb.toString()));
    writer.print(asin + "\t" + inSb + "\t" + inKey + "\n");
    inSb.setLength(0);
  }

}
class EmbeddedToken {

  private String name;
  private String value;

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public EmbeddedToken(String name, String value) {
    super();
    this.name = name;
    this.value = value;
  }

}
