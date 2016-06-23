package com.wishitventures.nlp.domain;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.Collection;

// Mostly useful for visualization for now
public final class WordRelationsGraph {

    public final SimpleGraph<String, DefaultEdge> g = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);

    private final Collection<Word> words;

    public WordRelationsGraph(Collection<Word> words) {
        this.words = words;
    }

    public void load() {
        for(Word word: words) {
            addVertices(word);
        }

        for(Word word: words) {
            addEdges(word);
        }
    }

    private void addVertices(Word word) {
        addVertex(word);
        for(Word synonym: word.synonyms()) {
            addVertex(synonym);
        }
    }

    private void addEdges(Word word) {
        for(Word synonym: word.synonyms()) {
            g.addEdge(word.characters(), synonym.characters());
        }
    }

    private void addVertex(Word word) {
        g.addVertex(word.characters());
    }
}
