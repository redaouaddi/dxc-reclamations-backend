package com.dxc.gdr.config;

import com.dxc.gdr.dao.UserRepository;
import com.dxc.gdr.model.Access;
import com.dxc.gdr.model.User;
import com.dxc.gdr.service.AuditLogService;
import com.dxc.gdr.service.implement.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class ApiActionAuditInterceptor implements HandlerInterceptor {

    private static final List<String> ROLE_PRIORITY = List.of(
            "ADMIN",
            "SERVICE_MANAGER",
            "CHEF_EQUIPE",
            "AGENT",
            "CLIENT",
            "USER"
    );

    private static final List<Pattern> NOISY_READ_PATTERNS = List.of(
            Pattern.compile("^/api/reclamations$"),
            Pattern.compile("^/api/reclamations/(mes-reclamations|mes-missions|nouvelles|count)$"),
            Pattern.compile("^/api/reclamations/equipe/[^/]+$"),
            Pattern.compile("^/api/admin/users$"),
            Pattern.compile("^/api/admin/accesses$"),
            Pattern.compile("^/api/equipes$"),
            Pattern.compile("^/api/equipes/agents-libres$"),
            Pattern.compile("^/api/messages-internes/reclamation/[^/]+$"),
            Pattern.compile("^/api/admin/sla$"),
            Pattern.compile("^/api/sla$")
    );

    private static final Pattern SENSITIVE_QUERY_PARAM = Pattern.compile(
            "(?i)(password|token|jwt|secret|image|file|contenu|motif)=([^&]*)"
    );

    private final AuditLogService auditLogService;
    private final UserRepository userRepository;

    public ApiActionAuditInterceptor(AuditLogService auditLogService, UserRepository userRepository) {
        this.auditLogService = auditLogService;
        this.userRepository = userRepository;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) {
        if (!shouldAudit(request)) {
            return;
        }

        try {
            String path = normalizePath(request);
            String method = request.getMethod();
            int status = response.getStatus();
            boolean failed = ex != null || status >= 400;
            String entityType = resolveEntityType(path);
            Actor actor = resolveActor();

            String action = truncate(resolveAction(method, path, entityType, failed), 80);
            String entityId = truncate(resolveEntityId(path), 60);

            auditLogService.record(
                    actor.email,
                    actor.name,
                    actor.role,
                    action,
                    truncate(entityType, 60),
                    entityId,
                    truncate(buildHumanDetails(action, entityId, failed), 2000),
                    truncate(resolveClientIp(request), 45)
            );
        } catch (Exception auditError) {
            System.err.println("Erreur journalisation audit API: " + auditError.getMessage());
        }
    }

    private boolean shouldAudit(HttpServletRequest request) {
        String path = normalizePath(request);
        return path.startsWith("/api/")
                && !request.getMethod().equalsIgnoreCase("OPTIONS")
                && !path.equals("/api/admin/audit-logs")
                && !path.startsWith("/api/admin/audit-logs/")
                && !(request.getMethod().equalsIgnoreCase("GET") && isNoisyRead(path));
    }

    private boolean isNoisyRead(String path) {
        return NOISY_READ_PATTERNS.stream().anyMatch(pattern -> pattern.matcher(path).matches());
    }

    private String normalizePath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();
        if (contextPath != null && !contextPath.isBlank() && uri.startsWith(contextPath)) {
            return uri.substring(contextPath.length());
        }
        return uri;
    }

    private String resolveAction(String method, String path, String entityType, boolean failed) {
        String action;

        if (path.equals("/api/auth/signin")) {
            action = "CONNEXION";
        } else if (path.equals("/api/face/login")) {
            action = "CONNEXION_FACIALE";
        } else if (path.equals("/api/face/register")) {
            action = "ENREGISTREMENT_VISAGE";
        } else if (path.equals("/api/passkey/register/options")) {
            action = "DEMANDE_ENREGISTREMENT_PASSKEY";
        } else if (path.equals("/api/passkey/register/verify")) {
            action = "ENREGISTREMENT_PASSKEY";
        } else if (path.equals("/api/chatbot/ask")) {
            action = "QUESTION_CHATBOT";
        } else if (path.startsWith("/api/dashboard/")) {
            action = "CONSULTATION_DASHBOARD";
        } else if (path.contains("/assigner-equipe")) {
            action = "ASSIGNATION_RECLAMATION";
        } else if (path.contains("/rejeter")) {
            action = "REJET_RECLAMATION";
        } else if (path.contains("/accepter")) {
            action = "ACCEPTATION_RECLAMATION";
        } else if (path.contains("/resoudre")) {
            action = "RESOLUTION_RECLAMATION";
        } else if (path.contains("/reouvrir")) {
            action = "REOUVERTURE_RECLAMATION";
        } else if (path.contains("/telecharger-piece-jointe")) {
            action = "TELECHARGEMENT_PIECE_JOINTE";
        } else if (path.contains("/agents/") && method.equalsIgnoreCase("POST")) {
            action = "AJOUT_AGENT_EQUIPE";
        } else if (path.contains("/agents/") && method.equalsIgnoreCase("DELETE")) {
            action = "RETRAIT_AGENT_EQUIPE";
        } else if (path.contains("/roles") && method.equalsIgnoreCase("PUT")) {
            action = "MODIFICATION_ROLES_UTILISATEUR";
        } else if (path.startsWith("/api/messages-internes") && method.equalsIgnoreCase("POST")) {
            action = path.contains("/avec-fichier") ? "ENVOI_MESSAGE_INTERNE_FICHIER" : "ENVOI_MESSAGE_INTERNE";
        } else {
            action = switch (method.toUpperCase()) {
                case "GET" -> "CONSULTATION_" + entityType;
                case "POST" -> "CREATION_" + entityType;
                case "PUT", "PATCH" -> "MODIFICATION_" + entityType;
                case "DELETE" -> "SUPPRESSION_" + entityType;
                default -> method.toUpperCase() + "_" + entityType;
            };
        }

        return failed ? "ECHEC_" + action : action;
    }

    private String resolveEntityType(String path) {
        if (path.startsWith("/api/admin/users")) return "UTILISATEUR";
        if (path.startsWith("/api/admin/accesses")) return "ROLE";
        if (path.startsWith("/api/reclamations")) return "RECLAMATION";
        if (path.startsWith("/api/equipes")) return "EQUIPE";
        if (path.startsWith("/api/messages-internes")) return "MESSAGE_INTERNE";
        if (path.startsWith("/api/admin/sla") || path.startsWith("/api/sla")) return "SLA";
        if (path.startsWith("/api/dashboard")) return "DASHBOARD";
        if (path.startsWith("/api/auth") || path.startsWith("/api/face")) return "AUTH";
        if (path.startsWith("/api/passkey")) return "PASSKEY";
        if (path.startsWith("/api/chatbot")) return "CHATBOT";
        return "API";
    }

    private String resolveEntityId(String path) {
        List<String> segments = Arrays.stream(path.split("/"))
                .filter(segment -> !segment.isBlank())
                .toList();

        if (segments.size() < 3) {
            return null;
        }

        if (segments.contains("agents")) {
            int agentIndex = segments.indexOf("agents");
            String teamId = valueAfter(segments, "equipes");
            String agentId = agentIndex + 1 < segments.size() ? segments.get(agentIndex + 1) : null;
            return joinIds("equipe", teamId, "agent", agentId);
        }

        for (String marker : List.of("users", "accesses", "reclamations", "equipes", "sla", "reclamation")) {
            String value = valueAfter(segments, marker);
            if (value != null && !isActionSegment(value)) {
                return value;
            }
        }

        return null;
    }

    private String valueAfter(List<String> segments, String marker) {
        int index = segments.indexOf(marker);
        return index >= 0 && index + 1 < segments.size() ? segments.get(index + 1) : null;
    }

    private boolean isActionSegment(String value) {
        return List.of(
                "json",
                "count",
                "nouvelles",
                "equipe",
                "mes-reclamations",
                "mes-missions",
                "ma-gestion",
                "agents-libres",
                "register",
                "login",
                "signin",
                "ask"
        ).contains(value);
    }

    private String joinIds(String firstLabel, String firstValue, String secondLabel, String secondValue) {
        return firstLabel + "=" + (firstValue != null ? firstValue : "-")
                + ", " + secondLabel + "=" + (secondValue != null ? secondValue : "-");
    }

    private String buildHumanDetails(String action, String entityId, boolean failed) {
        if (action == null) return failed ? "Action échouée" : "Action effectuée";

        // Strip leading ECHEC_ prefix to get the base action code
        String baseAction = action.startsWith("ECHEC_") ? action.substring(6) : action;
        String ref = (entityId != null && !entityId.isBlank()) ? " #" + entityId : "";

        String description = switch (baseAction) {
            // --- Authentication & Security ---
            case "CONNEXION"                        -> "Connexion au système";
            case "CONNEXION_FACIALE"                -> "Connexion par reconnaissance faciale";
            case "ENREGISTREMENT_VISAGE"            -> "Enregistrement du visage (Face ID)";
            case "DEMANDE_ENREGISTREMENT_PASSKEY"   -> "Demande d'enregistrement d'une clé de sécurité (Passkey)";
            case "ENREGISTREMENT_PASSKEY"           -> "Clé de sécurité (Passkey) enregistrée";

            // --- Réclamations ---
            case "CREATION_RECLAMATION"             -> "Nouvelle réclamation créée";
            case "MODIFICATION_RECLAMATION"         -> "Réclamation" + ref + " modifiée";
            case "SUPPRESSION_RECLAMATION"          -> "Réclamation" + ref + " supprimée";
            case "ASSIGNATION_RECLAMATION"          -> "Réclamation" + ref + " assignée à une équipe";
            case "REJET_RECLAMATION"                -> "Réclamation" + ref + " rejetée";
            case "ACCEPTATION_RECLAMATION"          -> "Réclamation" + ref + " acceptée et mise en cours";
            case "RESOLUTION_RECLAMATION"           -> "Réclamation" + ref + " marquée comme résolue";
            case "REOUVERTURE_RECLAMATION"          -> "Réclamation" + ref + " réouverte";
            case "TELECHARGEMENT_PIECE_JOINTE"      -> "Pièce jointe de la réclamation" + ref + " téléchargée";

            // --- Utilisateurs ---
            case "CREATION_UTILISATEUR"             -> "Nouveau compte utilisateur créé";
            case "MODIFICATION_UTILISATEUR"         -> "Informations de l'utilisateur" + ref + " modifiées";
            case "SUPPRESSION_UTILISATEUR"          -> "Compte utilisateur" + ref + " supprimé";
            case "MODIFICATION_ROLES_UTILISATEUR"   -> "Rôles et permissions de l'utilisateur" + ref + " mis à jour";

            // --- Équipes ---
            case "CREATION_EQUIPE"                  -> "Nouvelle équipe créée";
            case "MODIFICATION_EQUIPE"              -> "Équipe" + ref + " modifiée";
            case "SUPPRESSION_EQUIPE"               -> "Équipe" + ref + " supprimée";
            case "AJOUT_AGENT_EQUIPE"               -> "Agent ajouté à l'équipe" + ref;
            case "RETRAIT_AGENT_EQUIPE"             -> "Agent retiré de l'équipe" + ref;

            // --- Rôles & Accès ---
            case "CREATION_ROLE"                    -> "Nouveau rôle créé";
            case "MODIFICATION_ROLE"                -> "Rôle" + ref + " modifié";
            case "SUPPRESSION_ROLE"                 -> "Rôle" + ref + " supprimé";

            // --- Messagerie interne ---
            case "ENVOI_MESSAGE_INTERNE"            -> "Message interne envoyé";
            case "ENVOI_MESSAGE_INTERNE_FICHIER"    -> "Message interne avec pièce jointe envoyé";

            // --- SLA ---
            case "CREATION_SLA"                     -> "Paramètres SLA créés";
            case "MODIFICATION_SLA"                 -> "Paramètres SLA mis à jour";

            // --- Chatbot ---
            case "QUESTION_CHATBOT"                 -> "Consultation de l'assistant virtuel (chatbot)";

            // --- Dashboard (filtrés normalement, mais au cas où) ---
            case "CONSULTATION_DASHBOARD"           -> "Consultation du tableau de bord";

            // --- Fallback générique lisible ---
            default -> {
                // Try to produce something readable from the code (e.g. "CREATION_API" -> "Création API")
                String readable = baseAction
                        .replace("CREATION_", "Création : ")
                        .replace("MODIFICATION_", "Modification : ")
                        .replace("SUPPRESSION_", "Suppression : ")
                        .replace("CONSULTATION_", "Consultation : ")
                        .replace("_", " ");
                yield readable;
            }
        };

        return failed ? description + " — Échec" : description + " — Succès";
    }

    private String maskSensitiveQuery(String query) {
        return SENSITIVE_QUERY_PARAM.matcher(query).replaceAll("$1=***");
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }

    private Actor resolveActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = resolveActorEmail(authentication);

        return userRepository.findByEmail(email)
                .map(user -> new Actor(email, resolveUserName(user), selectDisplayRole(user.getRoles())))
                .orElseGet(() -> new Actor(email, email, resolveRoleFromAuthentication(authentication)));
    }

    private String resolveActorEmail(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anonymous@gdr.local";
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            return userDetails.getEmail();
        }

        String name = authentication.getName();
        return name != null && !name.isBlank() && !"anonymousUser".equals(name)
                ? name
                : "anonymous@gdr.local";
    }

    private String resolveUserName(User user) {
        String fullName = (user.getFirstName() + " " + user.getLastName()).trim();
        return fullName.isBlank() ? user.getEmail() : fullName;
    }

    private String resolveRoleFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            return "ANONYMOUS";
        }

        Set<String> authorities = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return selectDisplayRole(authorities);
    }

    private String selectDisplayRole(Set<?> roles) {
        if (roles == null || roles.isEmpty()) {
            return "ANONYMOUS";
        }

        Set<String> roleNames = roles.stream()
                .map(role -> role instanceof Access access ? access.getName() : String.valueOf(role))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (String priorityRole : ROLE_PRIORITY) {
            if (roleNames.contains(priorityRole)) {
                return priorityRole;
            }
        }

        return roleNames.stream().findFirst().orElse("ANONYMOUS");
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private static class Actor {
        private final String email;
        private final String name;
        private final String role;

        private Actor(String email, String name, String role) {
            this.email = email;
            this.name = name;
            this.role = role;
        }
    }
}
