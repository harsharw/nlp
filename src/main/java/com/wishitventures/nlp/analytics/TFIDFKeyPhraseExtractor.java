package com.wishitventures.nlp.analytics;

import com.wishitventures.nlp.domain.*;

import java.util.*;
import com.wishitventures.nlp.analytics.WordDistances.DistanceToWords;

public final class TFIDFKeyPhraseExtractor implements KeyPhraseExtractor {

    private final WordDistances distances;

    public TFIDFKeyPhraseExtractor(WordDistances distances) {
        this.distances = distances;
    }

    @Override
    public List<KeyPhrases> extract(HashSet<Word> targets) {
        Map<Word, DistanceToWords> wordsToKeyWordDistances = distances.wordDistances(targets);

        for (Word word : wordsToKeyWordDistances.keySet()) {
            DistanceToWords dMap = wordsToKeyWordDistances.get(word);
            List<Word> x = dMap.closestTargets();
            if (!x.isEmpty()) {
                System.out.println(word + " --> " + x);
            }
        }
        return null;
    }

}
