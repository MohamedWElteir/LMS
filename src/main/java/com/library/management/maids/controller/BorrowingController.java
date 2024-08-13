package com.library.management.maids.controller;

import com.library.management.maids.exception.GeneralException;
import com.library.management.maids.model.BorrowingRecord;
import com.library.management.maids.service.BorrowingService;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api")
public class BorrowingController {

    @Autowired
    private BorrowingService borrowingService;


    @PostMapping(value = "/borrow/{bookId}/patron/{patronId}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<BorrowingRecord> borrowBook(@PathVariable Long bookId, @PathVariable Long patronId, @Nullable @RequestParam(name = "return-date")
    LocalDate expectedReturnDate) throws GeneralException {
        return ResponseEntity.ok(borrowingService.borrowBook(bookId, patronId, expectedReturnDate));
    }

    @PutMapping(value = "/return/{bookId}/patron/{patronId}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<BorrowingRecord> returnBook(@PathVariable Long bookId, @PathVariable Long patronId) throws GeneralException {
        return ResponseEntity.ok(borrowingService.returnBook(bookId, patronId));
    }
}