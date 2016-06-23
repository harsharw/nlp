package com.wishitventures.search.io;

import com.wishitventures.search.ItemMeta;

public final class FileItemDescriptor implements ItemMeta.Descriptor {

    private final String id;
    private final double matchScore;
    private final String content;
    private final String typeStr;

    private FileItemDescriptor(String[] fields, Schema schema) {

        if(schema.typeInd != -1) {
            this.typeStr = fields[schema.typeInd].trim().toLowerCase();
        } else {
            this.typeStr = Type.Keyword.toString();
        }
        this.id = fields[schema.idInd];
        this.matchScore = Double.parseDouble(fields[schema.scoreInd]);
        this.content = fields[schema.contentInd].trim().toLowerCase();
    }

    public static FileItemDescriptor fromFields(String[] fields, Schema schema) {
        return new FileItemDescriptor(fields, schema);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String content() {
        return content;
    }

    @Override
    public double matchScore() {
        return matchScore;
    }

    @Override
    public String typeStr() {
        return typeStr;
    }

    @Override
    public String toString() {
        return "FileItemDescriptor{" +
                "id='" + id + '\'' +
                ", matchScore=" + matchScore +
                ", content='" + content + '\'' +
                ", typeStr='" + typeStr + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return typeStr.hashCode() + content.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof FileItemDescriptor) {
            FileItemDescriptor cast = (FileItemDescriptor) o;
            return cast.content.equalsIgnoreCase(this.content)
                    && cast.typeStr.equalsIgnoreCase(this.typeStr);
        }
        return false;
    }

    public static final class Schema {

        private final int typeInd;
        private final int contentInd;
        private final int scoreInd;
        private final int idInd;

        public Schema(int typeInd, int contentInd, int scoreInd, int idInd) {
            this.typeInd = typeInd;
            this.contentInd = contentInd;
            this.scoreInd = scoreInd;
            this.idInd = idInd;
        }
    }
}
