package com.trendythread.app.services.impl;

import com.trendythread.app.entities.Tag;
import com.trendythread.app.repositories.TagRepository;
import com.trendythread.app.services.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class TagServiceImpl implements TagService {

    @Autowired
    private TagRepository tagRepository;

    @Override
    @Transactional
    public Set<Tag> resolveOrCreateTags(Set<String> tagNames) {
        Set<Tag> resolved = new LinkedHashSet<>();
        if (tagNames == null) {
            return resolved;
        }

        for (String rawName : tagNames) {
            if (rawName == null || rawName.isBlank()) {
                continue;
            }
            String normalized = rawName.trim().toLowerCase();

            Tag tag = tagRepository.findByNameIgnoreCase(normalized)
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(normalized);
                        return tagRepository.save(newTag);
                    });
            resolved.add(tag);
        }

        return resolved;
    }

    @Override
    public List<String> findAllTagNames() {
        return tagRepository.findAll().stream()
                .map(Tag::getName)
                .sorted()
                .toList();
    }
}
