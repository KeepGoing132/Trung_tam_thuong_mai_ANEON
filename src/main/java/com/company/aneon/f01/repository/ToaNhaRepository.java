package com.company.aneon.f01.repository;

import java.util.UUID;

import com.company.aneon.f01.entity.ToaNha;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ToaNhaRepository extends JpaRepository<ToaNha, UUID> {
}
