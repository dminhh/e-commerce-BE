package com.prod.RAG.services;

import com.prod.RAG.model.VisualSearchResult;
import org.springframework.web.multipart.MultipartFile;

public interface IVisualSearchService {
    VisualSearchResult searchByImage(MultipartFile image, int topK);
}
