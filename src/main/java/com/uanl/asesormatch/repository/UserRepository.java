package com.uanl.asesormatch.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uanl.asesormatch.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);

	Optional<User> findByUniversityId(String universityId);
}