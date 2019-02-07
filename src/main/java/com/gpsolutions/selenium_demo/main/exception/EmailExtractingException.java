package com.gpsolutions.selenium_demo.main.exception;

public class EmailExtractingException extends RuntimeException {
    public EmailExtractingException() {
        super();
    }

    public EmailExtractingException(final String message) {
        super(message);
    }

    public EmailExtractingException(final Exception e) {
        super(e);
    }

    public EmailExtractingException(final String message, final Exception e) {
        super(message, e);
    }
}
