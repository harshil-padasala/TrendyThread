package com.trendythread.app.services;

import com.trendythread.app.entities.Tag;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public interface TagService {

    /**
     * Resolve a set of free-form tag names to persisted Tag entities,
     * creating any that don't already exist yet (case-insensitive match).
     */
    Set<Tag> resolveOrCreateTags(Set<String> tagNames);

    /**
     * All tag names currently in use, sorted alphabetically.
     */
    List<String> findAllTagNames();
}
