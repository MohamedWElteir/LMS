package com.library.management.maids.service;

import com.library.management.maids.exception.GeneralException;
import com.library.management.maids.model.Book;
import com.library.management.maids.model.BorrowingRecord;
import com.library.management.maids.model.Patron;
import com.library.management.maids.repository.BookRepository;
import com.library.management.maids.repository.BorrowingRecordRepository;
import com.library.management.maids.repository.PatronRepository;
import com.library.management.maids.util.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class BorrowingService {

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private PatronRepository patronRepository;
    @Autowired
    private BorrowingRecordRepository borrowingRecordRepository;

    public synchronized BorrowingRecord borrowBook(Long bookId, Long patronId, LocalDate expectedReturnDate) throws GeneralException {
        if (expectedReturnDate != null && expectedReturnDate.isBefore(LocalDate.now()))
            throw new GeneralException(HttpStatus.BAD_REQUEST, ErrorCode.BAD_PARAMETER_RETURN_DATE);

        Optional<Book> candidateBook = bookRepository.findById(bookId);
        if (candidateBook.isEmpty())
            throw new GeneralException(HttpStatus.NOT_FOUND, ErrorCode.BOOK_DOES_NOT_EXIST);

        Book actualBook = candidateBook.get();
        if (actualBook.getQuantity() == 0)
            throw new GeneralException(HttpStatus.NOT_FOUND, ErrorCode.BOOKS_NO_LONGER_EXIST);

        List<BorrowingRecord> availableBooks = borrowingRecordRepository.findByBookId(bookId);
        boolean isAllBooksBorrowed = (!availableBooks.isEmpty() && actualBook.getQuantity() == availableBooks.size());
        if (isAllBooksBorrowed)
            throw new GeneralException(HttpStatus.NOT_FOUND, ErrorCode.ALL_BOOKS_BORROWED);

        Optional<Patron> candidatePatron = patronRepository.findById(patronId);
        if (candidatePatron.isEmpty())
            throw new GeneralException(HttpStatus.NOT_FOUND, ErrorCode.PATRON_DOES_NOT_EXIST);


        List<BorrowingRecord> borrowedBookByPatronWithinDate = borrowingRecordRepository.getBorrowingRecordsWithin(bookId, patronId, LocalDate.now(), false);
        boolean isBorrowingTheSameBook = !borrowedBookByPatronWithinDate.isEmpty();
        List<BorrowingRecord> totalBorrowedBookByPatron = borrowingRecordRepository.getBooksBasedOnPatronAndReturnType(bookId, patronId, false);
        boolean isTryingToBorrowAfterReturnDate = totalBorrowedBookByPatron.stream().map(borrowingRecord ->
                LocalDate.now().isAfter(borrowingRecord.getReturnDate())).findAny().orElse(false);

        if (isBorrowingTheSameBook || isTryingToBorrowAfterReturnDate)
            throw new GeneralException(HttpStatus.FORBIDDEN, ErrorCode.PATRON_ALREADY_OWNS_BOOK);


        Patron actualPatron = candidatePatron.get();

        BorrowingRecord record = new BorrowingRecord();
        record.setBook(actualBook);
        record.setPatron(actualPatron);
        record.setBorrowDate(LocalDate.now());
        record.setReturnDate(expectedReturnDate);
        record.setIsReturned(false);
        try {
            return borrowingRecordRepository.save(record);
        } catch (DataIntegrityViolationException duplicateException) {
            log.warn("Cannot update a duplicate row");
            throw new GeneralException(HttpStatus.CONFLICT, ErrorCode.BORROWING_RECORDS_ALREADY_EXISTS, duplicateException);
        } catch (Exception unexpectedException) {
            log.error("Unexpected exception occurred while saving a borrowing record. Borrowing Record [{}]", record);
            throw unexpectedException;
        }
    }


    @Transactional
    public synchronized BorrowingRecord returnBook(Long bookId, Long patronId) throws GeneralException {
        List<BorrowingRecord> records = borrowingRecordRepository.getBooksBasedOnPatronAndReturnType(bookId, patronId, false);
        if (records.isEmpty())
            throw new GeneralException(HttpStatus.NOT_FOUND, ErrorCode.NO_ACTIVE_BORROWING_RECORDS);


        BorrowingRecord record = records.get(0);
        if (record.getReturnDate() != null && record.getReturnDate().isAfter(LocalDate.now()))
            log.info("You've exceeded your time for borrowing, there should be a fine.... more business logic");


        record.setReturnDate(LocalDate.now());
        record.setIsReturned(true);
        try {
            return borrowingRecordRepository.save(record);
        } catch (DataIntegrityViolationException duplicateException) {
            log.warn("Cannot update a duplicate row");
            throw new GeneralException(HttpStatus.CONFLICT, ErrorCode.BORROWING_RECORDS_ALREADY_EXISTS, duplicateException);
        } catch (Exception unexpectedException) {
            log.error("Unexpected exception occurred while returning a book. Borrowing Record [{}]", record);
            throw unexpectedException;
        }
    }
}