package com.trendythread.app.repositories;

import com.trendythread.app.entities.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

	// Existing method
	Optional<Category> findByName(String categoryName);

	// Find featured categories ordered by displayOrder
	List<Category> findByFeaturedTrueOrderByDisplayOrderAsc();

	// Find all categories with pagination
	Page<Category> findAllByOrderByNameAsc(Pageable pageable);

	// Search categories by name
	Page<Category> findByNameContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

	// Get top categories by post count for auto-suggestion
	@Query("SELECT c FROM Category c ORDER BY c.postCount DESC")
	List<Category> findTopCategoriesByPostCount(Pageable pageable);

	// Count total posts in a category
	@Query("SELECT COUNT(p) FROM Post p WHERE p.category.id = :categoryId")
	Long countPostsByCategoryId(@Param("categoryId") Long categoryId);

	// Get categories with their post counts
	@Query("SELECT c FROM Category c LEFT JOIN FETCH c.posts WHERE c.id = :categoryId")
	Optional<Category> findByIdWithPosts(@Param("categoryId") Long categoryId);

	@Modifying
	@Query("UPDATE Category c SET c.featured = false")
	void setAllCategoriesNotFeatured();
}
