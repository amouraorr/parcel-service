package com.fiap.parcelservice.application.service;

import com.fiap.parcelservice.application.dto.ParcelRequest;
import com.fiap.parcelservice.application.dto.ParcelResponse;

import java.util.List;

public interface ParcelService {

    ParcelResponse receiveParcel(ParcelRequest request);
    ParcelResponse getParcel(Long id);
    List<ParcelResponse> listParcels();
    ParcelResponse markPickedUp(Long id, String pickedBy);
}