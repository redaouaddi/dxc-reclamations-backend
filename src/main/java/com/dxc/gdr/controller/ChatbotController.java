package com.dxc.gdr.controller;

import com.dxc.gdr.Dto.request.ChatbotRequest;
import com.dxc.gdr.Dto.response.ChatbotResponse;
import com.dxc.gdr.service.ChatbotService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/ask")
    public ChatbotResponse ask(@RequestBody ChatbotRequest request) {
        return new ChatbotResponse(chatbotService.askAI(request.getMessage()));
    }
}