package com.lgf.warehouse.modules.lucene.controller;

import com.lgf.warehouse.core.vo.R;
import com.lgf.warehouse.modules.lucene.service.DocumentImportService;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/lucene/import")
public class LuceneImportController {
    @Value("${base.file.save-path}")
    private String basePath;

    @Autowired
    private DocumentImportService documentImportService;

    @PostMapping(value = "/upload/single", consumes = "multipart/form-data")
    public R uploadSingleFile(@RequestParam(value = "file", required = true) MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return R.error("上传文件为空");
        }
        String dirPath = new Date().getTime() + "";
        String savePath = basePath + "/" + dirPath;
        Path path = Paths.get(savePath, file.getOriginalFilename());
        //如果路径的目录不存在，则创建目录
        if (!Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
        Files.write(path, file.getBytes());
        this.documentImportService.indexDirectory(savePath);
        return R.ok("导入成功");
    }

    @PostMapping(value = "/upload/multiple", consumes = "multipart/form-data")
    public R uploadMultipleFiles(@RequestParam(value = "files") MultipartFile[] files) throws IOException {
        if (files.length == 0) {
            return R.error("上传文件为空");
        }
        String dirPath = new Date().getTime() + "";
        String savePath = basePath + "/" + dirPath;

        for (MultipartFile file : files) {
            Path path = Paths.get(savePath, file.getOriginalFilename());
            //如果路径的目录不存在，则创建目录
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            Files.write(path, file.getBytes());
        }
        this.documentImportService.indexDirectory(savePath);
        return R.ok("导入成功");
    }
}
