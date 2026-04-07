package com.dxc.gdr.Dto.response;

public class PythonFaceVerifyResponse {

    private boolean success;
    private boolean match;
    private double distance;
    private double threshold;
    private String model;
    private String detector_backend;
    private String message;

    public PythonFaceVerifyResponse() {
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isMatch() {
        return match;
    }

    public double getDistance() {
        return distance;
    }

    public double getThreshold() {
        return threshold;
    }

    public String getModel() {
        return model;
    }

    public String getDetector_backend() {
        return detector_backend;
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

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setDetector_backend(String detector_backend) {
        this.detector_backend = detector_backend;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}