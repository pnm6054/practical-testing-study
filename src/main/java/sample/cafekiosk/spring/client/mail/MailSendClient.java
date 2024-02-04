package sample.cafekiosk.spring.client.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MailSendClient {
    public boolean sendEmail(final String fromEmail, final String toEmail, final String subject, final String content) {
        log.info("메일 전송");
        throw new IllegalStateException("메일 전송");
    }
}
