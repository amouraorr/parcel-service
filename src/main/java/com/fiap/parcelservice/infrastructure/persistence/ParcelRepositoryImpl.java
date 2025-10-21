package com.fiap.parcelservice.infrastructure.persistence;

import com.fiap.parcelservice.domain.model.Parcel;
import com.fiap.parcelservice.domain.repository.ParcelRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ParcelRepositoryImpl implements ParcelRepository {

    private final ParcelJpaRepository jpaRepository;

    public ParcelRepositoryImpl(ParcelJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Parcel save(Parcel parcel) {
        return jpaRepository.save(parcel);
    }

    @Override
    public Optional<Parcel> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Parcel> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<Parcel> findByStatus(String status) {
        return jpaRepository.findByStatus(status);
    }
}