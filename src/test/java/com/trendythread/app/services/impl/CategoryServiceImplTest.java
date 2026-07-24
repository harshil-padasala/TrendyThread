package com.trendythread.app.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trendythread.app.constants.IRedisConstant;
import com.trendythread.app.constants.IRedisTtlConstant;
import com.trendythread.app.dto.AdminCategoryUpdateDto;
import com.trendythread.app.dto.CategoryDto;
import com.trendythread.app.entities.Category;
import com.trendythread.app.exceptions.ResourceNotFoundException;
import com.trendythread.app.repositories.CategoryRepository;
import com.trendythread.app.util.RedisCacheSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private RedisCacheSupport redisCacheSupport;

    @Mock
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void bulkUpdateFeaturedCategoriesRefreshesCategoryCachesAfterCommit() {
        CategoryServiceImpl service = new CategoryServiceImpl(categoryRepository, redisCacheSupport, objectMapper);
        List<Integer> requestedCategoryIds = List.of(1, 2);
        Category oldFeaturedCategory = category(3, "Old Featured", false, 3);
        Category firstRequestedCategory = category(1, "Technology", false, 0);
        Category secondRequestedCategory = category(2, "Sports", false, 0);
        Category updatedFirstCategory = category(1, "Technology", true, 1);
        Category updatedSecondCategory = category(2, "Sports", true, 2);
        Category updatedFormerFeaturedCategory = category(3, "Old Featured", false, 3);
        Set<String> cachedCategoryKeys = Set.of(
                IRedisConstant.REDIS_CATEGORY_FEATURED,
                IRedisConstant.REDIS_CATEGORY_KEYWORD.concat("sports")
        );

        when(categoryRepository.findByFeaturedTrueOrderByDisplayOrderAsc())
                .thenReturn(List.of(oldFeaturedCategory));
        when(categoryRepository.findAllById(requestedCategoryIds))
                .thenReturn(List.of(firstRequestedCategory, secondRequestedCategory));
        when(categoryRepository.findById(1)).thenReturn(java.util.Optional.of(firstRequestedCategory));
        when(categoryRepository.findById(2)).thenReturn(java.util.Optional.of(secondRequestedCategory));
        when(categoryRepository.findAllById(argThat(ids -> containsExactly(ids, List.of(3, 1, 2)))))
                .thenReturn(List.of(updatedFormerFeaturedCategory, updatedFirstCategory, updatedSecondCategory));
        when(redisCacheSupport.scanKeys(IRedisConstant.REDIS_CATEGORY.concat("*"))).thenReturn(cachedCategoryKeys);

        TransactionSynchronizationManager.initSynchronization();

        service.bulkUpdateFeaturedCategories(requestedCategoryIds);

        verify(redisCacheSupport, never()).scanKeys(anyString());
        verify(redisCacheSupport, never()).set(anyString(), any(), any());

        triggerAfterCommit();

        verify(redisCacheSupport).scanKeys(IRedisConstant.REDIS_CATEGORY.concat("*"));
        verify(redisCacheSupport).delete(cachedCategoryKeys);
        verify(redisCacheSupport).delete(IRedisConstant.REDIS_ALL_CATEGORY_CACHE);
        verify(redisCacheSupport, never()).set(eq(IRedisConstant.REDIS_ALL_CATEGORY_CACHE), any(), any());
        verify(redisCacheSupport, never()).set(eq(IRedisConstant.REDIS_CATEGORY_FEATURED), any(), any());
        verify(redisCacheSupport).set(eq(IRedisConstant.REDIS_CATEGORY_ID.concat("1")), any(), eq(IRedisTtlConstant.TTL_ENTITY));
        verify(redisCacheSupport).set(eq(IRedisConstant.REDIS_CATEGORY_ID.concat("2")), any(), eq(IRedisTtlConstant.TTL_ENTITY));
        verify(redisCacheSupport).set(eq(IRedisConstant.REDIS_CATEGORY_ID.concat("3")), any(), eq(IRedisTtlConstant.TTL_ENTITY));
        verify(categoryRepository).setAllCategoriesNotFeatured();
        verify(categoryRepository, times(1)).findByFeaturedTrueOrderByDisplayOrderAsc();
    }

    @Test
    void bulkUpdateFeaturedCategoriesWithEmptyListRefreshesCachesToEmptyFeaturedState() {
        CategoryServiceImpl service = new CategoryServiceImpl(categoryRepository, redisCacheSupport, objectMapper);
        Category oldFeaturedCategory = category(7, "Travel", false, 1);

        when(categoryRepository.findByFeaturedTrueOrderByDisplayOrderAsc())
                .thenReturn(List.of(oldFeaturedCategory));
        when(categoryRepository.findAllById(List.of())).thenReturn(List.of());
        when(categoryRepository.findAllById(argThat(ids -> containsExactly(ids, List.of(7)))))
                .thenReturn(List.of(oldFeaturedCategory));
        when(redisCacheSupport.scanKeys(IRedisConstant.REDIS_CATEGORY.concat("*"))).thenReturn(Set.of(IRedisConstant.REDIS_CATEGORY_FEATURED));

        TransactionSynchronizationManager.initSynchronization();

        assertDoesNotThrow(() -> service.bulkUpdateFeaturedCategories(List.of()));
        triggerAfterCommit();

        verify(categoryRepository).setAllCategoriesNotFeatured();
        verify(redisCacheSupport, never()).set(eq(IRedisConstant.REDIS_CATEGORY_FEATURED), any(), any());
        verify(redisCacheSupport).set(eq(IRedisConstant.REDIS_CATEGORY_ID.concat("7")), any(), eq(IRedisTtlConstant.TTL_ENTITY));
    }

    @Test
    void bulkUpdateFeaturedCategoriesWithMissingCategoryIdsDoesNotTouchCache() {
        CategoryServiceImpl service = new CategoryServiceImpl(categoryRepository, redisCacheSupport, objectMapper);
        List<Integer> requestedCategoryIds = List.of(1, 99);
        Category existingCategory = category(1, "Technology", false, 0);

        when(categoryRepository.findByFeaturedTrueOrderByDisplayOrderAsc()).thenReturn(List.of());
        when(categoryRepository.findAllById(requestedCategoryIds)).thenReturn(List.of(existingCategory));

        assertThrows(ResourceNotFoundException.class, () -> service.bulkUpdateFeaturedCategories(requestedCategoryIds));

        verify(categoryRepository, never()).setAllCategoriesNotFeatured();
        verify(redisCacheSupport, never()).get(anyString());
        verify(redisCacheSupport, never()).scanKeys(anyString());
    }

    @Test
    void createCategoryRefreshesCategoryCachesAfterCommit() {
        CategoryServiceImpl service = new CategoryServiceImpl(categoryRepository, redisCacheSupport, objectMapper);
        CategoryDto request = scienceCategoryDto();
        Category savedCategory = category(11, "Science", false, 0);
        Set<String> cachedCategoryKeys = Set.of(
                IRedisConstant.REDIS_CATEGORY_FEATURED,
                IRedisConstant.REDIS_CATEGORY_KEYWORD.concat("sci")
        );

        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);
        when(categoryRepository.findAllById(argThat(ids -> containsExactly(ids, List.of(11)))))
                .thenReturn(List.of(savedCategory));
        when(redisCacheSupport.scanKeys(IRedisConstant.REDIS_CATEGORY.concat("*"))).thenReturn(cachedCategoryKeys);

        TransactionSynchronizationManager.initSynchronization();

        service.createCategory(request);

        verify(redisCacheSupport, never()).scanKeys(anyString());
        triggerAfterCommit();

        verify(redisCacheSupport).delete(argThat((Set<String> keys) -> keys.equals(Set.of(IRedisConstant.REDIS_CATEGORY_KEYWORD.concat("sci")))));
        verify(redisCacheSupport).delete(IRedisConstant.REDIS_ALL_CATEGORY_CACHE);
        verify(redisCacheSupport, never()).set(eq(IRedisConstant.REDIS_ALL_CATEGORY_CACHE), any(), any());
        verify(redisCacheSupport).set(eq(IRedisConstant.REDIS_CATEGORY_ID.concat("11")), any(), eq(IRedisTtlConstant.TTL_ENTITY));
        verify(redisCacheSupport, never()).set(eq(IRedisConstant.REDIS_CATEGORY_FEATURED), any(), any());
    }

    @Test
    void deleteByCategoryIdEvictsCollectionAndDeletedIdCachesAfterCommit() {
        CategoryServiceImpl service = new CategoryServiceImpl(categoryRepository, redisCacheSupport, objectMapper);
        Category deletedCategory = category(8, "Travel", true, 1);
        Set<String> cachedCategoryKeys = Set.of(
                IRedisConstant.REDIS_CATEGORY_FEATURED,
                IRedisConstant.REDIS_CATEGORY.concat(":page:0:size:10")
        );

        when(categoryRepository.findById(8)).thenReturn(java.util.Optional.of(deletedCategory));
        when(redisCacheSupport.scanKeys(IRedisConstant.REDIS_CATEGORY.concat("*"))).thenReturn(cachedCategoryKeys);

        TransactionSynchronizationManager.initSynchronization();

        service.deleteByCategoryId(8);
        triggerAfterCommit();

        verify(categoryRepository).deleteById(8);
        verify(redisCacheSupport).delete(cachedCategoryKeys);
        verify(redisCacheSupport).delete(argThat((Set<String> keys) -> keys.equals(Set.of(IRedisConstant.REDIS_CATEGORY_ID.concat("8")))));
        verify(redisCacheSupport, never()).set(eq(IRedisConstant.REDIS_ALL_CATEGORY_CACHE), any(), any());
        verify(redisCacheSupport, never()).set(eq(IRedisConstant.REDIS_CATEGORY_FEATURED), any(), any());
        verify(redisCacheSupport, never()).set(eq(IRedisConstant.REDIS_CATEGORY_ID.concat("8")), any(), any());
    }

    @Test
    void updateCategoryFeaturedStatusEvictsCollectionAndRefreshesCategoryIdCacheAfterCommit() {
        CategoryServiceImpl service = new CategoryServiceImpl(categoryRepository, redisCacheSupport, objectMapper);
        Category category = category(5, "Fashion", false, 0);
        Category savedCategory = category(5, "Fashion", true, 2);
        Set<String> cachedCategoryKeys = Set.of(
                IRedisConstant.REDIS_CATEGORY_FEATURED,
                IRedisConstant.REDIS_CATEGORY_KEYWORD.concat("fashion")
        );

        when(categoryRepository.findById(5)).thenReturn(java.util.Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);
        when(categoryRepository.findAllById(argThat(ids -> containsExactly(ids, List.of(5)))))
                .thenReturn(List.of(savedCategory));
        when(redisCacheSupport.scanKeys(IRedisConstant.REDIS_CATEGORY.concat("*"))).thenReturn(cachedCategoryKeys);

        TransactionSynchronizationManager.initSynchronization();

        service.updateCategoryFeaturedStatus(5, new AdminCategoryUpdateDto(true, 2));
        triggerAfterCommit();

        verify(redisCacheSupport).delete(cachedCategoryKeys);
        verify(redisCacheSupport, never()).set(eq(IRedisConstant.REDIS_CATEGORY_FEATURED), any(), any());
        verify(redisCacheSupport).set(eq(IRedisConstant.REDIS_CATEGORY_ID.concat("5")), any(), eq(IRedisTtlConstant.TTL_ENTITY));
        verify(redisCacheSupport, never()).set(eq(IRedisConstant.REDIS_ALL_CATEGORY_CACHE), any(), any());
        verify(categoryRepository, times(2)).save(any(Category.class));
    }

    private void triggerAfterCommit() {
        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCommit();
        }
        TransactionSynchronizationManager.clearSynchronization();
    }

    private boolean containsExactly(Iterable<Integer> actualIds, List<Integer> expectedIds) {
        java.util.ArrayList<Integer> collectedIds = new java.util.ArrayList<>();
        actualIds.forEach(collectedIds::add);
        return collectedIds.equals(expectedIds);
    }

    private CategoryDto scienceCategoryDto() {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setCategoryName("Science");
        categoryDto.setDescription("Science category description");
        return categoryDto;
    }

    private Category category(int id, String name, boolean featured, int displayOrder) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setDescription(name + " description");
        category.setFeatured(featured);
        category.setDisplayOrder(displayOrder);
        category.setAutoSuggested(false);
        category.setPostCount(0);
        return category;
    }
}
