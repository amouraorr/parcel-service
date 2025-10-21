package com.fiap.parcelservice.domain.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "parcels")
public class Parcel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="recipient_name", nullable = false)
    private String recipientName;

    @Column(name="apartment", nullable = false)
    private String apartment;

    @Column(name="description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable = false)
    private ParcelStatus status;

    @Column(name="notified", nullable = false)
    private boolean notified = false;

    @Column(name="created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name="updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public Parcel() {
    }

    public Parcel(String recipientName, String apartment, String description, ParcelStatus status) {
        this.recipientName = recipientName;
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

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
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

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}