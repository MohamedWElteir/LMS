package com.library.management.maids.repository;

import com.library.management.maids.model.BorrowingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface BorrowingRecordRepository extends JpaRepository<BorrowingRecord, Long> {
    @Query(value = "SELECT id, book_id, patron_id, borrow_date, return_date, is_returned FROM borrowing_records WHERE book_id = :bookId AND patron_id = :patronId AND is_returned = :isReturned",
            nativeQuery = true)List<BorrowingRecord> getBooksBasedOnPatronAndReturnType(Long bookId, Long patronId, boolean isReturned);

    List<BorrowingRecord> findByBookId(Long bookId);

    @Query(value = "SELECT id, book_id, patron_id, borrow_date, return_date, is_returned FROM borrowing_records WHERE book_id = :bookId AND patron_id = :patronId AND is_returned = :isReturned AND borrow_date <= :currentDate AND return_date >= :currentDate",
            nativeQuery = true)
    List<BorrowingRecord> getBorrowingRecordsWithin(Long bookId, Long patronId, LocalDate currentDate, boolean isReturned);
}
