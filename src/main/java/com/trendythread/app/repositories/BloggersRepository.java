package com.trendythread.app.repositories;

import com.trendythread.app.entities.Blogger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BloggersRepository extends JpaRepository<Blogger, Integer> {

    Blogger findByUserName(String name);

    Optional<Blogger> findByEmail(String email);
}
