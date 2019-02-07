package com.gpsolutions.selenium_demo.main.service;

import com.gpsolutions.selenium_demo.main.dto.EmailMessage;

import java.util.List;

public interface EmailMessageExtractor {
    List<EmailMessage> getEmailMessagesBySenderEmailAddress(final String senderEmailAddress);
}
