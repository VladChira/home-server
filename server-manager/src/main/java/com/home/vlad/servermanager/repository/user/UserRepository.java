package com.home.vlad.servermanager.repository.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.home.vlad.servermanager.model.user.AppUser;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String name);
}

