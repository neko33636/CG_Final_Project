package com.cgvsu.objreader;

public class ObjReaderException extends RuntimeException {

    private final int lineInd;

    public ObjReaderException(String message, int lineInd) {
        super(formatMessage(message, lineInd));
        this.lineInd = lineInd;
    }

    public int getLineInd() {
        return lineInd;
    }

    private static String formatMessage(String message, int lineInd) {
        if (lineInd < 0) {
            return "Error parsing OBJ file: " + message;
        }
        return "Error parsing OBJ file on line " + lineInd + ": " + message;
    }
}
