package com.library.management.maids.service;

import com.library.management.maids.exception.GeneralException;
import com.library.management.maids.model.Book;
import com.library.management.maids.repository.BookRepository;
import com.library.management.maids.util.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class BookService {


    @Autowired
    private BookRepository bookRepository;

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBook(Long id) throws GeneralException {
        Optional<Book> book = bookRepository.findById(id);
        if (book.isEmpty())
            throw new GeneralException(HttpStatus.NOT_FOUND, ErrorCode.BOOK_DOES_NOT_EXIST);

        return book.get();
    }

    public Book saveBook(Book book) throws GeneralException {
        try {
            Book savedBook = bookRepository.save(book);
            log.info("Book successfully saved. [{}]", savedBook);
            return savedBook;
        } catch (DataIntegrityViolationException duplicateException) {
            log.warn("Cannot save a duplicate row");
            throw new GeneralException(HttpStatus.CONFLICT, ErrorCode.BOOK_ALREADY_EXISTS, duplicateException);
        } catch (Exception unexpectedException) {
            log.error("Unexpected exception occurred while saving a book. Book [{}]", book);
            throw unexpectedException;
        }
    }

    @Transactional
    public synchronized Book updateBook(Long id, Book bookDetails) throws GeneralException {
        Optional<Book> candidateBook = bookRepository.findById(id);

        if (candidateBook.isEmpty())
            throw new GeneralException(HttpStatus.NOT_FOUND, ErrorCode.BOOK_DOES_NOT_EXIST);

        Book updatedBook = candidateBook
                .map(existingBook -> {
                    existingBook.setTitle(bookDetails.getTitle());
                    existingBook.setAuthor(bookDetails.getAuthor());
                    existingBook.setIsbn(bookDetails.getIsbn());
                    existingBook.setPublicationYear(bookDetails.getPublicationYear());
                    existingBook.setQuantity(bookDetails.getQuantity());
                    return existingBook;
                }).get();

        try {
            Book savedBook = bookRepository.save(updatedBook);
            log.info("Book successfully saved. [{}]", savedBook);
            return updatedBook;
        } catch (Exception unexpectedException) {
            log.error("Unexpected exception occurred while updating a book. Existing Book [{}], Updated Book [{}]", candidateBook.get(), updatedBook);
            throw unexpectedException;
        }
    }

    @Transactional
    public synchronized Book deleteBook(Long id) throws GeneralException {
        Optional<Book> candidateBook = bookRepository.findById(id);

        if (candidateBook.isEmpty())
            throw new GeneralException(HttpStatus.NOT_FOUND, ErrorCode.BOOK_DOES_NOT_EXIST);

        // TODO: I am not sure If you're expecting me to delete the borrowed books (cascading)
        //  I am keeping it simple and not going to do anything about it other than cascading removal of parent model (uni-directional relation)
        //  but to solve this issue, we can use soft-delete to keep track in database or hard-delete to completely delete everything

        Book existingBook = candidateBook.get();
        try {
            bookRepository.delete(existingBook);
            log.info("Book successfully deleted. [{}]", existingBook);
            return existingBook;
        } catch (Exception unexpectedException) {
            log.error("Unexpected exception occurred while deleting a book.  Book [{}]", existingBook);
            throw unexpectedException;
        }
    }
}
