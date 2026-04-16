package com.dxc.gdr.service.interfaces;

public interface EmailService {
    void sendReclamationAcknowledgment(String toEmail, String clientName, String numeroReclamation, String attachmentPath, String attachmentName);
    void sendAssignmentNotification(String chefEmail, String teamName, String numeroReclamation, String titre);
    void sendStatusChangeNotification(String toEmail, String clientName, String numeroReclamation, com.dxc.gdr.model.ReclamationStatus newStatus, String titre);
}
