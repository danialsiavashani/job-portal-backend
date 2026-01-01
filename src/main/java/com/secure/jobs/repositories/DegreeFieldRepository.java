package com.secure.jobs.repositories;

import com.secure.jobs.models.user.profile.DegreeField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DegreeFieldRepository extends JpaRepository<DegreeField, Long> {
    List<DegreeField> findByActiveTrueOrderByNameAsc();
    boolean existsByNameIgnoreCase(String name);
}