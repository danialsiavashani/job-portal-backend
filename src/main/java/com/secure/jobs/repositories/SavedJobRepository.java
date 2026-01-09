package com.secure.jobs.repositories;


import com.secure.jobs.models.job.Job;
import com.secure.jobs.models.job.SavedJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

public interface SavedJobRepository extends JpaRepository<SavedJob, Long>, JpaSpecificationExecutor<SavedJob> {

    boolean existsByUser_UserIdAndJob_Id(Long userId, Long jobId);

    Optional<SavedJob>  findByUser_UserIdAndJob_Id(Long userId, Long jobId);

    void deleteByUser_UserIdAndJob_Id(Long userId, Long jobId);

    @EntityGraph(attributePaths = {
            "user",
            "job"
    })
    Page<SavedJob> findAll(Pageable pageable);

    @Query("select sj.job.id from SavedJob sj where sj.user.userId = :userId")
    List<Long> findSavedJobIdsByUserId(@Param("userId") Long userId);

}
