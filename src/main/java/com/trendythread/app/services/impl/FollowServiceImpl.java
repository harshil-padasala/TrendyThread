package com.trendythread.app.services.impl;

import com.trendythread.app.dto.FollowStatusDto;
import com.trendythread.app.entities.Blogger;
import com.trendythread.app.entities.Follow;
import com.trendythread.app.exceptions.BlogAPIException;
import com.trendythread.app.exceptions.ResourceNotFoundException;
import com.trendythread.app.repositories.BloggersRepository;
import com.trendythread.app.repositories.FollowRepository;
import com.trendythread.app.services.FollowService;
import com.trendythread.app.services.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class FollowServiceImpl implements FollowService {

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private BloggersRepository bloggersRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public FollowStatusDto toggleFollow(Integer targetBloggerId, String followerEmail) {
        log.info("toggleFollow - request received: targetBloggerId={}, follower={}", targetBloggerId, followerEmail);

        Blogger target = bloggersRepository.findById(targetBloggerId)
                .orElseThrow(() -> new ResourceNotFoundException("Blogger", "Blogger Id", targetBloggerId));

        if (target.getEmail().equalsIgnoreCase(followerEmail)) {
            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "You cannot follow yourself");
        }

        Optional<Follow> existing = followRepository.findByFollowerEmailAndFollowingId(followerEmail, targetBloggerId);

        boolean nowFollowing;
        if (existing.isPresent()) {
            followRepository.delete(existing.get());
            nowFollowing = false;
            log.info("toggleFollow - {} unfollowed bloggerId={}", followerEmail, targetBloggerId);
        } else {
            Blogger follower = bloggersRepository.findByEmail(followerEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("Blogger", "email", followerEmail));

            Follow follow = new Follow();
            follow.setFollower(follower);
            follow.setFollowing(target);
            followRepository.save(follow);
            nowFollowing = true;

            notificationService.createNotification(
                    target.getId(),
                    "FOLLOW",
                    follower.getUserName() + " started following you",
                    null
            );
            log.info("toggleFollow - {} followed bloggerId={}", followerEmail, targetBloggerId);
        }

        long followerCount = followRepository.countByFollowingId(targetBloggerId);
        return new FollowStatusDto(nowFollowing, followerCount);
    }

    @Override
    public FollowStatusDto getFollowStatus(Integer targetBloggerId, String viewerEmailOrNull) {
        if (!bloggersRepository.existsById(targetBloggerId)) {
            throw new ResourceNotFoundException("Blogger", "Blogger Id", targetBloggerId);
        }

        boolean following = viewerEmailOrNull != null
                && followRepository.existsByFollowerEmailAndFollowingId(viewerEmailOrNull, targetBloggerId);
        long followerCount = followRepository.countByFollowingId(targetBloggerId);

        return new FollowStatusDto(following, followerCount);
    }

    @Override
    public List<Integer> getFollowingIds(String email) {
        return followRepository.findFollowingIdsByFollowerEmail(email);
    }
}
