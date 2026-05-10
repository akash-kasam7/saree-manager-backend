//package com.shop.saree_manager.service;
//
//import com.shop.saree_manager.model.Bill;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//public class GeminiBillService {
//
//    // Get your key from: https://aistudio.google.com/app/apikey
//    @Value("${gemini.api.key}")
//    private String apiKey;
//
//    private final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";
//
//    public String extractBillToJson(String billText) {
//        RestTemplate restTemplate = new RestTemplate();
//        String url = GEMINI_URL + apiKey;
//
//        // 1. Create the Prompt
//        String systemPrompt = "Extract data from this saree invoice. Return ONLY a JSON object with: " +
//                "date, customerName, items (list of name, quantity, price), and totalAmount. " +
//                "Invoice text: " + billText;
//
//        // 2. Build the JSON request body manually
//        Map<String, Object> textPart = Map.of("text", systemPrompt);
//        Map<String, Object> parts = Map.of("parts", List.of(textPart));
//        Map<String, Object> contents = Map.of("contents", List.of(parts));
//
//        // 3. Set Headers
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(contents, headers);
//
//        try {
//            // 4. Send Request
//            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
//
//            // 5. Parse the nested response (Candidates -> Content -> Parts -> Text)
//            List candidates = (List) response.getBody().get("candidates");
//            Map firstCandidate = (Map) candidates.get(0);
//            Map content = (Map) firstCandidate.get("content");
//            List resParts = (List) content.get("parts");
//            Map firstPart = (Map) resParts.get(0);
//
//            return firstPart.get("text").toString();
//        } catch (Exception e) {
//            return "{\"error\": \"Could not process bill: " + e.getMessage() + "\"}";
//        }
//    }
//
//    public String getAnswerFromData(String question, List<Bill> bills) {
//        String url = GEMINI_URL + apiKey;
//
//        // Create a text summary of the data for the AI
//        String billContext = bills.stream()
//                .map(b -> String.format("Date: %s, Manufacturer: %s, Amount: %.2f, Status: %s",
//                        b.getBillDate(), b.getManufacturerName(), b.getAmount(), b.getStatus()))
//                .collect(Collectors.joining("\n"));
//
//        String systemPrompt = "You are an expert accountant for a saree shop. " +
//                "Based on the following bill data, answer the user's question clearly.\n\n" +
//                "DATA:\n" + billContext + "\n\n" +
//                "USER QUESTION: " + question;
//
//        // Reuse your existing manual JSON request builder logic here...
//        // (Follow the structure you used in extractBillToJson)
//        return callGemini(systemPrompt);
//    }
//}