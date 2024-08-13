package com.library.management.maids.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.management.maids.model.Book;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class FileHelper {

    private final ObjectMapper objectMapper;

    public FileHelper() {
        this.objectMapper = new ObjectMapper();
        objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        objectMapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public List<Book> readBooksFromFile(String fileName) throws Exception {
        URL url = FileHelper.class.getResource(fileName);
        File file = ResourceUtils.getFile(url);
        InputStream inputStream = new FileInputStream(file);
        return objectMapper.readValue(inputStream, new TypeReference<List<Book>>() {});
    }

    public Book readBookFromFile(String fileName) throws Exception {
        URL url = FileHelper.class.getResource(fileName);
        File file = ResourceUtils.getFile(url);
        InputStream inputStream = new FileInputStream(file);
        return objectMapper.readValue(inputStream, new TypeReference<Book>() {});
    }
}
