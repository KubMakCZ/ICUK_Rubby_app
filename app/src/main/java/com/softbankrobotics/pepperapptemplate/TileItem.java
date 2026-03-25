package com.softbankrobotics.pepperapptemplate;

public class TileItem {
    private final String title;
    private final String speechText;
    private final String imageName; // nullable

    public TileItem(String title, String speechText, String imageName) {
        this.title = title;
        this.speechText = speechText;
        this.imageName = imageName;
    }

    public String getTitle() { return title; }
    public String getSpeechText() { return speechText; }
    public String getImageName() { return imageName; }
}
