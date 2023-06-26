package com.lgf.warehouse.modules.lucene.controller;

import com.lgf.warehouse.core.vo.R;
import com.lgf.warehouse.modules.lucene.service.LuceneService;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/lucene")
public class LuceneSearchController {

    @Autowired
    private LuceneService luceneService;

    @PostMapping("/index")
    public void index(@RequestBody Map<String, String> document) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("title", document.get("title"), Field.Store.YES));
        doc.add(new TextField("content", document.get("content"), Field.Store.YES));
        luceneService.addDocument(doc);
    }
//
//    @GetMapping("/search")
//    public List<Document> search(@RequestParam String q, @RequestParam(defaultValue = "10") int num) throws IOException, ParseException, InvalidTokenOffsetsException {
//        return luceneService.search(q, num);
//    }


    @GetMapping("/search")
    public R search(@RequestParam String q, @RequestParam(defaultValue = "10") int num) throws IOException, ParseException, InvalidTokenOffsetsException {
        List<String> list=this.luceneService.search(q,num);
        return R.ok(list);
    }
}
