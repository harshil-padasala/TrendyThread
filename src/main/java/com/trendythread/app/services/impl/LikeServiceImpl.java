package com.trendythread.app.services.impl;

import com.trendythread.app.dto.LikeStatusDto;
import com.trendythread.app.entities.Blogger;
import com.trendythread.app.entities.Post;
import com.trendythread.app.entities.PostLike;
import com.trendythread.app.exceptions.ResourceNotFoundException;
import com.trendythread.app.repositories.BloggersRepository;
import com.trendythread.app.repositories.PostLikeRepository;
import com.trendythread.app.repositories.PostRepository;
import com.trendythread.app.services.LikeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class LikeServiceImpl implements LikeService {

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private BloggersRepository bloggersRepository;

    @Override
    @Transactional
    public LikeStatusDto toggleLike(Integer postId, String authenticatedUserEmail) {
        log.info("toggleLike - request received: postId={}, user={}", postId, authenticatedUserEmail);

        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post", "Post Id", postId);
        }

        boolean alreadyLiked = postLikeRepository.existsByPostIdAndBloggerEmail(postId, authenticatedUserEmail);

        if (alreadyLiked) {
            postLikeRepository.deleteByPostIdAndBloggerEmail(postId, authenticatedUserEmail);
            log.info("toggleLike - unliked postId={} by user={}", postId, authenticatedUserEmail);
        } else {
            Post post = postRepository.getReferenceById(postId);
            Blogger blogger = bloggersRepository.findByEmail(authenticatedUserEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("Blogger", "email", authenticatedUserEmail));

            PostLike like = new PostLike();
            like.setPost(post);
            like.setBlogger(blogger);
            postLikeRepository.save(like);
            log.info("toggleLike - liked postId={} by user={}", postId, authenticatedUserEmail);
        }

        long likeCount = postLikeRepository.countByPostId(postId);
        return new LikeStatusDto(!alreadyLiked, likeCount);
    }

    @Override
    public LikeStatusDto getLikeStatus(Integer postId, String authenticatedUserEmailOrNull) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post", "Post Id", postId);
        }

        boolean liked = authenticatedUserEmailOrNull != null
                && postLikeRepository.existsByPostIdAndBloggerEmail(postId, authenticatedUserEmailOrNull);
        long likeCount = postLikeRepository.countByPostId(postId);

        return new LikeStatusDto(liked, likeCount);
    }
}
