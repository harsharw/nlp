package com.wishitventures.search.io;

import com.wishitventures.search.ItemMeta;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public final class FileItemDescriptorLoader {

    public static final String RESOURCES = "/Users/ECA/2014_Projects/bookvibes_new/delphiclabs_bookvibes_dev_svn/trunk/delphiclabs_search_svn/src/main/resources";
    private final File inputDir;
    private final String fieldDelimiter;
    private final FileItemDescriptors descriptors = new FileItemDescriptors();

    public FileItemDescriptorLoader(File inputDir, String fieldDelimiter) {
        this.inputDir = inputDir;
        this.fieldDelimiter = fieldDelimiter;
    }

    public void reLoad() throws IOException {
        descriptors.clear();
        loadFromFile();
    }

    private void loadFromFile() throws IOException {
        for(File file: inputDir.listFiles()) {
            for(String line: FileUtils.readLines(file)) {
                if(line.isEmpty()) {
                    // Just ignore empty lines
                    continue;
                }

                String[] fields = StringUtils.split(line, fieldDelimiter);
                if(fields.length == 3) {
                    loadOneDescriptorForItem(fields);
                }
            }
        }
    }

    public FileItemDescriptors descriptors() {
        return descriptors;
    }

    private void loadOneDescriptorForItem(String[] fields) {
        FileItemDescriptor.Schema schema = new FileItemDescriptor.Schema(-1, 1, 2, 0);
        FileItemDescriptor descriptor = FileItemDescriptor.fromFields(fields, schema);
        descriptors.add(descriptor);
        //System.out.println(descriptor);
    }

    public static void main(String[] args) throws IOException {
        File inputDir = new File("/Users/ECA/Desktop/AWS_Search/Example_Items_For_Search/reviews/item1/");
        // new File("/Users/ECA/Desktop/AWS_Search/Example_Items_For_Search");
        FileItemDescriptorLoader loader = new FileItemDescriptorLoader(inputDir, "\t");
        loader.reLoad();
        FileItemDescriptors descriptors = loader.descriptors();

        Collection<ItemMeta.Descriptor> griffindor = descriptors.retrieveIDs(ItemMeta.Descriptor.Type.Keyword, "and", null);

        for(ItemMeta.Descriptor d: griffindor) {
          System.out.println(d);
        }
    }
}
