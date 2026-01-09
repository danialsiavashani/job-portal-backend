CREATE TABLE saved_jobs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    CONSTRAINT fk_saved_jobs_user
        FOREIGN KEY (user_id) REFERENCES users(user_id),

    CONSTRAINT fk_saved_jobs_job
        FOREIGN KEY (job_id) REFERENCES jobs(id),

    CONSTRAINT uk_saved_job_user_job
        UNIQUE (user_id, job_id)
);

CREATE INDEX idx_saved_jobs_user_created_at ON saved_jobs(user_id, created_at);
CREATE INDEX idx_saved_jobs_job ON saved_jobs(job_id);
