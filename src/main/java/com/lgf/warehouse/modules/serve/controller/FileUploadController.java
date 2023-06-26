package com.lgf.warehouse.modules.serve.controller;

import com.lgf.warehouse.core.vo.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

@RestController
@RequestMapping("/file")
public class FileUploadController {
    @Value("${base.file.save-path}")
    private String savePath;

    @PostMapping(value = "/upload/single", consumes = "multipart/form-data")
    public R uploadSingleFile(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return R.error("上传文件为空");
        }
        String dirPath = new Date().getTime() + "";
        Path path = Paths.get(savePath + File.pathSeparator + dirPath, file.getOriginalFilename());
        Files.write(path, file.getBytes());
        return R.ok(dirPath);
    }

    @PostMapping(value = "/upload/multiple", consumes = "multipart/form-data")
    public R uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) throws IOException {
        if (files.length == 0) {
            return R.error("上传文件为空");
        }
        String dirPath = new Date().getTime() + "";
        for (MultipartFile file : files) {
            Path path = Paths.get(savePath + File.pathSeparator + dirPath, file.getOriginalFilename());
            Files.write(path, file.getBytes());
        }
        return R.ok(dirPath);
    }
}