package com.dxc.gdr.service;

public interface EmailService {
    void sendReclamationAcknowledgment(String toEmail, String clientName, String numeroReclamation, byte[] attachmentData, String attachmentName);
    void sendAssignmentNotification(String chefEmail, String teamName, String numeroReclamation, String titre);
}