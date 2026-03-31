package com.dxc.gdr.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendReclamationAcknowledgment(String toEmail, String clientName, String numeroReclamation) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Accusé de réception de votre réclamation " + numeroReclamation);
        message.setText(
                "Bonjour " + clientName + ",\n\n" +
                        "Nous accusons réception de votre réclamation enregistrée sous le numéro : " + numeroReclamation + ".\n\n" +
                        "Notre équipe va traiter votre demande dans les meilleurs délais.\n\n" +
                        "Cordialement,\n" +
                        "L’équipe DXC"
        );

        mailSender.send(message);

        System.out.println("EMAIL ENVOYÉ À : " + toEmail + " / Réclamation : " + numeroReclamation);
    }
}