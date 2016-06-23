package com.wishitventures.search;

import java.util.Collection;

public interface ItemMeta {

    public enum Domain {
        US, UK // Price reflects domain
    }

    String id();
    Collection<Descriptor> descriptors();
    double avgRating();
    double price();

    interface Descriptor {

        public enum Type {
            Keyword, keyPhrase, Theme, Summary, Review;

            @Override
            public String toString() {
                switch(this) {
                    case Keyword: return "k";
                    case keyPhrase: return "kp";
                    case Theme: return "t";
                    case Summary: return "s";
                    case Review: return "r";
                    default: throw new IllegalArgumentException();
                }
            }
        }
        String id();
        String content();
        double matchScore();
        String typeStr();

    }

}
