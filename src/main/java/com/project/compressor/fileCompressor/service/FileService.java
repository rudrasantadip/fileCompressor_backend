package com.project.compressor.fileCompressor.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService
{
    public String FILENAME;
    public String FILENAME_compressed;


    public String uploadFile(MultipartFile file, String path) throws IOException
    {
        String uuid = UUID.randomUUID().toString();
        String fileName = file.getOriginalFilename();
        fileName = uuid.concat(fileName.substring(fileName.lastIndexOf(".")));
        String uploadPath = path+File.separator+fileName;

        File toUpload = new File(path);
        if(!toUpload.exists())
        {
            toUpload.mkdir();
        }
        Files.copy(file.getInputStream(), Paths.get(uploadPath));
        return fileName;
    }


    public InputStream getResource(String path, String fileName) throws FileNotFoundException
    {
        String filePath = path+File.separator+fileName;
        System.out.println(filePath);
        InputStream file = new FileInputStream(filePath);
        return file;
    }
}


