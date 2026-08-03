package com.example.campus_hub.repository;

import com.example.campus_hub.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchRepository
        extends JpaRepository<Batch, String> {}