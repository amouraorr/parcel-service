package com.fiap.parcelservice.domain.repository;

import com.fiap.parcelservice.domain.model.Parcel;

import java.util.List;
import java.util.Optional;

public interface ParcelRepository {

    Parcel save(Parcel parcel);
    Optional<Parcel> findById(Long id);
    List<Parcel> findAll();
    List<Parcel> findByStatus(String status);
}