package com.library.management.maids.util;

public class ErrorCode {
    public static final String BOOK_DOES_NOT_EXIST = "noBookFound";
    public static final String PATRON_DOES_NOT_EXIST = "noPatronFound";
    public static final String NO_ACTIVE_BORROWING_RECORDS = "noBorrowingRecords";
    public static final String ALL_BOOKS_BORROWED = "allBooksBorrowed";
    public static final String BOOKS_NO_LONGER_EXIST = "booksNotAvailable";
    public static final String PATRON_ALREADY_OWNS_BOOK = "patronBorrowsSameBookTwice";
    public static final String BAD_PARAMETER_RETURN_DATE = "badParameter.returnDateBeforeCurrentTime";
    public static final String BORROWING_RECORDS_ALREADY_EXISTS = "borrowingRecordAlreadyExists";
    public static final String PATRON_ALREADY_EXISTS = "patronAlreadyExists";
    public static final String BOOK_ALREADY_EXISTS = "bookAlreadyExists";
    public static final String VALIDATION_ERROR = "comm.validation.error";
}
