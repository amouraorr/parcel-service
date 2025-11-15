package com.fiap.parcelservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ParcelRequest {

    @NotBlank
    @Size(max = 255)
    private String residentName;

    @NotBlank
    @Size(max = 50)
    private String apartment;

    private String description;

    @Size(max = 255)
    private String contact;

    @Size(max = 20)
    private String channel;

    public ParcelRequest() {
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

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}