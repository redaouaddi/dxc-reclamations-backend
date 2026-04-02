package com.dxc.gdr.Dto.response;

public class PythonFaceVerifyResponse {

    private boolean success;
    private boolean match;
    private double hist_score;
    private double pixel_diff;
    private double similarity_score;
    private String message;

    public PythonFaceVerifyResponse() {
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isMatch() {
        return match;
    }

    public double getHist_score() {
        return hist_score;
    }

    public double getPixel_diff() {
        return pixel_diff;
    }

    public double getSimilarity_score() {
        return similarity_score;
    }

    public String getMessage() {
        return message;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMatch(boolean match) {
        this.match = match;
    }

    public void setHist_score(double hist_score) {
        this.hist_score = hist_score;
    }

    public void setPixel_diff(double pixel_diff) {
        this.pixel_diff = pixel_diff;
    }

    public void setSimilarity_score(double similarity_score) {
        this.similarity_score = similarity_score;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}