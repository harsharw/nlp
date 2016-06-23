package com.wishitventures.search.io;

import com.wishitventures.search.ItemMeta.Descriptor;

import java.util.Collection;

public interface ItemMetaRetriever {

    /**
     * @param content
     * @param regex
     * @return ID --> Descriptors
     */
    Collection<Descriptor> retrieveIDs(Descriptor.Type type, String content, String regex);

}
