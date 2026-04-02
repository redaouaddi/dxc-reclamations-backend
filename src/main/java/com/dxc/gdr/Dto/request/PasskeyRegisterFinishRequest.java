package com.dxc.gdr.Dto.request;

public class PasskeyRegisterFinishRequest {
    private String credentialId;
    private String publicKey;
    private Long signCount;
    private String label;

    public String getCredentialId() { return credentialId; }
    public void setCredentialId(String credentialId) { this.credentialId = credentialId; }

    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }

    public Long getSignCount() { return signCount; }
    public void setSignCount(Long signCount) { this.signCount = signCount; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
}