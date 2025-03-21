package com.example.demo.service;

import com.example.demo.model.Order;
import com.example.demo.model.OrderItem;
import com.example.demo.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;


    @Value("spring.mail.username")
    private String fromEmail;

    public void sendOrderConfirmation( Order order, int amountInPYG ) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

        try {
            Context context = new Context();
            context.setVariable("userName", order.getUser().getUsername());
            context.setVariable("orderItems", order.getItems());
            context.setVariable("totalAmount", amountInPYG);
            context.setVariable("shippingAddress", order.getAddress());
            context.setVariable("phoneNumber", order.getPhoneNumber());

            String htmlContent = templateEngine.process("order-confirmation", context);

            helper.setFrom(fromEmail);
            helper.setTo(order.getUser().getEmail());
            helper.setSubject("Confirmación de Pedido");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Correo enviado exitosamente a: " + order.getUser().getEmail());
        } catch (MessagingException e) {
            log.error("Error al enviar el correo de confirmación", e);
        }
    }

    public void sendConfirmationCode(User user) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

        try {
            Context context = new Context();
            context.setVariable("userName", user.getUsername());
            context.setVariable("confirmationCode", user.getConfirmationCode());

            String htmlContent = templateEngine.process("email-confirmation", context);

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Confirm your Email");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Correo de confirmación enviado exitosamente a: " + user.getEmail());
        } catch (MessagingException e) {
            log.error("Error al enviar el correo de confirmación", e);
        }
    }

    public void sendRecoverAccount(User user, String newPassword) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

        try {
            Context context = new Context();
            context.setVariable("userName", user.getUsername());
            context.setVariable("newPassword", newPassword);

            String htmlContent = templateEngine.process("account-recovery", context);

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Recover your Account");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Correo de recuperación de cuenta enviado exitosamente a: " + user.getEmail());
        } catch (MessagingException e) {
            log.error("Error al enviar el correo de recuperación de cuenta", e);
        }
    }
}
