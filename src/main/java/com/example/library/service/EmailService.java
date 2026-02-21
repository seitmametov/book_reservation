package com.example.library.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final org.springframework.mail.javamail.JavaMailSender mailSender;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.username}")
    private String fromEmail;

    // В EmailService.java
    public void send(String to, String text) {
        // Он просто вызывает основной метод с дефолтной темой
        send(to, "Confirm your email", text);
    }

    // МЕТОД 2: Основной (где 3 параметра: кому, тема, текст)
    public void send(String to, String subject, String text) {
        org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
    public void sendWelcomeEmail(String to, String firstName) {
        String subject = "Добро пожаловать в Salman Library! 📚";
        String text = "Здравствуйте, " + firstName + "!\n\n" +
                "У нас отличные новости — ваш аккаунт был успешно подтвержден администратором! 🎉\n\n" +
                "Теперь вам доступны все сокровища нашей библиотеки. Вы можете брать книги, " +
                "оставлять отзывы и управлять своим профилем.\n\n" +
                "Заходите к нам скорее: http://85.239.42.236:9005\n\n" +
                "С уважением,\nКоманда Salman Library";

        send(to, subject, text);
    }

    public void sendApproveEmail(String to, String firstName) {
        String subject = "Ваш доступ к Salman Library открыт! 📚";
        String text = "Здравствуйте, " + firstName + "!\n\n" +
                "Отличные новости! Администратор проверил и одобрил ваш профиль.\n" +
                "Теперь вы полноправный читатель нашей библиотеки.\n\n" +
                "Что теперь можно делать:\n" +
                "— Бронировать любые доступные книги\n" +
                "— Управлять своим профилем и аватаром\n" +
                "— Оставлять отзывы\n\n" +
                "Скорее заходите: http://85.239.42.236:9005\n\n" +
                "Добро пожаловать!";

        send(to, text);

    }

    public void sendWithSubject(String to, String subject, String text) {
        org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}