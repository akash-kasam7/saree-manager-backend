package com.shop.saree_manager.service;

import com.shop.saree_manager.model.Bill;
import com.shop.saree_manager.repository.BillRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final BillRepository billRepository;
    private final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    public ChatService(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    public String askAnything(String userQuery) {
        // 1. Fetch current bill data to give Gemini context
        List<Bill> bills = billRepository.findAll();
        String billContext = bills.stream()
                .map(b -> String.format("Bill: %s, Mfr: %s, Date: %s, Amount: %.2f, Paid: %.2f, Status: %s",
                        b.getBillNo(), b.getManufacturerName(), b.getBillDate(), b.getAmount(), b.getPaidAmount(), b.getStatus()))
                .collect(Collectors.joining(" | "));

        String fullPrompt = "You are a helpful assistant for a Saree Shop. Keep your answer very concise and to the point." +
                "Here is the shop's bill data: " + billContext + ". " +
                "Answer this question based on the data: " + userQuery;

        // 2. Call Gemini API via RestTemplate (Same way as your OcrService)
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> payload = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", fullPrompt)))
                )
        );

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(GEMINI_URL + apiKey, payload, Map.class);
            List candidates = (List) response.getBody().get("candidates");
            Map firstCandidate = (Map) candidates.get(0);
            Map content = (Map) firstCandidate.get("content");
            List parts = (List) content.get("parts");
            Map firstPart = (Map) parts.get(0);
            return firstPart.get("text").toString();
        } catch (Exception e) {
            return "Error: Could not reach Gemini. " + e.getMessage();
        }
    }
}
