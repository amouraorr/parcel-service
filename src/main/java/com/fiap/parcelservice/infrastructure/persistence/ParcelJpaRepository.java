package com.fiap.parcelservice.infrastructure.persistence;

import com.fiap.parcelservice.domain.model.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParcelJpaRepository extends JpaRepository<Parcel, Long> {

    List<Parcel> findByStatus(String status);
}