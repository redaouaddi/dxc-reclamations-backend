package com.dxc.gdr.Dto.request;

public class PythonFaceVerifyRequest {

    private String reference_image;
    private String candidate_image;

    public PythonFaceVerifyRequest() {
    }

    public PythonFaceVerifyRequest(String reference_image, String candidate_image) {
        this.reference_image = reference_image;
        this.candidate_image = candidate_image;
    }

    public String getReference_image() {
        return reference_image;
    }

    public void setReference_image(String reference_image) {
        this.reference_image = reference_image;
    }

    public String getCandidate_image() {
        return candidate_image;
    }

    public void setCandidate_image(String candidate_image) {
        this.candidate_image = candidate_image;
    }
}