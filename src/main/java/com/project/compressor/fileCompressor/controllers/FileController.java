package com.project.compressor.fileCompressor.controllers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


import com.project.compressor.fileCompressor.service.FileService;
import com.project.compressor.fileCompressor.utils.Huffman;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/file")
public class FileController 
{

    @Value("${project.file}")
    public String uploadPath;

    @Autowired
    FileService fService;

    @PostMapping("/upload")
    public String uploadfile(@RequestParam ("file") MultipartFile file)
    {
      try {
        String response = fService.uploadFile(file, uploadPath);
        fService.FILENAME=response;
        return response;

    } catch (IOException e) {
        
        e.printStackTrace();
    }  
    return "";
    }

    @GetMapping(value = "/download/{file}")
    public String downloadFile(@PathVariable ("file") String name, HttpServletResponse response)
    {
        try 
        {
            InputStream fileStream = fService.getResource(uploadPath, name);
            StreamUtils.copy(fileStream, response.getOutputStream());
            return name;
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        return "";
    }

    @GetMapping(value = "/compress/{file}")
    public String fCompress(@PathVariable("file") String fileName)
    {
        String randomUuid = UUID.randomUUID().toString()+".huff";
        String filePath= uploadPath+File.separator+fileName;
        String compressedFile = uploadPath+File.separator+randomUuid;
        Huffman hCompress = new Huffman(filePath,compressedFile );
        hCompress.compress();
        fService.FILENAME_compressed=randomUuid;
        return fService.FILENAME_compressed;
    }
}
