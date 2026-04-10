package com.lqm.academic_service.utils;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class MailUtils {

    private final String senderEmail;
    private final Session session;

    public MailUtils(
            @Value("${spring.mail.username}") String senderEmail,
            @Value("${spring.mail.password}") String senderPassword,
            @Value("${spring.mail.host:smtp.gmail.com}") String host,
            @Value("${spring.mail.port:587}") String port
    ) {
        this.senderEmail = senderEmail;

        if (this.senderEmail == null || senderPassword == null) {
            throw new IllegalStateException("Chưa cấu hình mail trong application.properties!");
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", host);

        this.session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            Message message = new MimeMessage(this.session);
            message.setFrom(new InternetAddress(senderEmail, "Scholar"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("Email sent successfully to " + to);
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email đến " + to + ": " + e.getMessage());
        }
    }

    @Async
    public void sendEmailAsync(String to, String subject, String body) {
        sendEmail(to, subject, body);
    }
}
