package com.library.management.maids.service;

import com.library.management.maids.exception.GeneralException;
import com.library.management.maids.model.Patron;
import com.library.management.maids.repository.PatronRepository;
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
public class PatronService {


    @Autowired
    private PatronRepository patronRepository;

    public List<Patron> getAllPatrons() {
        return patronRepository.findAll();
    }

    public Patron getPatron(Long id) throws GeneralException {
        Optional<Patron> patron = patronRepository.findById(id);
        if (patron.isEmpty())
            throw new GeneralException(HttpStatus.NOT_FOUND, ErrorCode.PATRON_DOES_NOT_EXIST);

        return patron.get();
    }

    public Patron savePatron(Patron patron) throws GeneralException {
        try {
            Patron savedPatron = patronRepository.save(patron);
            log.info("Patron successfully saved. [{}]", savedPatron);
            return savedPatron;
        }
        catch (DataIntegrityViolationException duplicateException) {
            log.warn("Cannot save a duplicate row");
            throw new GeneralException(HttpStatus.CONFLICT, ErrorCode.PATRON_ALREADY_EXISTS, duplicateException);
        }
        catch (Exception unexpectedException) {
            log.error("Unexpected exception occurred while saving a Patron. Patron [{}]", patron);
            throw unexpectedException;
        }
    }

    @Transactional
    public synchronized Patron updatePatron(Long id, Patron patronDetails) throws GeneralException {
        Optional<Patron> candidatePatron = patronRepository.findById(id);

        if (candidatePatron.isEmpty())
            throw new GeneralException(HttpStatus.NOT_FOUND, ErrorCode.PATRON_DOES_NOT_EXIST);

        Patron updatedPatron = candidatePatron
                .map(existingPatron -> {
                    existingPatron.setName(patronDetails.getName());
                    existingPatron.setContactInfo(patronDetails.getContactInfo());
                    return existingPatron;
                }).get();

        try {
            Patron savedPatron = patronRepository.save(updatedPatron);
            log.info("Patron successfully saved. [{}]", savedPatron);
            return savedPatron;
        } catch (Exception unexpectedException) {
            log.error("Unexpected exception occurred while updating a Patron. Existing Patron [{}], Updated Patron [{}]", candidatePatron.get(), updatedPatron);
            throw unexpectedException;
        }
    }

    @Transactional
    public synchronized Patron deletePatron(Long id) throws GeneralException {
        Optional<Patron> candidatePatron = patronRepository.findById(id);

        if (candidatePatron.isEmpty())
            throw new GeneralException(HttpStatus.NOT_FOUND, ErrorCode.PATRON_DOES_NOT_EXIST);


        // TODO: I am not sure If you're expecting me to delete the borrowed books (cascading)
        //  I am keeping it simple and not going to do anything about it other than cascading removal of parent model (uni-directional relation)
        //  but to solve this issue, we can use soft-delete to keep track in database or hard-delete to completely delete everything

        try {
            patronRepository.delete(candidatePatron.get());
            log.info("Patron successfully deleted. [{}]", candidatePatron.get());
            return candidatePatron.get();
        } catch (Exception unexpectedException) {
            log.error("Unexpected exception occurred while deleting a Patron.  Patron [{}]", candidatePatron.get());
            throw unexpectedException;
        }
    }
}
