package com.dxc.gdr.controller;

import com.dxc.gdr.Dto.request.MessageInterneRequest;
import com.dxc.gdr.Dto.response.MessageInterneResponse;
import com.dxc.gdr.service.MessageInterneService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages-internes")
public class MessageInterneController {

    private final MessageInterneService messageInterneService;

    public MessageInterneController(MessageInterneService messageInterneService) {
        this.messageInterneService = messageInterneService;
    }

    @PostMapping
    public MessageInterneResponse envoyerMessage(@RequestBody MessageInterneRequest request) {
        return messageInterneService.envoyerMessage(request);
    }

    @GetMapping("/reclamation/{reclamationId}")
    public List<MessageInterneResponse> getMessagesByReclamation(@PathVariable Long reclamationId) {
        return messageInterneService.getMessagesByReclamation(reclamationId);
    }
}