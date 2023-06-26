package com.lgf.warehouse.modules.lucene.config;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.nio.file.Paths;

@Configuration
public class LuceneConfig {
    @Value("${functions.lucene.index.path}")
    private String indexPath;

    @Bean
    public Directory directory() throws IOException {
        return FSDirectory.open(Paths.get(indexPath));
    }

    @Bean
    public Analyzer analyzer() {
        return new IKAnalyzer();
    }

    @Bean
    public IndexWriterConfig indexWriterConfig() {
        return new IndexWriterConfig(analyzer());
    }



    @Bean
    public QueryParser queryParser() {
        return new QueryParser("content", analyzer());
    }
}
