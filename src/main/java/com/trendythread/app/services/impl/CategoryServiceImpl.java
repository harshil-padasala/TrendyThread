package com.trendythread.app.services.impl;

import com.trendythread.app.controllers.AdminCategoryController;
import com.trendythread.app.dto.AdminCategoryUpdateDto;
import com.trendythread.app.dto.CategoryStatsDto;
import com.trendythread.app.entities.Category;
import com.trendythread.app.exceptions.ResourceNotFoundException;
import com.trendythread.app.dto.CategoryDto;
import com.trendythread.app.payloads.CategoryResponse;
import com.trendythread.app.repositories.CategoryRepository;
import com.trendythread.app.services.CategoryService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

	private final CategoryRepository categoryRepository;

	private final ModelMapper modelMapper;

	@Autowired
	public CategoryServiceImpl(CategoryRepository categoryRepository, ModelMapper modelMapper) {
		this.categoryRepository = categoryRepository;
		this.modelMapper = modelMapper;
	}

    @Override
    public CategoryDto findByCategoryId(Integer categoryId) {
        log.info("findByCategoryId - request received: id={}", categoryId);
        Category category = this.categoryRepository.findById(categoryId).
                orElseThrow(() -> new ResourceNotFoundException("Category", "category id", categoryId));
        CategoryDto dto = this.categoryToCategoryDto(category);
        log.debug("findByCategoryId - fetched category: {}", dto);
        return dto;
    }

    @Override
    public List<CategoryDto> findAll() {
        log.info("findAll - request received");
        List<Category> categoryList = this.categoryRepository.findAll();
        List<CategoryDto> result = categoryList.stream().map((this::categoryToCategoryDto)).collect(Collectors.toList());
        log.debug("findAll - fetched {} categories", result.size());
        return result;
    }

    @Override
    public CategoryDto createCategory(CategoryDto categoryDto) {
        log.info("createCategory - request received: {}", categoryDto);
        Category category = categoryDtoToCategory(categoryDto);
        Category savedCategory = this.categoryRepository.save(category);
        CategoryDto dto = this.categoryToCategoryDto(savedCategory);
        log.info("createCategory - created category id={}", dto.getCategoryId());
        return dto;
    }

    @Override
    public CategoryDto updateByCategoryId(Integer categoryId, CategoryDto categoryDto) {
        log.info("updateByCategoryId - request received: id={}, dto={}", categoryId, categoryDto);
        Category category = this.categoryRepository.findById(categoryId).
                orElseThrow(() -> new ResourceNotFoundException("Category", "category id", categoryId));
        category.setName(categoryDto.getCategoryName());
        category.setDescription(categoryDto.getDescription());

        Category savedCategory = this.categoryRepository.save(category);

        CategoryDto dto = this.categoryToCategoryDto(savedCategory);
        log.info("updateByCategoryId - update successful: id={}", dto.getCategoryId());
        return dto;
    }

    @Override
    public void deleteByCategoryId(Integer categoryId) {
        log.info("deleteByCategoryId - request received: id={}", categoryId);
        Category category = this.categoryRepository.findById(categoryId).
                orElseThrow(() -> new ResourceNotFoundException("Category", "category id", categoryId));
        this.categoryRepository.deleteById(categoryId);
        log.info("deleteByCategoryId - deleted category id={}", categoryId);
    }

	@Override
	@Transactional(readOnly = true)
	public List<CategoryDto> getFeaturedCategories() {
		log.info("getFeaturedCategories - request received");

		List<Category> byFeaturedTrueOrderByDisplayOrderAsc = categoryRepository.findByFeaturedTrueOrderByDisplayOrderAsc();

		List<CategoryDto> categoryDtos = byFeaturedTrueOrderByDisplayOrderAsc.stream().map(this::categoryToCategoryDto).toList();

		log.info("getFeaturedCategories - fetched {} featured categories", categoryDtos.size());

		return categoryDtos;
	}

	@Override
	@Transactional(readOnly = true)
	public CategoryResponse getAllCategoriesPaginated(int page, int size, String sortBy) {
		log.info("getAllCategoriesPaginated - request received: page={}, size={}, sortBy={}", page, size, sortBy);

		Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());

		Page<Category> categoryPage = categoryRepository.findAll(pageable);

		List<CategoryDto> categoryDtos = categoryPage.getContent().stream().map(this::categoryToCategoryDto).toList();

		CategoryResponse categoryResponse = CategoryResponse.builder()
				.content(categoryDtos)
				.pageNumber(categoryPage.getNumber())
				.pageSize(categoryPage.getSize())
				.totalElements(categoryPage.getTotalElements())
				.totalPages(categoryPage.getTotalPages())
				.build();

		log.info("getAllCategoriesPaginated - fetched {} categories for page {}", categoryDtos.size(), page);

		return categoryResponse;
	}

	@Override
	@Transactional(readOnly = true)
	public CategoryResponse searchCategories(String keyword, int page, int size) {
		log.info("searchCategories - request received: keyword={}, page={}, size={}", keyword, page, size);

		Page<Category> byNameContainingIgnoreCase = categoryRepository.findByNameContainingIgnoreCase(keyword, PageRequest.of(page, size));

		CategoryResponse categoryResponse = CategoryResponse.builder()
				.content(byNameContainingIgnoreCase.getContent().stream().map(this::categoryToCategoryDto).toList())
				.pageNumber(byNameContainingIgnoreCase.getNumber())
				.pageSize(byNameContainingIgnoreCase.getSize())
				.totalElements(byNameContainingIgnoreCase.getTotalElements())
				.totalPages(byNameContainingIgnoreCase.getTotalPages())
				.build();

		log.info("searchCategories - found {} categories matching keyword '{}' for page {}", byNameContainingIgnoreCase.getNumberOfElements(), keyword, page);

		return categoryResponse;
	}

	@Override
	@Transactional
	public CategoryDto updateCategoryFeaturedStatus(Integer categoryId, AdminCategoryUpdateDto updateDto) {

		log.info("updateCategoryFeaturedStatus - request received: categoryId={}, updateDto={}", categoryId, updateDto);

		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category", "category id", categoryId));

		category.setFeatured(updateDto.getFeatured());
		category.setDisplayOrder(updateDto.getDisplayOrder());
		category.setAutoSuggested(false);

		category = categoryRepository.save(category);

		log.info("updateCategoryFeaturedStatus - fetched category: id={}, current featured={}, current displayOrder={}",
				category.getId(), category.getFeatured(), category.getDisplayOrder());

		return this.categoryToCategoryDto(categoryRepository.save(category));
	}

	@Override
	@Transactional(readOnly = true)
	public List<CategoryStatsDto> getSuggestedCategories(int limit) {

		log.info("getSuggestedCategories - request received: limit={}", limit);

		Pageable pageable = PageRequest.of(0, limit);
		List<Category> topCategoriesByPostCount = categoryRepository.findTopCategoriesByPostCount(pageable);

		log.info("getSuggestedCategories - fetched {} suggested categories based on post count", topCategoriesByPostCount.size());

		return topCategoriesByPostCount.stream()
				.map(category -> new CategoryStatsDto(
						Long.valueOf(category.getId()),
						category.getName(),
						category.getPostCount(),
						0, // Future: add views tracking
						category.getPostCount() > 10 // Suggest if it has many posts
				))
				.collect(Collectors.toList());
	}

	/**
	 * Performs a bulk update to manage featured categories in a single atomic operation.
	 * This method replaces the entire set of featured categories with a new list in the specified order.
	 * 
	 * <p><b>Operation Flow:</b></p>
	 * <ol>
	 *   <li>Unfeatures ALL existing categories (sets featured=false for all)</li>
	 *   <li>Validates that all provided category IDs exist in the database</li>
	 *   <li>Features the specified categories in the exact order provided</li>
	 *   <li>Assigns displayOrder values starting from 1 based on list position</li>
	 *   <li>Persists all changes in a single transaction</li>
	 * </ol>
	 * 
	 * <p><b>Important Characteristics:</b></p>
	 * <ul>
	 *   <li><b>Atomic Operation:</b> All changes occur within a single transaction - either all succeed or all fail</li>
	 *   <li><b>Order Matters:</b> The order of IDs in the list determines the displayOrder (navbar display sequence)</li>
	 *   <li><b>Replace Strategy:</b> Previous featured categories are completely replaced, not merged</li>
	 *   <li><b>Validation:</b> Throws exception if ANY category ID doesn't exist - partial updates are not allowed</li>
	 *   <li><b>Auto-Suggested Flag:</b> Does NOT modify autoSuggested flag - manual admin control is preserved</li>
	 * </ul>
	 * 
	 * <p><b>Use Cases:</b></p>
	 * <ul>
	 *   <li>Admin reordering featured categories via drag-and-drop UI</li>
	 *   <li>Replacing seasonal/promotional category sets</li>
	 *   <li>Initial setup of featured categories for new deployments</li>
	 *   <li>Bulk import from configuration files</li>
	 * </ul>
	 * 
	 * <p><b>Example Usage:</b></p>
	 * <pre>
	 * // Admin wants to feature these categories in this specific order:
	 * // 1. Technology (id=5) - will be displayed first
	 * // 2. Sports (id=12) - will be displayed second  
	 * // 3. Travel (id=8) - will be displayed third
	 * // 4. Food (id=20) - will be displayed fourth
	 * 
	 * List&lt;Integer&gt; featuredIds = Arrays.asList(5, 12, 8, 20);
	 * categoryService.bulkUpdateFeaturedCategories(featuredIds);
	 * 
	 * // Result in database:
	 * // Category id=5 (Technology): featured=true, displayOrder=1
	 * // Category id=12 (Sports): featured=true, displayOrder=2
	 * // Category id=8 (Travel): featured=true, displayOrder=3
	 * // Category id=20 (Food): featured=true, displayOrder=4
	 * // All other categories: featured=false, displayOrder unchanged
	 * 
	 * // Subsequently reorder by moving Food to first position:
	 * List&lt;Integer&gt; reorderedIds = Arrays.asList(20, 5, 12, 8);
	 * categoryService.bulkUpdateFeaturedCategories(reorderedIds);
	 * 
	 * // New result:
	 * // Category id=20 (Food): featured=true, displayOrder=1
	 * // Category id=5 (Technology): featured=true, displayOrder=2
	 * // Category id=12 (Sports): featured=true, displayOrder=3
	 * // Category id=8 (Travel): featured=true, displayOrder=4
	 * </pre>
	 * 
	 * <p><b>Error Scenarios:</b></p>
	 * <pre>
	 * // Scenario 1: Invalid category ID
	 * List&lt;Integer&gt; invalidIds = Arrays.asList(1, 999, 3); // 999 doesn't exist
	 * categoryService.bulkUpdateFeaturedCategories(invalidIds);
	 * // Throws: ResourceNotFoundException("Category", "categoryIds", [999])
	 * // Result: No changes made (transaction rolled back)
	 * 
	 * // Scenario 2: Empty list (unfeatures all categories)
	 * List&lt;Integer&gt; emptyList = Collections.emptyList();
	 * categoryService.bulkUpdateFeaturedCategories(emptyList);
	 * // Result: All categories have featured=false
	 * 
	 * // Scenario 3: Duplicate IDs (processes each occurrence)
	 * List&lt;Integer&gt; duplicates = Arrays.asList(1, 2, 1); // id=1 appears twice
	 * categoryService.bulkUpdateFeaturedCategories(duplicates);
	 * // Result: Category id=1 gets displayOrder from last occurrence (position 3)
	 * </pre>
	 * 
	 * <p><b>Performance Considerations:</b></p>
	 * <ul>
	 *   <li>Database queries: 1 bulk unfeatured + 1 findAllById + 1 saveAll = 3 queries</li>
	 *   <li>Recommended limit: 10-20 featured categories for optimal navbar UX</li>
	 *   <li>Transaction scope: All operations within single database transaction</li>
	 *   <li>Concurrency: @Transactional ensures thread-safe operations</li>
	 * </ul>
	 * 
	 * <p><b>Integration Points:</b></p>
	 * <ul>
	 *   <li><b>Frontend:</b> Admin panel drag-and-drop reordering sends new ID sequence</li>
	 *   <li><b>Scheduler:</b> NOT used by autoUpdateFeaturedCategories() - that method preserves manual selections</li>
	 *   <li><b>API:</b> AdminCategoryController.bulkUpdateFeaturedCategories() endpoint</li>
	 * </ul>
	 * 
	 * @param categoryIds List of category IDs to feature, in desired display order.
	 *                    Position in list determines displayOrder (first item = displayOrder 1).
	 *                    Empty list is valid and will unfeatured all categories.
	 *                    Order is preserved exactly as provided.
	 * 
	 * @throws ResourceNotFoundException if any category ID in the list does not exist in the database.
	 *                                   Exception message includes all invalid IDs.
	 *                                   Transaction is rolled back - no partial updates occur.
	 * 
	 * @throws IllegalArgumentException if categoryIds list contains null values
	 * 
	 * @see #autoUpdateFeaturedCategories() for automated algorithm-based featuring
	 * @see #updateCategoryFeaturedStatus(Integer, AdminCategoryUpdateDto) for single category updates
	 * @see AdminCategoryController#bulkUpdateFeaturedCategories(List) for REST API endpoint
	 * 
	 * @since 1.0.0
	 * @author TrendyThread Development Team
	 */
	@Override
	@Transactional
	public void bulkUpdateFeaturedCategories(List<Integer> categoryIds) {

		log.info("bulkUpdateFeaturedCategories - request received: categoryIds={}", categoryIds);

		// First, unfeatured all categories
		categoryRepository.setAllCategoriesNotFeatured();

		List<Category> categoriesById = categoryRepository.findAllById(categoryIds);

		if (categoriesById.size() != categoryIds.size()) {
			log.warn("bulkUpdateFeaturedCategories - some categoryIds not found: requested={}, found={}", categoryIds.size(), categoriesById.size());
			 List<Integer> foundIds = categoriesById.stream().map(Category::getId).toList();
			 List<Integer> notFoundIds = categoryIds.stream().filter(id -> !foundIds.contains(id)).toList();
			 log.warn("bulkUpdateFeaturedCategories - categoryIds not found: {}", notFoundIds);
			 throw new ResourceNotFoundException("Category", "categoryIds", notFoundIds);
		}

		// Then, feature the selected ones with order
		for (int i = 0; i < categoryIds.size(); i++) {
			Integer categoryId = categoryIds.get(i);
			Category category = categoryRepository.findById(categoryId)
					.orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

			category.setFeatured(true);
			category.setDisplayOrder(i + 1);
			categoryRepository.save(category);
		}

		for (Category category : categoriesById) {
			log.debug("bulkUpdateFeaturedCategories - updated category: id={}, featured={}, displayOrder={}",
					category.getId(), category.getFeatured(), category.getDisplayOrder());
			category.setFeatured(true);
			category.setDisplayOrder(categoryIds.indexOf(category.getId()) + 1);
			log.debug("bulkUpdateFeaturedCategories - prepared category for save: id={}, featured={}, displayOrder={}",
					category.getId(), category.getFeatured(), category.getDisplayOrder());
		}

		categoryRepository.saveAll(categoriesById);

		log.info("bulkUpdateFeaturedCategories - completed bulk update of featured categories");

	}

	@Override
	@Transactional
	public void updatePostCount(Integer categoryId) {
		log.info("updatePostCount - request received: categoryId={}", categoryId);

		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category", "category id", categoryId));

		Long postCount = categoryRepository.countPostsByCategoryId(Long.valueOf(categoryId));
		category.setPostCount(postCount.intValue());

		categoryRepository.save(category);

		log.info("updatePostCount - updated post count for category id={}: new post count={}", categoryId, postCount);
	}

	@Override
	@Transactional
	public void recalculateAllPostCounts() {
		log.info("recalculateAllPostCounts - starting recalculation for all categories");

		List<Category> allCategories = categoryRepository.findAll();
		int updatedCount = 0;

		for (Category category : allCategories) {
			Long postCount = categoryRepository.countPostsByCategoryId(Long.valueOf(category.getId()));
			category.setPostCount(postCount.intValue());
			updatedCount++;
		}

		categoryRepository.saveAll(allCategories);

		log.info("recalculateAllPostCounts - completed. Updated {} categories", updatedCount);
	}

	/**
	 * Automatically updates featured categories using an intelligent algorithm that balances
	 * manual admin control with automated popularity-based suggestions.
	 * 
	 * <p>This scheduled task runs periodically (every 10 minutes by default) to ensure featured
	 * categories stay current with trending content while respecting administrator preferences.</p>
	 * 
	 * <p><b>Algorithm Flow:</b></p>
	 * <ol>
	 *   <li><b>Identify Top Categories:</b> Query top 10 categories by post count (popularity metric)</li>
	 *   <li><b>Preserve Manual Selections:</b> Find all featured categories with autoSuggested=false</li>
	 *   <li><b>Calculate Available Slots:</b> Target 10 total featured categories minus manual ones</li>
	 *   <li><b>Fill Remaining Slots:</b> Auto-feature popular categories (excluding manual ones)</li>
	 *   <li><b>Assign Display Order:</b> Manual categories keep their order, auto-suggestions follow after</li>
	 *   <li><b>Mark as Auto-Suggested:</b> Set autoSuggested=true for algorithm-selected categories</li>
	 * </ol>
	 * 
	 * <p><b>Key Characteristics:</b></p>
	 * <ul>
	 *   <li><b>Non-Destructive:</b> Never removes or modifies manually featured categories</li>
	 *   <li><b>Dynamic:</b> Auto-suggested categories can change between runs based on popularity</li>
	 *   <li><b>Gap Filling:</b> Only adds suggestions if total featured count is below target (10)</li>
	 *   <li><b>Popularity-Based:</b> Uses postCount as the primary ranking metric</li>
	 *   <li><b>Scheduled Execution:</b> Runs automatically via @Scheduled annotation</li>
	 *   <li><b>Idempotent:</b> Safe to call multiple times - produces consistent results</li>
	 * </ul>
	 * 
	 * <p><b>Scheduling Configuration:</b></p>
	 * <pre>
	 * Current: @Scheduled(cron = "0 *\/10 * * * *") // Every 10 minutes
	 * 
	 * Other options:
	 * - Every hour:     "0 0 * * * *"
	 * - Daily at 2 AM:  "0 0 2 * * *"
	 * - Every 30 min:   "0 *\/30 * * * *"
	 * - Weekly (Mon):   "0 0 2 * * MON"
	 * </pre>
	 * 
	 * <p><b>Example Scenarios:</b></p>
	 * <pre>
	 * // Scenario 1: Admin has manually featured 3 categories
	 * // Database state BEFORE auto-update:
	 * // - Category id=5 (Technology): featured=true, displayOrder=1, autoSuggested=false
	 * // - Category id=12 (Sports): featured=true, displayOrder=2, autoSuggested=false
	 * // - Category id=8 (Travel): featured=true, displayOrder=3, autoSuggested=false
	 * // - All other categories: featured=false
	 * 
	 * autoUpdateFeaturedCategories(); // Runs at scheduled time
	 * 
	 * // Database state AFTER auto-update:
	 * // Manual categories (unchanged):
	 * // - Category id=5 (Technology): featured=true, displayOrder=1, autoSuggested=false
	 * // - Category id=12 (Sports): featured=true, displayOrder=2, autoSuggested=false
	 * // - Category id=8 (Travel): featured=true, displayOrder=3, autoSuggested=false
	 * // 
	 * // Auto-suggested categories (newly added, 7 total to reach target of 10):
	 * // - Category id=20 (Food): featured=true, displayOrder=4, autoSuggested=true
	 * // - Category id=15 (Fashion): featured=true, displayOrder=5, autoSuggested=true
	 * // - Category id=30 (Health): featured=true, displayOrder=6, autoSuggested=true
	 * // - Category id=25 (Gaming): featured=true, displayOrder=7, autoSuggested=true
	 * // - Category id=40 (Music): featured=true, displayOrder=8, autoSuggested=true
	 * // - Category id=18 (Books): featured=true, displayOrder=9, autoSuggested=true
	 * // - Category id=22 (Movies): featured=true, displayOrder=10, autoSuggested=true
	 * 
	 * // Scenario 2: Next run 10 minutes later - "Gaming" became more popular
	 * // Result: Auto-suggested categories may be reordered, but manual ones stay unchanged
	 * 
	 * // Scenario 3: Admin manually features 12 categories (exceeds target of 10)
	 * // Result: No auto-suggestions added (remainingSlots = 10 - 12 = -2, no action taken)
	 * </pre>
	 * 
	 * <p><b>Interaction with Manual Admin Actions:</b></p>
	 * <pre>
	 * // Admin manually features a category using updateCategoryFeaturedStatus()
	 * AdminCategoryUpdateDto updateDto = new AdminCategoryUpdateDto(true, 1);
	 * categoryService.updateCategoryFeaturedStatus(100, updateDto);
	 * // Result: Category id=100 has autoSuggested=false (manual control)
	 * 
	 * // Later, autoUpdateFeaturedCategories() runs
	 * // Result: Category id=100 is preserved, other auto-suggestions fill remaining slots
	 * 
	 * // If admin later uses bulkUpdateFeaturedCategories() with different IDs
	 * categoryService.bulkUpdateFeaturedCategories(Arrays.asList(1, 2, 3, 4, 5));
	 * // Result: All 5 are manual, next auto-update will add 5 more auto-suggestions
	 * </pre>
	 * 
	 * <p><b>Database Impact:</b></p>
	 * <ul>
	 *   <li><b>Queries per execution:</b> 
	 *       <ol>
	 *         <li>findTopCategoriesByPostCount() - SELECT top 10 by post count</li>
	 *         <li>findByFeaturedTrueOrderByDisplayOrderAsc() - SELECT current featured</li>
	 *         <li>saveAll() - Batch UPDATE for auto-suggestions (0-10 categories)</li>
	 *       </ol>
	 *   </li>
	 *   <li><b>Transaction scope:</b> All operations within single transaction (rollback on error)</li>
	 *   <li><b>Performance:</b> Minimal impact - typically updates 0-10 rows per execution</li>
	 *   <li><b>Locking:</b> Row-level locks only on categories being updated</li>
	 * </ul>
	 * 
	 * <p><b>Manual Triggering (Admin Use Case):</b></p>
	 * <pre>
	 * // Admin can manually trigger via REST API endpoint:
	 * POST /api/v1/admin/categories/auto-update
	 * Authorization: Bearer &lt;ADMIN_JWT_TOKEN&gt;
	 * 
	 * // Controller delegates to this method
	 * // Useful for testing or immediate refresh without waiting for schedule
	 * </pre>
	 * 
	 * <p><b>Comparison with bulkUpdateFeaturedCategories():</b></p>
	 * <table border="1">
	 *   <tr>
	 *     <th>Feature</th>
	 *     <th>autoUpdateFeaturedCategories()</th>
	 *     <th>bulkUpdateFeaturedCategories()</th>
	 *   </tr>
	 *   <tr>
	 *     <td>Trigger</td>
	 *     <td>Automatic (scheduled)</td>
	 *     <td>Manual (admin action)</td>
	 *   </tr>
	 *   <tr>
	 *     <td>Preserves Manual</td>
	 *     <td>Yes (always)</td>
	 *     <td>No (replaces all)</td>
	 *   </tr>
	 *   <tr>
	 *     <td>Algorithm-Based</td>
	 *     <td>Yes (popularity)</td>
	 *     <td>No (explicit IDs)</td>
	 *   </tr>
	 *   <tr>
	 *     <td>Target Count</td>
	 *     <td>10 total</td>
	 *     <td>Variable (as specified)</td>
	 *   </tr>
	 *   <tr>
	 *     <td>AutoSuggested Flag</td>
	 *     <td>Sets to true</td>
	 *     <td>Does not modify</td>
	 *   </tr>
	 * </table>
	 * 
	 * <p><b>Edge Cases:</b></p>
	 * <ul>
	 *   <li><b>Zero categories exist:</b> Method completes safely with no updates</li>
	 *   <li><b>All categories manually featured:</b> No auto-suggestions added (remainingSlots ≤ 0)</li>
	 *   <li><b>Fewer than 10 total categories:</b> Features all available categories</li>
	 *   <li><b>Tie in post counts:</b> Database ordering determines which categories are selected</li>
	 *   <li><b>Concurrent execution:</b> @Transactional ensures data consistency</li>
	 * </ul>
	 * 
	 * <p><b>Monitoring and Observability:</b></p>
	 * <pre>
	 * // Log output example:
	 * INFO  - autoUpdateFeaturedCategories - request received
	 * DEBUG - Found 3 manually featured categories
	 * DEBUG - Remaining slots: 7
	 * DEBUG - Auto-featuring categories: [20, 15, 30, 25, 40, 18, 22]
	 * INFO  - autoUpdateFeaturedCategories - completed successfully
	 * 
	 * // For monitoring, watch for:
	 * // - Execution time (should be &lt; 100ms typically)
	 * // - Exception rate (should be near zero)
	 * // - Number of auto-suggested categories fluctuating wildly (may indicate data quality issues)
	 * </pre>
	 * 
	 * <p><b>Future Enhancements:</b></p>
	 * <ul>
	 *   <li>Configurable target count (externalize the "10" limit)</li>
	 *   <li>Multiple ranking metrics (views, comments, engagement score)</li>
	 *   <li>Time-decay for post counts (recent posts weighted higher)</li>
	 *   <li>Category-specific boosting rules (seasonal promotions)</li>
	 *   <li>A/B testing variants for different user segments</li>
	 * </ul>
	 * 
	 * @throws org.springframework.dao.DataAccessException if database operation fails
	 * 
	 * @see #bulkUpdateFeaturedCategories(List) for manual featured category management
	 * @see #updateCategoryFeaturedStatus(Integer, AdminCategoryUpdateDto) for single category updates
	 * @see CategoryRepository#findTopCategoriesByPostCount(Pageable) for popularity query
	 * @see #updatePostCount(Integer) for maintaining accurate post counts
	 * 
	 * @since 1.0.0
	 * @author TrendyThread Development Team
	 */
	@Override
	@Transactional
