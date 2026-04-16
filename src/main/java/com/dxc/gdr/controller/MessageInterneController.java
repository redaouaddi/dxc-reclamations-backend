package com.dxc.gdr.controller;

import com.dxc.gdr.Dto.request.MessageInterneRequest;
import com.dxc.gdr.Dto.response.MessageInterneResponse;
import com.dxc.gdr.service.MessageInterneService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages-internes")
@PreAuthorize("hasRole('AGENT') or hasRole('CHEF_EQUIPE') or hasRole('ADMIN') or hasRole('SERVICE_MANAGER')")
public class MessageInterneController {

    private final MessageInterneService messageInterneService;

    public MessageInterneController(MessageInterneService messageInterneService) {
        this.messageInterneService = messageInterneService;
    }

    @PostMapping
    public MessageInterneResponse envoyerMessage(
            @RequestBody MessageInterneRequest request,
            Authentication authentication) {
        return messageInterneService.envoyerMessage(request, authentication.getName());
    }

    @GetMapping("/reclamation/{reclamationId}")
    public List<MessageInterneResponse> getMessagesByReclamation(@PathVariable Long reclamationId) {
        return messageInterneService.getMessagesByReclamation(reclamationId);
    }
}