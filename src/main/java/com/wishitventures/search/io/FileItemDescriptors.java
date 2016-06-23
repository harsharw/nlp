package com.wishitventures.search.io;

import com.wishitventures.search.ItemMeta.Descriptor;

import java.util.*;

// This will do for now as long as the descriptors fit in memory. When they don't, move on to a key value store
// or partition across servers (not sure if last option is possible)
public final class FileItemDescriptors implements ItemMetaRetriever {

    private final Map<String, ContentToDescriptors> typeToContentToDescriptors
            = new HashMap<String, ContentToDescriptors>();

    void add(Descriptor descriptor) {
        String typeStr = descriptor.typeStr();
        if(!typeToContentToDescriptors.containsKey(typeStr)) {
            typeToContentToDescriptors.put(typeStr, new ContentToDescriptors());
        }

        typeToContentToDescriptors.get(typeStr).add(descriptor);
    }

    void clear() {
        typeToContentToDescriptors.clear();
    }

    @Override
    public Collection<Descriptor> retrieveIDs(Descriptor.Type type, String content, String regex) {
        ContentToDescriptors ctd = typeToContentToDescriptors.get(type.toString());
        if(ctd != null) {
            return ctd.descriptors(content);
        }
        return Collections.emptyList();
    }

    private static final class ContentToDescriptors {

        private ContentToDescriptors(){}

        private final Map<String, Collection<Descriptor>> contentToDescriptors
                = new HashMap<String, Collection<Descriptor>>();

        void add(Descriptor descriptor) {
            String content = descriptor.content();
            if(!contentToDescriptors.containsKey(content)) {
                contentToDescriptors.put(content, new ArrayList<Descriptor>());
            }
            contentToDescriptors.get(content).add(descriptor);
        }

        Collection<Descriptor> descriptors(String content) {
            return contentToDescriptors.get(content);
        }
    }
}
