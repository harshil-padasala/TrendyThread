package com.trendythread.app.repositories;

import com.trendythread.app.entities.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Integer> {

    Optional<Follow> findByFollowerEmailAndFollowingId(String followerEmail, Integer followingId);

    boolean existsByFollowerEmailAndFollowingId(String followerEmail, Integer followingId);

    long countByFollowingId(Integer followingId);

    long countByFollowerId(Integer followerId);

    @Transactional
    void deleteByFollowerEmailAndFollowingId(String followerEmail, Integer followingId);

    @Query("select f.following.id from Follow f where f.follower.email = :email")
    List<Integer> findFollowingIdsByFollowerEmail(@Param("email") String email);
}
