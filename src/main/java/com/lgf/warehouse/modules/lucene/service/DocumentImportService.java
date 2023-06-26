package com.lgf.warehouse.modules.lucene.service;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Arrays;

/**
 * 文档导入服务
 */
@Service
public class DocumentImportService {

    private static final String[] SUPPORTED_FILE_TYPES = {"txt", "doc", "docx", "xls", "xlsx"};

    @Autowired
    private LuceneService luceneService;

    public void indexDirectory(String dirPath) throws IOException {
        File dir = new File(dirPath);
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + dirPath);
        }
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                indexDirectory(file.getAbsolutePath());
            } else {
                String fileName = file.getName();
                String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
                if (Arrays.asList(SUPPORTED_FILE_TYPES).contains(fileType)) {
                    indexFile(file);
                }
            }
        }
    }

    private void indexFile(File file) throws IOException {
        String fileName = file.getName();
        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
        String content = null;
        switch (fileType) {
            case "txt":
                content = readTxtFile(file);
                break;
            case "doc":
            case "docx":
                content = readWordFile(file);
                break;
            case "xls":
            case "xlsx":
                content = readExcelFile(file);
                break;
            default:
                break;
        }
        if (content != null) {
            Document doc = new Document();
            doc.add(new TextField("filename", fileName, Field.Store.YES));
            doc.add(new TextField("content", content, Field.Store.YES));
            this.luceneService.addDocument(doc);
        }
    }

    private String readTxtFile(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    private String readWordFile(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(file)) {
            XWPFDocument doc = new XWPFDocument(fis);
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
            sb.append(extractor.getText());
        }
        return sb.toString();
    }

    private String readExcelFile(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(file)) {
            Workbook workbook = WorkbookFactory.create(fis);
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                for (Cell cell : row) {
                    sb.append(cell.getStringCellValue()).append(" ");
                }
            }
        }
        return sb.toString();
    }

}
