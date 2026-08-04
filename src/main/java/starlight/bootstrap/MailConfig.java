package starlight.bootstrap;

import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.util.StringUtils;

import java.util.Properties;

@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender(MailProperties mailProperties) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(mailProperties.getHost());
        sender.setPort(resolveSmtpPort(mailProperties));
        sender.setUsername(mailProperties.getUsername());
        sender.setPassword(mailProperties.getPassword());
        sender.setJavaMailProperties(buildJavaMailProps(mailProperties));
        return sender;
    }

    private Properties buildJavaMailProps(MailProperties mailProperties) {
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");

        boolean smtpAuth = StringUtils.hasText(mailProperties.getUsername())
                && StringUtils.hasText(mailProperties.getPassword());
        props.put("mail.smtp.auth", String.valueOf(smtpAuth));

        int port = resolveSmtpPort(mailProperties);
        if (port == 465) {
            props.put("mail.smtp.ssl.enable", "true");
        } else if (port == 587) {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        }

        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        props.putAll(mailProperties.getProperties());
        return props;
    }

    private int resolveSmtpPort(MailProperties mailProperties) {
        return mailProperties.getPort() != null ? mailProperties.getPort() : 587;
    }
}
