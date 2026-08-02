package com.trendythread.app.services.impl;

import com.trendythread.app.entities.Tag;
import com.trendythread.app.repositories.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagServiceImpl tagService;

    @Test
    void resolveOrCreateTagsReusesExistingTagCaseInsensitively() {
        Tag existing = new Tag();
        existing.setId(1);
        existing.setName("cloud");
        when(tagRepository.findByNameIgnoreCase("cloud")).thenReturn(Optional.of(existing));

        Set<Tag> result = tagService.resolveOrCreateTags(Set.of("Cloud"));

        assertEquals(1, result.size());
        assertTrue(result.contains(existing));
        verify(tagRepository, never()).save(any());
    }

    @Test
    void resolveOrCreateTagsCreatesNewTagWhenMissing() {
        when(tagRepository.findByNameIgnoreCase("newtag")).thenReturn(Optional.empty());
        Tag saved = new Tag();
        saved.setId(2);
        saved.setName("newtag");
        when(tagRepository.save(any(Tag.class))).thenReturn(saved);

        Set<Tag> result = tagService.resolveOrCreateTags(Set.of("newtag"));

        assertEquals(1, result.size());
        ArgumentCaptor<Tag> captor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository).save(captor.capture());
        assertEquals("newtag", captor.getValue().getName());
    }

    @Test
    void resolveOrCreateTagsDedupesCaseVariantsToOneTag() {
        Tag existing = new Tag();
        existing.setId(1);
        existing.setName("aws");
        when(tagRepository.findByNameIgnoreCase("aws")).thenReturn(Optional.of(existing));

        Set<Tag> result = tagService.resolveOrCreateTags(Set.of("AWS", "aws", " aws "));

        assertEquals(1, result.size());
    }

    @Test
    void resolveOrCreateTagsSkipsBlankAndNullInput() {
        Set<Tag> result = tagService.resolveOrCreateTags(null);
        assertTrue(result.isEmpty());

        Set<Tag> resultWithBlanks = tagService.resolveOrCreateTags(new java.util.HashSet<>(java.util.Arrays.asList("", "   ")));
        assertTrue(resultWithBlanks.isEmpty());
    }

    @Test
    void findAllTagNamesReturnsSortedNames() {
        Tag tagB = new Tag();
        tagB.setName("bravo");
        Tag tagA = new Tag();
        tagA.setName("alpha");
        when(tagRepository.findAll()).thenReturn(List.of(tagB, tagA));

        List<String> result = tagService.findAllTagNames();

        assertEquals(List.of("alpha", "bravo"), result);
    }
}
