package com.dxc.gdr.Dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public class UpdateUserRolesRequest {

    @NotEmpty
    private Set<String> roles; // ex: ["AGENT","SERVICE_MANAGER"]

    public Set<String> getRoles() { return roles; }

    public void setRoles(Set<String> roles) { this.roles = roles; }
}
