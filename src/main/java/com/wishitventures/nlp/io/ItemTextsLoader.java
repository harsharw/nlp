package com.wishitventures.nlp.io;

import com.wishitventures.nlp.domain.Dictionary;
import com.wishitventures.nlp.domain.ItemText;
import com.wishitventures.nlp.domain.SemanticText;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface ItemTextsLoader {
    List<ItemText> load() throws IOException;

   // Root dir contains population items. Contents of one directory is ItemText
   static final class DirLoader implements ItemTextsLoader {

        private final File rootDir;
        private final String regex;
        private List<ItemText> itemTexts = null;

        public DirLoader(File rootDir, String regex) {
            this.rootDir = rootDir;
            this.regex = regex;
        }

        @Override
        public List<ItemText> load() throws IOException {
            if(itemTexts != null) {
                return itemTexts;
            }

            itemTexts = new ArrayList<ItemText>();
            System.out.println("RootDir: " + rootDir);
            for(File dir: rootDir.listFiles()) {

                List<SemanticText> descriptions = new ArrayList<SemanticText>();
                if(!dir.isDirectory() || !dir.getName().matches(regex)) {
                    System.out.println("Skipping " + dir.getAbsolutePath());
                    continue;
                }

                for (File textFile: dir.listFiles()) {
                    String fileName = textFile.getName();
                    String contents = FileUtils.readFileToString(textFile); // TODO: Charset type
                    descriptions.add(new SemanticText(contents, Dictionary.instance(),
                            SemanticText.Type.Contents, fileName));
                }
                itemTexts.add(new ItemText(dir.getName(), descriptions));
            }
            return itemTexts;
        }
    }
}
