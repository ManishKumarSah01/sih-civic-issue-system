package com.sih.civic.service;
import com.sih.civic.model.Complaint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final Optional<JavaMailSender> mailSender;
    @Value("${spring.mail.host:}") private String host;
    @Value("${spring.mail.username:}") private String username;
    @Value("${app.mail.from:}") private String from;

    // Use Optional<JavaMailSender> so Spring doesn't fail when mail is unconfigured
    public EmailService(Optional<JavaMailSender> mailSender) {
        this.mailSender = mailSender;
    }

    public void notify(Complaint c, String subject, String message) {
        if (host == null || host.isBlank() || username == null || username.isBlank()
                || from == null || from.isBlank() || mailSender.isEmpty()) {
            log.info("Email not configured; skipped notification for complaint {}", c.getId());
            return;
        }
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(from);
            mail.setTo(c.getCreatedBy().getEmail());
            mail.setSubject(subject);
            mail.setText(message);
            mailSender.get().send(mail);
        } catch (MailException ex) {
            log.warn("Email notification failed for complaint {}: {}", c.getId(), ex.getMessage());
        }
    }
}
