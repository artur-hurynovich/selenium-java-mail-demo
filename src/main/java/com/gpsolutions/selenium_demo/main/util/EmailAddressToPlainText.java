package com.gpsolutions.selenium_demo.main.util;

public class EmailAddressToPlainText {
    private EmailAddressToPlainText() {
        throw new IllegalStateException("Utility class!");
    }

    public static String convert(final String emailAddress) {
        final int start = emailAddress.indexOf('<') + 1;
        final int end = emailAddress.indexOf('>');
        return emailAddress.substring(start, end);
    }
}
