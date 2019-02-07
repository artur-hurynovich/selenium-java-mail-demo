package com.gpsolutions.selenium_demo.main.service.impl;

import com.gpsolutions.selenium_demo.main.dto.EmailMessage;
import com.gpsolutions.selenium_demo.main.exception.EmailExtractingException;
import com.gpsolutions.selenium_demo.main.service.EmailMessageExtractor;
import com.gpsolutions.selenium_demo.main.util.EmailAddressToPlainText;
import com.gpsolutions.selenium_demo.main.util.TextFormatter;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.SearchTerm;
import java.io.IOException;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Component
@Data
public class MailRuEmailMessageExtractor implements EmailMessageExtractor {
    private final Properties properties;
    private Store store;
    @Value("${mail.ru.host}")
    private String host;
    @Value("${mail.ru.user}")
    private String user;
    @Value("${mail.ru.password}")
    private String password;
    @Value("${mail.ru.inbox.folder}")
    private String inboxFolderName;

    @Autowired
    public MailRuEmailMessageExtractor(final @Qualifier("mailRuProperties") Properties properties) {
        this.properties = properties;
    }

    @PostConstruct
    private void init() throws MessagingException {
        final Session session = Session.getInstance(properties);
        store = session.getStore();
        store.connect(host, user, password);
    }

    @Override
    public List<EmailMessage> getEmailMessagesBySenderEmailAddress(final String senderEmailAddress) {
        try (final Folder inboxFolder = store.getFolder(inboxFolderName)) {
            inboxFolder.open(Folder.READ_ONLY);
            final List<Message> messagesBySenderEmailAddress =
                    getMessagesBySenderEmailAddress(inboxFolder, senderEmailAddress);
            return messagesBySenderEmailAddress.stream().map(this::convertMessageToEmailMessage).
                    collect(Collectors.toList());
        } catch (MessagingException e) {
            throw new EmailExtractingException("Failed to extract email!" + e);
        }
    }

    private List<Message> getMessagesBySenderEmailAddress(final Folder inboxFolder, final String senderEmailAddress)
            throws MessagingException{
        return Arrays.asList(inboxFolder.search(new SearchTerm() {
            @Override
            public boolean match(final Message message) {
                try {
                    return message.getFrom()[0].toString().contains(senderEmailAddress);
                } catch (MessagingException e) {
                    throw new EmailExtractingException(e);
                }
            }
        }));
    }

    private EmailMessage convertMessageToEmailMessage(final Message message) {
        final EmailMessage emailMessage = new EmailMessage();
        try {
            emailMessage.setSenderEmailAddress(EmailAddressToPlainText.convert(message.getFrom()[0].toString()));
            emailMessage.setSubject(TextFormatter.format(message.getSubject()));
            emailMessage.setMessageText(TextFormatter.format(getMessage(message)));
            emailMessage.setRecipientEmailAddress(Arrays.stream(message.getAllRecipients()).
                    map(Address::toString).
                    map(EmailAddressToPlainText::convert).
                    collect(Collectors.toList()));
            emailMessage.setSentDate(
                    message.getSentDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            emailMessage.setReceivedDate(
                    message.getReceivedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        } catch (MessagingException | IOException e) {
            throw new EmailExtractingException("Failed to covert Message instance to EmailMessageInstance", e);
        }
        return emailMessage;
    }

    private String getMessage(final Message message) throws MessagingException, IOException {
        String messageText;
        if (message.isMimeType("multipart/*")) {
            final MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            messageText = getTextFromMimeMultipart(mimeMultipart);
        } else {
            messageText = message.getContent().toString();
        }
        return messageText;
    }

    private String getTextFromMimeMultipart(final MimeMultipart mimeMultipart) throws MessagingException, IOException {
        final StringBuilder textBuilder = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            final BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.getContent() instanceof MimeMultipart){
                textBuilder.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
            } else {
                textBuilder.append(bodyPart.getContent().toString());
                break;
            }
        }
        return textBuilder.toString();
    }
}
