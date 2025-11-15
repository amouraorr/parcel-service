package com.fiap.parcelservice.application.dto;

import com.fiap.parcelservice.domain.model.ParcelStatus;

import java.time.OffsetDateTime;

public class ParcelResponse {
    private Long id;
    private String residentName;
    private String apartment;
    private String description;
    private ParcelStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String contact;
    private String channel;

    public ParcelResponse() {
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

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getContact() { return contact; }

    public void setContact(String contact) { this.contact = contact; }

    public String getChannel() { return channel; }

    public void setChannel(String channel) { this.channel = channel; }
}