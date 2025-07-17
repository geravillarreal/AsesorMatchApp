package com.uanl.asesormatch.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.uanl.asesormatch.entity.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    @Query("SELECT DISTINCT i FROM Profile p JOIN p.interests i")
    List<String> findDistinctInterests();

    @Query("SELECT DISTINCT a FROM Profile p JOIN p.areas a")
    List<String> findDistinctAreas();

    @Query("SELECT DISTINCT av FROM Profile p JOIN p.availability av")
    List<String> findDistinctAvailability();

    @Query("SELECT DISTINCT p.level FROM Profile p WHERE p.level IS NOT NULL")
    List<String> findDistinctLevels();

    @Query("SELECT DISTINCT p.modality FROM Profile p WHERE p.modality IS NOT NULL")
    List<String> findDistinctModalities();

    @Query("SELECT DISTINCT p.language FROM Profile p WHERE p.language IS NOT NULL")
    List<String> findDistinctLanguages();
}
