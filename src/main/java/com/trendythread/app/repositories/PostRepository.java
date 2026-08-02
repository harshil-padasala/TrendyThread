package com.trendythread.app.repositories;

import com.trendythread.app.entities.Blogger;
import com.trendythread.app.entities.Category;
import com.trendythread.app.entities.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {

    Page<Post> findByCategory(Category category, Pageable pageable);

    Page<Post> findByBlogger(Blogger blogger, Pageable pageable);

    Page<Post> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description, Pageable pageable);

    Page<Post> findByTags_NameIgnoreCase(String tagName, Pageable pageable);

    Page<Post> findByBlogger_IdIn(List<Integer> bloggerIds, Pageable pageable);
}