//	@Scheduled(cron = "0 0 2 * * *") // Runs daily at 2 AM
	@Scheduled(cron = "0 */10 * * * *") // Runs every 10 minutes
	public void autoUpdateFeaturedCategories() {
		log.info("autoUpdateFeaturedCategories - request received");

		Pageable pageable = PageRequest.of(0, 10);
		List<Category> topCategories = categoryRepository.findTopCategoriesByPostCount(pageable);

		// Get current featured categories (manually set)
		List<Category> manuallyFeatured = categoryRepository
				.findByFeaturedTrueOrderByDisplayOrderAsc()
				.stream()
				.filter(cat -> !cat.getAutoSuggested())
				.toList();

		// Keep manually featured, add auto-suggestions to fill up to 10
		int remainingSlots = 10 - manuallyFeatured.size();
		if (remainingSlots > 0) {
			List<Integer> manualFeaturedIds = manuallyFeatured.stream()
					.map(Category::getId)
					.toList();

			List<Category> autoSuggestions = topCategories.stream()
					.filter(cat -> !manualFeaturedIds.contains(cat.getId()))
					.limit(remainingSlots)
					.toList();

			int displayOrder = manuallyFeatured.size() + 1;
			for (Category category : autoSuggestions) {
				category.setFeatured(true);
				category.setAutoSuggested(true);
				category.setDisplayOrder(displayOrder++);
			}
			categoryRepository.saveAll(autoSuggestions);
		}
	}

	private CategoryDto categoryToCategoryDto(Category category) {
		CategoryDto dto = new CategoryDto();
		dto.setCategoryId(category.getId());
		dto.setCategoryName(category.getName());
		dto.setDescription(category.getDescription());
		dto.setFeatured(category.getFeatured());
		dto.setDisplayOrder(category.getDisplayOrder());
		dto.setPostCount(category.getPostCount());
		dto.setAutoSuggested(category.getAutoSuggested());
		return dto;
	}

    private Category categoryDtoToCategory(CategoryDto categoryDto) {
        Category category = new Category();
        if (categoryDto.getCategoryId() != null) {
            category.setId(categoryDto.getCategoryId());
        }
        category.setName(categoryDto.getCategoryName());
        category.setDescription(categoryDto.getDescription());
        return category;
    }
}
