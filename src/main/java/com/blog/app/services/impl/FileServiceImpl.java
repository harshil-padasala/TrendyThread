package com.blog.app.services.impl;

import com.blog.app.services.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {
    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {

        // File Name
        String name = file.getOriginalFilename();

        // random name generate file
        String randomID = UUID.randomUUID().toString();
        String fileName1 = randomID.concat(Objects.requireNonNull(name).substring(name.lastIndexOf(".")));

        // Full path
        String filePath = path + File.separator + fileName1;

        // Create folder if not created
        File f = new File(path);
        if (f.exists()) {
            boolean isCreated = f.mkdir();
        }

        // File Copy
        Files.copy(file.getInputStream(), Paths.get(filePath));

        return fileName1;
    }

    @Override
    public InputStream getResource(String path, String fileName) throws FileNotFoundException {
        String fullPath = path + File.separator + fileName;

        try (InputStream is = new FileInputStream(fullPath)) {
            return is;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
