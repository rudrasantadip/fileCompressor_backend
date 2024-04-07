package com.project.compressor.fileCompressor.controllers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
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
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/file")
public class FileController 
{

    @Value("${project.file}")
    public String uploadPath;

    @Autowired
    FileService fService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadfile(@RequestParam ("file") MultipartFile file)
    {
      try {
        String response = fService.uploadFile(file, uploadPath);
        fService.FILENAME=response;
        return ResponseEntity.ok(response);

    } catch (IOException e) {
        
        e.printStackTrace();
    }  
    return ResponseEntity.badRequest().body(null);
    }

    @GetMapping(value = "/download/{file}")
    public void downloadFile(@PathVariable ("file") String name, HttpServletResponse response)
    {
        try 
        {
            InputStream fileStream = fService.getResource(uploadPath, name);
            StreamUtils.copy(fileStream, response.getOutputStream());
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + name + "\"");
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    @GetMapping(value = "/compress/{file}")
    public String fCompress(@PathVariable("file") String fileName)
    {
        String randomUuid = UUID.randomUUID().toString()+".huff";
        String filePath= uploadPath+File.separator+fileName;
        String compressedFile = uploadPath+File.separator+randomUuid;
        try {
            Huffman.compress(filePath, compressedFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Huffman hCompress = new Huffman(filePath,compressedFile );
        // hCompress.compress();
        fService.FILENAME_compressed=randomUuid;
        return fService.FILENAME_compressed;
    }

    @GetMapping(value = "/decompress/{file}")
    public String fdeCompress(@PathVariable("file") String fileName) throws ClassNotFoundException
    {
        String randomUuid = UUID.randomUUID().toString()+".txt";
        String filePath= uploadPath+File.separator+fileName;
        String decompressedFile = uploadPath+File.separator+randomUuid;
        try {
            Huffman.decompress(filePath, decompressedFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        fService.FILENAME_compressed=randomUuid;
        return fService.FILENAME_compressed;
    }
}
