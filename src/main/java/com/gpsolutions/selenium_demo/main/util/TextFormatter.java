package com.gpsolutions.selenium_demo.main.util;

import org.jsoup.Jsoup;

public class TextFormatter {
    private TextFormatter() {
        throw new IllegalStateException("Utility class!");
    }

    public static String format(final String html) {
        return Jsoup.parse(html).text().replaceAll("(\\ud83d\\udd14|\\r|\\n|={2,})", " ").trim();
    }
}
