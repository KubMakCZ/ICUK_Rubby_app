package com.softbankrobotics.pepperapptemplate;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContentParser {

    private static final String TAG = "MSI_ContentParser";
    private static final Pattern HEADING_PATTERN = Pattern.compile("^#\\s+(.+)$");
    private static final Pattern IMAGE_PATTERN = Pattern.compile("^!\\[.*?]\\((.+?)\\)$");
    private static final Pattern QICHAT_SPECIAL = Pattern.compile("[\\$~\\^\\[\\]%]");

    public static List<TileItem> parse(Context context) {
        List<TileItem> items = new ArrayList<>();
        try {
            InputStream is = context.getAssets().open("content.md");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String currentTitle = null;
            StringBuilder speechBuilder = new StringBuilder();
            String currentImage = null;
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }
                line = line.trim();

                Matcher headingMatcher = HEADING_PATTERN.matcher(line);
                if (headingMatcher.matches()) {
                    if (currentTitle != null) {
                        items.add(new TileItem(currentTitle,
                                sanitizeSpeech(speechBuilder.toString().trim()),
                                currentImage));
                    }
                    currentTitle = headingMatcher.group(1).trim();
                    speechBuilder = new StringBuilder();
                    currentImage = null;
                    continue;
                }

                if (currentTitle == null) continue;

                Matcher imageMatcher = IMAGE_PATTERN.matcher(line);
                if (imageMatcher.matches()) {
                    currentImage = imageMatcher.group(1).trim();
                    continue;
                }

                if (line.startsWith("##")) continue;

                String cleanLine = line.replaceAll("\\*+", "").replaceAll("_+", "");

                if (!cleanLine.isEmpty()) {
                    if (speechBuilder.length() > 0) {
                        speechBuilder.append(" ");
                    }
                    speechBuilder.append(cleanLine);
                }
            }
            if (currentTitle != null) {
                items.add(new TileItem(currentTitle,
                        sanitizeSpeech(speechBuilder.toString().trim()),
                        currentImage));
            }
            reader.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to read content.md: " + e.getMessage());
        }
        return items;
    }

    private static String sanitizeSpeech(String text) {
        return QICHAT_SPECIAL.matcher(text).replaceAll("");
    }
}
