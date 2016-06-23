package com.wishitventures.nlp.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Descriptions of one item from one source such as Amazon, IMDB or B&N, reviews, summary etc.
public final class ItemText implements WordsCollectionFactory.WordsCollector {

    private final String id;

    private final List<SemanticText> texts;

    public ItemText(String id, List<SemanticText> texts) {
        this.id = id;
        this.texts = texts;
    }

    @Override
    public List<Word> words() {
        List<Word> words = new ArrayList<Word>();
        for(SemanticText txt: texts) {
            words.addAll(txt.words());
        }
        return words;
    }

    public String id() {
        return id;
    }

    public List<SemanticText> descriptions() {
        return Collections.unmodifiableList(texts);
    }
}
