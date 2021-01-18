package com.therman.ancestorquotes;

public class Quote {
    private boolean alt = false;
    private final String source;
    private final String altSource;
    private final String text;

    public Quote(String source, String altSource, String text) {
        this.source = source;
        this.altSource = altSource;
        this.text = text;
    }

    public String getSource() {
        return source;
    }

    public String getSourceOrAltSource() {
        if(altSource.isEmpty()) return source;
        else if (alt = !alt) return source;
        else return altSource;
    }

    public String getText() {
        return text;
    }
}
