package com.lgf.warehouse.modules.ai.openai.functions.lucene.service;

import com.lgf.warehouse.modules.ai.openai.functions.AbsFunctionService;
import com.lgf.warehouse.modules.ai.openai.functions.FunctionAnnotation;
import com.lgf.warehouse.modules.lucene.service.LuceneService;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class LuceneSearchService extends AbsFunctionService {
@Value("${functions.lucene.default-page-size:10}")
    private int defaultPageSize = 10;

    @Autowired
    private LuceneService luceneService;


    /**
     * 搜索
     * @param queryKey
     * @return
     */
    @FunctionAnnotation(describe = "本地知识库检索服务")
    public List<String> search(@FunctionAnnotation(describe = "分析Prompt中的问题内容，将问题内容传入queryKey中，例如Prompt内容是：怎么拍摄一段好的视频？那么问题内容为：拍摄一段好的视频"
            ,required = true) String queryKey) {
        try {
            List<String> result= luceneService.search(queryKey, defaultPageSize);
            return result;
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public String getFunctionName() {
        return "luceneSearchService";
    }

    @Override
    public Class getCla() {
        return this.getClass();
    }
}
