package com.stirante.jarpatcher;

import java.io.Serializable;

public class Action implements Serializable {

    private final Type type;
    private final String filename;
    private final byte[] data;

    public String getFilename() {
        return filename;
    }

    public Type getType() {
        return type;
    }

    public byte[] getData() {
        return data;
    }

    public enum Type {
        ADD, ADD_DIR, REMOVE, REMOVE_DIR, CHANGE
    }

    public Action(Type type, String filename, byte[] data) {
        this.type = type;
        this.filename = filename;
        if (type == Type.REMOVE || type == Type.REMOVE_DIR || type == Type.ADD_DIR)
            this.data = null;
        else
            this.data = data;
    }

    @Override
    public int hashCode() {
        return filename.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof Action && filename.equals(((Action) obj).filename);
    }
}
