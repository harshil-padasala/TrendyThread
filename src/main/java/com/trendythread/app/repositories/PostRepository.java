package com.trendythread.app.repositories;

import com.trendythread.app.entities.Category;
import com.trendythread.app.entities.Post;
import com.trendythread.app.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {

    Page<Post> findByCategory(Category category,
                              Pageable pageable);

    Page<Post> findByUser(User user,
                          Pageable pageable);

    Page<Post> findByTitleContaining(String title,
                                     Pageable pageable);
}
