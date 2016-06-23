package com.wishitventures.search;

import com.wishitventures.nlp.analytics.KeyWordExtractor;
import com.wishitventures.nlp.analytics.TFIDFKeyWordExtractor;
import com.wishitventures.nlp.domain.*;
import com.wishitventures.nlp.io.ItemTextsLoader;
import com.wishitventures.nlp.analytics.KeyWordExtractor.KeyWords.KeyWord;

import java.io.*;
import java.util.*;

public class Main {

    public static final String PATH_FOR_MR = "/Users/ECA/Desktop/AWS_Search/Example_Items_For_Search/Output/EachItem/*/*";

    //"/Users/ECA/Desktop/Search_Project/Example_Items_For_Search/*/*";//"//Users/ECA/Desktop/Desktops/Desktop_2015_Feb/STAD_New/*/*";//;

    public static void main(String[] args) throws IOException {
        List<ItemText> itemTexts = loadItems();
        long start = System.currentTimeMillis();
        TFIDFKeyWordExtractor extractor = new TFIDFKeyWordExtractor(itemTexts);
        for(KeyWordExtractor.KeyWords keyWords: extractor.extract()) {
            for(KeyWord keyWord: keyWords.keyWords()) {
                System.out.println(keyWord);
            }
        }
    }

    private static void wordRelations() throws IOException {
        List<ItemText> itemTexts = loadItems();
        ItemText firstItem = itemTexts.get(0);
        List<Word> words = firstItem.words();
        WordRelationsGraph g = new WordRelationsGraph(words);
        g.load();
        //System.out.println(g.g.edgeSet());
    }

    private static List<ItemText> loadItems() throws IOException {
        ItemTextsLoader dirLoader = new ItemTextsLoader.DirLoader(new File(PATH_FOR_MR), "item[0-1]");//"item[0-9]+");
        return dirLoader.load();
    }

}
