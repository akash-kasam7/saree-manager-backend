package com.shop.saree_manager.controller;

import com.shop.saree_manager.model.Bill;
import com.shop.saree_manager.model.Payment;
import com.shop.saree_manager.repository.BillRepository;
import com.shop.saree_manager.service.ChatService;
import com.shop.saree_manager.service.CloudinaryService;
import com.shop.saree_manager.service.OcrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bills")
@CrossOrigin(origins = "https://saree-manager-ui.vercel.app", allowCredentials = "true")
public class BillController {

    private final OcrService ocrService;
    private final BillRepository billRepository;
    private final CloudinaryService cloudinaryService;


    public BillController(OcrService ocrService, BillRepository billRepository, CloudinaryService cloudinaryService) {
        this.ocrService = ocrService;
        this.billRepository = billRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping("/extract")
    public ResponseEntity<?> extractOnly(@RequestParam("file") MultipartFile file) {
        try {
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() { return file.getOriginalFilename(); }
            };
            String rawJson = ocrService.extractFromBill(resource);
            String cleanJson = rawJson.replace("```json", "").replace("```", "").trim();
            return ResponseEntity.ok(cleanJson);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Extraction failed: " + e.getMessage());
        }
    }

    @PostMapping("/confirm-save")
    public ResponseEntity<Bill> confirmAndSave(@RequestPart("bill") Bill bill,
                                               @RequestPart("file") MultipartFile file) {
        try {
            String imageUrl = cloudinaryService.uploadFile(file);
            bill.setDriveLink(imageUrl);
            return ResponseEntity.ok(billRepository.save(bill));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public List<Bill> getAllBills() {
        return billRepository.findAllByOrderByIdDesc();
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<Bill> addPayment(@PathVariable Long id, @RequestBody Payment payment) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        payment.setBill(bill);
        bill.getPayments().add(payment);

        double totalPaid = bill.getPayments().stream()
                .mapToDouble(Payment::getAmountPaid)
                .sum();
        bill.setPaidAmount(totalPaid);

        if (totalPaid >= bill.getAmount()) {
            bill.setStatus("PAID");
        } else if (totalPaid > 0) {
            bill.setStatus("PARTIAL");
        }

        return ResponseEntity.ok(billRepository.save(bill));
    }
    @Autowired
    private ChatService chatService;

    @GetMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestParam String query) {
        String response = chatService.askAnything(query);
        return ResponseEntity.ok(Map.of("response", response));
    }

}