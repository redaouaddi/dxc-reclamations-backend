package com.dxc.gdr.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ChatbotService {

    @Value("${ollama.api.url}")
    private String ollamaApiUrl;

    @Value("${ollama.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    public String askAI(String userMessage) {

        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "Veuillez saisir votre question.";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String prompt =
                "Tu es l'assistant virtuel intelligent de l'application GDR System de DXC Technology.\n" +
                        "Tu réponds uniquement aux questions liées à la plateforme de gestion des réclamations.\n" +
                        "Dans GDR System, le SLA correspond au délai interne prévu pour traiter une réclamation selon sa priorité.\n" +
                        "Tu aides le client à comprendre : la création d'une réclamation, le suivi, les statuts, le SLA, les pièces jointes et l'utilisation de l'espace client.\n\n" +
                        "Règles obligatoires :\n" +
                        "- Réponds toujours en français.\n" +
                        "- Réponds en maximum 2 phrases.\n"+
                        "- Utilise un ton professionnel et orienté service client.\n"+
                        "- Évite les phrases longues.\n"+
                        "- Sois clair, professionnel et rassurant.\n" +
                        "- Ne donne jamais de délai précis si le système ne l'a pas fourni.\n" +
                        "- N'invente jamais l'état d'une réclamation.\n" +
                        "- Si le client demande une réclamation précise, dis-lui de consulter la rubrique Réclamations ou de préciser la référence.\n" +
                        "- Si la question n'est pas liée à GDR System, réponds poliment que tu peux uniquement aider sur la plateforme de réclamations.\n\n" +

                        "Question client : " + userMessage;

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("prompt", prompt);
        body.put("stream", false);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    ollamaApiUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            Map responseBody = response.getBody();

            if (responseBody == null) {
                return "Réponse IA vide.";
            }

            Object aiResponse = responseBody.get("response");

            if (aiResponse == null) {
                return "Réponse IA introuvable.";
            }

            return aiResponse.toString().trim();

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur IA locale : " + e.getMessage();
        }
    }
}