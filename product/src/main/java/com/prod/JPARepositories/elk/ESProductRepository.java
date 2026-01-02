package com.prod.JPARepositories.elk;

import com.prod.models.elk.ESProducts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Optional;

public interface ESProductRepository extends ElasticsearchRepository<ESProducts, String> {
    Optional<ESProducts> findByDbId(String dbId);
    boolean existsByDbId(String dbId);
    Page<ESProducts> findAllByOrderByScoreDescUpdateAtDesc(Pageable pageable);
    Page<ESProducts> findByTitleContaining(String title, Pageable pageable);
    Page<ESProducts> findByTitle(String title, Pageable pageable);
}
