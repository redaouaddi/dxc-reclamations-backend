package com.dxc.gdr.Dto.request;

public class FaceRegisterRequest {

    private String email;
    private String image;

    public FaceRegisterRequest() {
    }

    public FaceRegisterRequest(String email, String image) {
        this.email = email;
        this.image = image;
    }

    public String getEmail() {
        return email;
    }

    public String getImage() {
        return image;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setImage(String image) {
        this.image = image;
    }
}