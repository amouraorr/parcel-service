package com.fiap.parcelservice.domain.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "parcels")
public class Parcel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="resident_name", nullable = false)
    private String residentName;

    @Column(name="apartment", nullable = false)
    private String apartment;

    @Column(name="description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable = false)
    private ParcelStatus status;

    @Column(name="created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name="updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @Column(name = "contact", length = 255)
    private String contact;

    @Column(name = "channel", length = 20)
    private String channel;

    public Parcel() {
    }

    public Parcel(String residentName, String apartment, String description, ParcelStatus status) {
        this.residentName = residentName;
        this.apartment = apartment;
        this.description = description;
        this.status = status;
    }

    @PrePersist
    public void prePersist() {
        createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getResidentName() {
        return residentName;
    }

    public void setResidentName(String residentName) {
        this.residentName = residentName;
    }

    public String getApartment() {
        return apartment;
    }

    public void setApartment(String apartment) {
        this.apartment = apartment;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ParcelStatus getStatus() {
        return status;
    }

    public void setStatus(ParcelStatus status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getContact() { return contact; }

    public void setContact(String contact) { this.contact = contact; }

    public String getChannel() { return channel; }

    public void setChannel(String channel) { this.channel = channel; }
}