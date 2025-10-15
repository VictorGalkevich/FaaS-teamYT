package com.example.controller;

import com.example.domain.Invoice;
import com.example.service.BillingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/invoice")
public class InvoiceController {
    private final BillingService billingService;

    public InvoiceController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping("/{revision}")
    public ResponseEntity<?> getInvoice(@PathVariable("revision") String revision) {
        try {
            return ResponseEntity.ok(billingService.getInvoiceForRevision(revision));
        } catch (Exception ex) {
            return ResponseEntity.ok(ex.getMessage());
        }
    }
}
