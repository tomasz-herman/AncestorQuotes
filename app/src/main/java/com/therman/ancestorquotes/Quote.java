package com.therman.ancestorquotes;

public class Quote {
    private String source;
    private String text;

    public Quote(String source, String text) {
        this.source = source;
        this.text = text;
    }

    public String getSource() {
        return source;
    }

    public String getText() {
        return text;
    }
}
