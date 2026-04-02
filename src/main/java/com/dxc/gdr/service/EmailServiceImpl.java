package com.dxc.gdr.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.MimeMessageHelper;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendReclamationAcknowledgment(String toEmail, String clientName, String numeroReclamation, byte[] attachmentData, String attachmentName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Accusé de réception de votre réclamation " + numeroReclamation);
            helper.setText(
                    "Bonjour " + clientName + ",\n\n" +
                            "Nous accusons réception de votre réclamation enregistrée sous le numéro : " + numeroReclamation + ".\n\n" +
                            "Notre équipe va traiter votre demande dans les meilleurs délais.\n" +
                            (attachmentData != null ? "Veuillez trouver ci-joint les documents téléchargés avec la réclamation.\n\n" : "\n") +
                            "Cordialement,\n" +
                            "L’équipe DXC"
            );

            if (attachmentData != null && attachmentData.length > 0 && attachmentName != null) {
                helper.addAttachment(attachmentName, new ByteArrayResource(attachmentData));
            }

            mailSender.send(message);

            System.out.println("EMAIL ENVOYÉ À : " + toEmail + " / Réclamation : " + numeroReclamation);
        } catch (Exception e) {
            System.err.println("Erreur de construction de l'email : " + e.getMessage());
        }
    }

    @Override
    public void sendAssignmentNotification(String chefEmail, String teamName, String numeroReclamation, String titre) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(chefEmail);
            message.setSubject("Nouvelle réclamation assignée à votre équipe : " + teamName);
            message.setText(
                    "Bonjour,\n\n" +
                            "La réclamation n° " + numeroReclamation + " (« " + titre + " ») a été assignée à votre équipe (" + teamName + ") par le Service Manager.\n\n" +
                            "Vous pouvez désormais commencer le traitement via votre tableau de bord.\n\n" +
                            "Cordialement,\n" +
                            "Le système de gestion des réclamations DXC"
            );
            mailSender.send(message);
            System.out.println("NOTIFICATION ENVOYÉE AU CHEF DE : " + teamName + " / " + chefEmail);
        } catch (Exception e) {
            System.err.println("Erreur d'envoi de notification d'assignation : " + e.getMessage());
        }
    }
}