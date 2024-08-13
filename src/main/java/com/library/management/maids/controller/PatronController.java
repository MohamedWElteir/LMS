package com.library.management.maids.controller;

import com.library.management.maids.exception.GeneralException;
import com.library.management.maids.model.Patron;
import com.library.management.maids.service.PatronService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/patrons")
public class PatronController {

    @Autowired
    private PatronService patronService;

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public List<Patron> getAllPatrons() {
        return patronService.getAllPatrons();
    }

    @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Patron> getPatronById(@PathVariable Long id) throws GeneralException {
        return ResponseEntity.ok(patronService.getPatron(id));
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Patron> createPatron(@Valid @RequestBody Patron patron) throws GeneralException {
        return ResponseEntity.ok(patronService.savePatron(patron));
    }

    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Patron> updatePatron(@PathVariable Long id, @RequestBody Patron patronDetails) throws GeneralException {
        return ResponseEntity.ok(patronService.updatePatron(id, patronDetails));
    }

    @DeleteMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Patron> deletePatron(@PathVariable Long id) throws GeneralException {
        return ResponseEntity.ok(patronService.deletePatron(id));
    }
}