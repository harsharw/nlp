package com.wishitventures.nlp.analytics;

import com.wishitventures.nlp.domain.Word;
import com.wishitventures.nlp.domain.WordsCollectionFactory;

import java.util.HashSet;
import java.util.List;

public interface KeyPhraseExtractor {

    List<KeyPhrases> extract(HashSet<Word> targets);

    interface KeyPhrases {

        List<KeyPhrase> keyPhrases();

        final class KeyPhrase {

            private final WordsCollectionFactory.WordsList phrase;

            public KeyPhrase(WordsCollectionFactory.WordsList phrase) {
                this.phrase = phrase;
            }

            public WordsCollectionFactory.WordsList phrase() {
                return phrase;
            }
        }
    }
}
