package com.example.controller;

import com.example.domain.Invoice;
import com.example.service.BillingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;

@RestController
@RequestMapping(value = "/invoice")
public class InvoiceController {
    private final BillingService billingService;

    public InvoiceController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping("/{name}")
    public ResponseEntity<?> getInvoice(
            @PathVariable("name") String name,
            @RequestParam("from") String from,
            @RequestParam("to") String to
    ) {
        try {
            var fromTime = Timestamp.valueOf(from);
            var toTime = Timestamp.valueOf(to);
            return ResponseEntity.ok(billingService.getInvoiceForRevision(fromTime, toTime, name));
        } catch (Exception ex) {
            return ResponseEntity.ok(ex.getMessage());
        }
    }
}
