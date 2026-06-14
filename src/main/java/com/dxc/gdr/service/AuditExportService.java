package com.dxc.gdr.service;

import com.dxc.gdr.Dto.response.AuditLogResponse;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AuditExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public ByteArrayInputStream exportToExcel(List<AuditLogResponse> logs) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Journal d'Audit");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Data Style
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            String[] columns = {"Date et Heure", "Utilisateur", "Rôle", "Action", "Détails"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (AuditLogResponse log : logs) {
                Row row = sheet.createRow(rowIdx++);

                Cell cellDate = row.createCell(0);
                cellDate.setCellValue(log.getTimestamp() != null ? log.getTimestamp().format(DATE_FORMATTER) : "—");
                cellDate.setCellStyle(dataStyle);

                Cell cellUser = row.createCell(1);
                String user = log.getActorName() != null ? log.getActorName() : "";
                if (log.getActorEmail() != null && !log.getActorEmail().equals(user)) {
                    user += " (" + log.getActorEmail() + ")";
                }
                cellUser.setCellValue(user.isEmpty() ? "—" : user);
                cellUser.setCellStyle(dataStyle);

                Cell cellRole = row.createCell(2);
                cellRole.setCellValue(log.getRole() != null ? log.getRole() : "—");
                cellRole.setCellStyle(dataStyle);

                Cell cellAction = row.createCell(3);
                cellAction.setCellValue(log.getAction() != null ? log.getAction() : "—");
                cellAction.setCellStyle(dataStyle);

                Cell cellDetails = row.createCell(4);
                cellDetails.setCellValue(cleanDetails(log));
                cellDetails.setCellStyle(dataStyle);
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la génération du fichier Excel", e);
        }
    }

    public ByteArrayInputStream exportToPdf(List<AuditLogResponse> logs) {
        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);

            // Page Number Event
            writer.setPageEvent(new PdfPageEventHelper() {
                @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    PdfContentByte cb = writer.getDirectContent();
                    cb.saveState();
                    String text = "Page " + writer.getPageNumber();
                    cb.beginText();
                    try {
                        cb.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED), 8);
                    } catch (Exception e) {
                        // Ignore standard fonts error
                    }
                    cb.setTextMatrix(document.right() - 40, document.bottom() - 15);
                    cb.showText(text);
                    cb.endText();
                    cb.restoreState();
                }
            });

            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, java.awt.Color.DARK_GRAY);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, java.awt.Color.GRAY);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, java.awt.Color.WHITE);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8, java.awt.Color.BLACK);

            // Header/Title
            Paragraph title = new Paragraph("JOURNAL D'AUDIT - RAPPORT DE TRACABILITE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);

            Paragraph subtitle = new Paragraph("Généré le: " + java.time.LocalDateTime.now().format(DATE_FORMATTER) + " | Total enregistrements: " + logs.size(), subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            // Table setup (5 columns)
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 2.0f, 1.2f, 2.0f, 4.5f});

            // Table Headers
            String[] headers = {"Date et Heure", "Utilisateur", "Rôle", "Action", "Détails"};
            java.awt.Color headerColor = new java.awt.Color(41, 128, 185); // Steel Blue

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(headerColor);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(6);
                table.addCell(cell);
            }

            // Table Rows
            boolean alternate = false;
            java.awt.Color altColor = new java.awt.Color(245, 247, 251);

            for (AuditLogResponse log : logs) {
                // Date
                PdfPCell cDate = new PdfPCell(new Phrase(log.getTimestamp() != null ? log.getTimestamp().format(DATE_FORMATTER) : "—", cellFont));
                styleCell(cDate, alternate, altColor);
                table.addCell(cDate);

                // User
                String user = log.getActorName() != null ? log.getActorName() : "";
                if (log.getActorEmail() != null && !log.getActorEmail().equals(user)) {
                    user += "\n(" + log.getActorEmail() + ")";
                }
                PdfPCell cUser = new PdfPCell(new Phrase(user.isEmpty() ? "—" : user, cellFont));
                styleCell(cUser, alternate, altColor);
                table.addCell(cUser);

                // Role
                PdfPCell cRole = new PdfPCell(new Phrase(log.getRole() != null ? log.getRole() : "—", cellFont));
                styleCell(cRole, alternate, altColor);
                table.addCell(cRole);

                // Action
                PdfPCell cAction = new PdfPCell(new Phrase(log.getAction() != null ? log.getAction() : "—", cellFont));
                styleCell(cAction, alternate, altColor);
                table.addCell(cAction);

                // Details
                PdfPCell cDetails = new PdfPCell(new Phrase(cleanDetails(log), cellFont));
                styleCell(cDetails, alternate, altColor);
                table.addCell(cDetails);

                alternate = !alternate;
            }

            document.add(table);
            document.close();

        } catch (DocumentException e) {
            throw new RuntimeException("Erreur lors de la génération du document PDF", e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private String cleanDetails(AuditLogResponse log) {
        String details = log.getDetails();
        if (details == null || details.isBlank()) {
            return "—";
        }

        if (!details.contains("HTTP") && !details.contains("statut")) {
            return details;
        }

        String action = log.getAction() != null ? log.getAction() : "";
        String entityId = log.getEntityId() != null ? log.getEntityId() : "";
        boolean failed = action.startsWith("ECHEC_");
        String baseAction = failed ? action.substring(6) : action;

        String description;
        switch (baseAction) {
            case "CONNEXION":
                description = "Connexion au système";
                break;
            case "CONNEXION_FACIALE":
                description = "Connexion par reconnaissance faciale";
                break;
            case "ENREGISTREMENT_VISAGE":
                description = "Enregistrement du visage (Face ID)";
                break;
            case "DEMANDE_ENREGISTREMENT_PASSKEY":
                description = "Demande d'enregistrement d'une clé de sécurité (Passkey)";
                break;
            case "ENREGISTREMENT_PASSKEY":
                description = "Clé de sécurité (Passkey) enregistrée";
                break;
            case "CREATION_RECLAMATION":
                description = "Nouvelle réclamation créée";
                break;
            case "MODIFICATION_RECLAMATION":
                description = "Réclamation" + (entityId.isEmpty() ? "" : " #" + entityId) + " modifiée";
                break;
            case "SUPPRESSION_RECLAMATION":
                description = "Réclamation" + (entityId.isEmpty() ? "" : " #" + entityId) + " supprimée";
                break;
            case "ASSIGNATION_RECLAMATION":
                description = "Réclamation" + (entityId.isEmpty() ? "" : " #" + entityId) + " assignée à une équipe";
                break;
            case "REJET_RECLAMATION":
                description = "Réclamation" + (entityId.isEmpty() ? "" : " #" + entityId) + " rejetée";
                break;
            case "ACCEPTATION_RECLAMATION":
                description = "Réclamation" + (entityId.isEmpty() ? "" : " #" + entityId) + " acceptée et mise en cours";
                break;
            case "RESOLUTION_RECLAMATION":
                description = "Réclamation" + (entityId.isEmpty() ? "" : " #" + entityId) + " marquée comme résolue";
                break;
            case "REOUVERTURE_RECLAMATION":
                description = "Réclamation" + (entityId.isEmpty() ? "" : " #" + entityId) + " réouverte";
                break;
            case "TELECHARGEMENT_PIECE_JOINTE":
                description = "Pièce jointe de la réclamation" + (entityId.isEmpty() ? "" : " #" + entityId) + " téléchargée";
                break;
            case "CREATION_UTILISATEUR":
                description = "Nouveau compte utilisateur créé" + (entityId.isEmpty() ? "" : " (" + entityId + ")");
                break;
            case "MODIFICATION_UTILISATEUR":
                description = "Informations de l'utilisateur" + (entityId.isEmpty() ? "" : " " + entityId) + " modifiées";
                break;
            case "SUPPRESSION_UTILISATEUR":
                description = "Compte utilisateur" + (entityId.isEmpty() ? "" : " " + entityId) + " supprimé";
                break;
            case "MODIFICATION_ROLES_UTILISATEUR":
                description = "Rôles et permissions de l'utilisateur" + (entityId.isEmpty() ? "" : " " + entityId) + " mis à jour";
                break;
            case "CREATION_EQUIPE":
                description = "Nouvelle équipe créée";
                break;
            case "MODIFICATION_EQUIPE":
                description = "Équipe" + (entityId.isEmpty() ? "" : " " + entityId) + " modifiée";
                break;
            case "SUPPRESSION_EQUIPE":
                description = "Équipe" + (entityId.isEmpty() ? "" : " " + entityId) + " supprimée";
                break;
            case "AJOUT_AGENT_EQUIPE":
                description = "Agent ajouté à l'équipe" + (entityId.isEmpty() ? "" : " " + entityId);
                break;
            case "RETRAIT_AGENT_EQUIPE":
                description = "Agent retiré de l'équipe" + (entityId.isEmpty() ? "" : " " + entityId);
                break;
            case "CREATION_ROLE":
                description = "Nouveau rôle créé";
                break;
            case "MODIFICATION_ROLE":
                description = "Rôle" + (entityId.isEmpty() ? "" : " " + entityId) + " modifié";
                break;
            case "SUPPRESSION_ROLE":
                description = "Rôle" + (entityId.isEmpty() ? "" : " " + entityId) + " supprimé";
                break;
            case "ENVOI_MESSAGE_INTERNE":
                description = "Message interne envoyé";
                break;
            case "ENVOI_MESSAGE_INTERNE_FICHIER":
                description = "Message interne avec pièce jointe envoyé";
                break;
            case "CREATION_SLA":
                description = "Paramètres SLA créés";
                break;
            case "MODIFICATION_SLA":
                description = "Paramètres SLA mis à jour";
                break;
            case "QUESTION_CHATBOT":
                description = "Consultation de l'assistant virtuel (chatbot)";
                break;
            case "CONSULTATION_DASHBOARD":
                description = "Consultation du tableau de bord";
                break;
            default:
                description = baseAction
                        .replace("CREATION_", "Création de ")
                        .replace("MODIFICATION_", "Modification de ")
                        .replace("SUPPRESSION_", "Suppression de ")
                        .replace("CONSULTATION_", "Consultation de ")
                        .replace("_", " ")
                        .toLowerCase();
                if (!description.isEmpty()) {
                    description = description.substring(0, 1).toUpperCase() + description.substring(1);
                } else {
                    description = details;
                }
                break;
        }

        return failed ? description + " — Échec" : description + " — Succès";
    }

    private void styleCell(PdfPCell cell, boolean alternate, java.awt.Color altColor) {
        if (alternate) {
            cell.setBackgroundColor(altColor);
        }
        cell.setPadding(5);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    }
}
