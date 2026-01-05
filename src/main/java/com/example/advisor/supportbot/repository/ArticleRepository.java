package com.example.advisor.supportbot.repository;

import com.example.advisor.supportbot.model.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Article (Knowledge Base) entity operations.
 */
@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    /**
     * Find all published articles in a specific category.
     */
    List<Article> findByCategoryAndPublishedTrue(String category);

    /**
     * Find all published articles.
     */
    List<Article> findByPublishedTrue();

    /**
     * Search articles by title containing keyword.
     */
    List<Article> findByTitleContainingIgnoreCaseAndPublishedTrue(String keyword);

    /**
     * Search articles by tags containing keyword.
     */
    List<Article> findByTagsContainingIgnoreCaseAndPublishedTrue(String tag);

    /**
     * Find most viewed articles.
     */
    @Query("SELECT a FROM Article a WHERE a.published = true ORDER BY a.viewCount DESC")
    List<Article> findTopViewedArticles();

    /**
     * Find most helpful articles.
     */
    @Query("SELECT a FROM Article a WHERE a.published = true ORDER BY a.helpfulCount DESC")
    List<Article> findTopHelpfulArticles();

    /**
     * Get all unique categories.
     */
    @Query("SELECT DISTINCT a.category FROM Article a WHERE a.published = true")
    List<String> findAllCategories();
}
