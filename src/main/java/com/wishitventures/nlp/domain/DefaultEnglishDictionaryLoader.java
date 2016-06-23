package com.wishitventures.nlp.domain;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import com.wishitventures.nlp.domain.EnglishWord.EnglishWordProps.PartOfSpeech;

public final class DefaultEnglishDictionaryLoader implements Dictionary.DictionaryLoader {

    private final File configDir;

    public DefaultEnglishDictionaryLoader(File configDir) {
        this.configDir = configDir;
    }

    @Override
    public WordsCollectionFactory.WordsCollector loadWords() {
        WordsCollectionFactory.WordsList toReturn = new WordsCollectionFactory.WordsList();
        try {
            addWordType(new File(configDir, "verbslist.txt"), PartOfSpeech.Verb, toReturn);
            addWordType(new File(configDir, "nounlist.txt"), PartOfSpeech.Noun, toReturn);
            addWordType(new File(configDir, "adjectives.txt"), PartOfSpeech.Adjective, toReturn);
            addWordType(new File(configDir, "pronouns.txt"), PartOfSpeech.ProNoun, toReturn);

            addWordType(new File(configDir, "adverbs.txt"), PartOfSpeech.Adverb, toReturn);
            addWordType(new File(configDir, "prepositions.txt"), PartOfSpeech.Preposition, toReturn);
            addWordType(new File(configDir, "conjunctions.txt"), PartOfSpeech.Conjunction, toReturn);
            addWordType(new File(configDir, "determiners.txt"), PartOfSpeech.Determiner, toReturn);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return toReturn;
    }

    private void addWordType(File wordFile, EnglishWord.EnglishWordProps.PartOfSpeech partOfSpeech, WordsCollectionFactory.WordsList wordsList) throws IOException {
        String partOfSpeechCfg = FileUtils.readFileToString(wordFile);

        for(String line: SemanticText.Paragraph.splitIntoSentences(partOfSpeechCfg)) {  // Using the para split to separate lines
            for(String rawValue: SemanticText.Paragraph.Sentence.textWords(line)) {
                wordsList.add(EnglishWord.fromStr(rawValue, partOfSpeech));
            }
        }
    }


}
