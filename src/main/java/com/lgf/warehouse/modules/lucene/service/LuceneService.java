package com.lgf.warehouse.modules.lucene.service;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * LuceneService
 */
@Service
public class LuceneService {
    @Autowired
    private Directory directory;
    @Autowired
    private IndexWriterConfig indexWriterConfig;

    @Autowired
    private Analyzer analyzer;

    @Autowired
    private QueryParser queryParser;

    public void addDocument(Document doc) throws IOException {
        IndexWriter indexWriter=this.getIndexWriter();
        indexWriter.addDocument(doc);
        indexWriter.commit();
        indexWriter.close();
    }

    public void deleteDocument(Term term) throws IOException {
        IndexWriter indexWriter=this.getIndexWriter();
        indexWriter.deleteDocuments(term);
        indexWriter.commit();
        indexWriter.close();
    }

    public List<String> search(String queryString,int numHits) throws IOException, ParseException {

        // 构建查询解析器，指定查询字段为 title 和 content
        String[] queryFields = {"title", "content"};
        MultiFieldQueryParser queryParser = new MultiFieldQueryParser(queryFields, this.analyzer);
        IndexSearcher indexSearcher=this.getIndexSearcher();
        // 解析查询字符串
        Query query = queryParser.parse(queryString);

        // 执行查询
        TopDocs topDocs = indexSearcher.search(query, numHits); // 返回前10个结果

        // 处理查询结果
        List<String> results = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document doc = indexSearcher.doc(scoreDoc.doc);
            String title = doc.get("title");
            String content = doc.get("content");
            String snippet = getSnippet(content, query.toString());
            results.add(title + "\n" + snippet); // 将标题和摘要合并为一行
        }
        return results;
    }

    // 获取查询结果的前后200个字符作为摘要
    private String getSnippet(String content, String query) {
        int maxLength = 200;
        int queryLength = query.length();
        int contentLength = content.length();
        int start = Math.max(content.toLowerCase().indexOf(query.toLowerCase()) - maxLength, 0);
        int end = Math.min(content.toLowerCase().indexOf(query.toLowerCase()) + queryLength + maxLength, contentLength);
        String snippet = content.substring(start, end);
        if (start > 0) {
            snippet = "..." + snippet;
        }
        if (end < contentLength) {
            snippet += "...";
        }
        return snippet;
    }

    private IndexWriter getIndexWriter() throws IOException {
        return new IndexWriter(this.directory, indexWriterConfig);
    }


    private IndexReader getIndexReader() throws IOException {
        return DirectoryReader.open(this.directory);
    }

    private IndexSearcher getIndexSearcher() throws IOException {
        return new IndexSearcher(getIndexReader());
    }
}