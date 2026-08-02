package com.trendythread.app.repositories;

import com.trendythread.app.entities.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Integer> {

    Optional<PostLike> findByPostIdAndBloggerEmail(Integer postId, String email);

    boolean existsByPostIdAndBloggerEmail(Integer postId, String email);

    long countByPostId(Integer postId);

    void deleteByPostIdAndBloggerEmail(Integer postId, String email);
}
