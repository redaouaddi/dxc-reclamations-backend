package com.dxc.gdr.service;

import com.dxc.gdr.model.User;
import com.dxc.gdr.model.UserFaceData;
import com.dxc.gdr.Dto.request.PythonFaceVerifyRequest;
import com.dxc.gdr.Dto.response.PythonFaceVerifyResponse;
import com.dxc.gdr.dao.UserFaceDataRepository;
import com.dxc.gdr.dao.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class FaceService {

    private final UserRepository userRepository;
    private final UserFaceDataRepository userFaceDataRepository;
    private final RestTemplate restTemplate;

    public FaceService(UserRepository userRepository,
                       UserFaceDataRepository userFaceDataRepository,
                       RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.userFaceDataRepository = userFaceDataRepository;
        this.restTemplate = restTemplate;
    }

    public void saveFace(String email, String image) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        UserFaceData faceData = userFaceDataRepository.findByUser(user)
                .orElse(new UserFaceData());

        faceData.setUser(user);
        faceData.setFaceImage(image);

        userFaceDataRepository.save(faceData);
    }

    public boolean verifyFace(String email, String candidateImage) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        UserFaceData faceData = userFaceDataRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Aucun visage enregistré pour cet utilisateur"));

        PythonFaceVerifyRequest request = new PythonFaceVerifyRequest(
                faceData.getFaceImage(),
                candidateImage
        );

        ResponseEntity<PythonFaceVerifyResponse> response = restTemplate.postForEntity(
                "http://127.0.0.1:8000/verify-face",
                request,
                PythonFaceVerifyResponse.class
        );

        PythonFaceVerifyResponse body = response.getBody();

        if (body == null || !body.isSuccess()) {
            throw new RuntimeException(body != null ? body.getMessage() : "Erreur service IA visage");
        }

        System.out.println("MATCH = " + body.isMatch());
        System.out.println("DISTANCE = " + body.getDistance());
        System.out.println("THRESHOLD = " + body.getThreshold());
        System.out.println("MODEL = " + body.getModel());
        System.out.println("DETECTOR BACKEND = " + body.getDetector_backend());

        return body.isMatch();
    }
}