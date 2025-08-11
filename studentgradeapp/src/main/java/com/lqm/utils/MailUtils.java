package com.lqm.utils;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class MailUtils {

    private final String senderEmail;
    private final String senderPassword;
    private final Session session;

    public MailUtils(
            @Value("${spring.mail.username}") String senderEmail,
            @Value("${spring.mail.password}") String senderPassword
    ) {
        this.senderEmail = senderEmail;
        this.senderPassword = senderPassword;

        if (this.senderEmail == null || this.senderPassword == null) {
            throw new IllegalStateException("Chưa cấu hình mail trong application.properties!");
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

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
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("Email sent successfully to " + to);
        } catch (MessagingException e) {
            System.err.println("Lỗi khi gửi email đến " + to + ": " + e.getMessage());
        }
    }

    @Async
    public void sendEmailAsync(String to, String subject, String body) {
        sendEmail(to, subject, body);
    }
}
