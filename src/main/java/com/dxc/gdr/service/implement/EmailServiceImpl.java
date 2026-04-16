package com.dxc.gdr.service.implement;

import com.dxc.gdr.service.interfaces.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
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
    public void sendReclamationAcknowledgment(String toEmail, String clientName, String numeroReclamation, String attachmentPath, String attachmentName) {
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
                            (attachmentPath != null && !attachmentPath.isEmpty() ? "Veuillez trouver ci-joint les documents téléchargés avec la réclamation.\n\n" : "\n") +
                            "Cordialement,\n" +
                            "L’équipe DXC"
            );

            if (attachmentPath != null && !attachmentPath.isEmpty() && attachmentName != null) {
                java.io.File file = new java.io.File(attachmentPath);
                if (file.exists()) {
                    helper.addAttachment(attachmentName, file);
                }
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

    @Override
    public void sendStatusChangeNotification(String toEmail, String clientName, String numeroReclamation, com.dxc.gdr.model.ReclamationStatus newStatus, String titre) {
        try {
            String statusText = "";
            if (newStatus == com.dxc.gdr.model.ReclamationStatus.EN_COURS) {
                statusText = "En cours de traitement";
            } else if (newStatus == com.dxc.gdr.model.ReclamationStatus.TRAITEE) {
                statusText = "Traitée (Résolue)";
            } else {
                return; // Only notify for these statuses as requested
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Mise à jour du statut de votre réclamation " + numeroReclamation);
            message.setText(
                    "Bonjour " + clientName + ",\n\n" +
                            "Nous vous informons que le statut de votre réclamation n° " + numeroReclamation + " (« " + titre + " ») a été mis à jour.\n\n" +
                            "Nouveau statut : " + statusText + ".\n\n" +
                            "Vous pouvez suivre l'état de votre demande depuis votre espace client.\n\n" +
                            "Cordialement,\n" +
                            "L'équipe DXC"
            );
            mailSender.send(message);
            System.out.println("NOTIFICATION DE STATUT ENVOYÉE AU CLIENT : " + toEmail + " / Statut : " + statusText);
        } catch (Exception e) {
            System.err.println("Erreur d'envoi de notification de changement de statut : " + e.getMessage());
        }
    }
}
