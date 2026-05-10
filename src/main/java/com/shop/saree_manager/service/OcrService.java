package com.shop.saree_manager.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class OcrService {

    @Value("${gemini.api.key}")
    public String apiKey;

    private final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=";

    public String extractFromBill(Resource image) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = GEMINI_URL + apiKey;

            byte[] imageBytes = StreamUtils.copyToByteArray(image.getInputStream());
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            String prompt = "Extract bill details into JSON: {billNo, billDate, manufacturerName, amount, transportName, lrNumber, deliveryDate}. If missing, use null.";

            Map<String, Object> payload = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt),
                                    Map.of("inline_data", Map.of(
                                            "mime_type", "image/jpeg",
                                            "data", base64Image
                                    ))
                            ))
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            List candidates = (List) response.getBody().get("candidates");
            Map firstCandidate = (Map) candidates.get(0);
            Map content = (Map) firstCandidate.get("content");
            List parts = (List) content.get("parts");
            Map firstPart = (Map) parts.get(0);

            return firstPart.get("text").toString();

        } catch (Exception e) {
            System.out.println("OCR ERROR: " + e.getMessage());
            throw new RuntimeException("Failed to process bill image: " + e.getMessage());
        }
    }
}