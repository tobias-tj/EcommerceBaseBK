package com.example.demo.service;

import com.example.demo.model.Order;
import com.example.demo.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("spring.mail.username")
    private String fromEmail;

    public void sendOrderConfirmation( Order order ) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(order.getUser().getEmail());
        message.setSubject("Order Confirmation");
        message.setText("Your order has been confirmed. Order ID "+ order.getId());
        mailSender.send(message);
        System.out.println("Correo enviado exitosamente a: " + order.getUser().getEmail());
    }

    public void sendConfirmationCode( User user ) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject("Confirm your Email");
        message.setText("Please confirm your email by enteeting this code " + user.getConfirmationCode());
        mailSender.send(message);
    }

    public void sendRecoverAccount(User user, String newPassword){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject("Recover your Account");
        message.setText("The new password associated with your account is: " + newPassword);
        mailSender.send(message);
    }
}
