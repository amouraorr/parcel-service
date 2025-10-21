package com.fiap.parcelservice.application.mapper;

import com.fiap.parcelservice.application.dto.ParcelRequest;
import com.fiap.parcelservice.application.dto.ParcelResponse;
import com.fiap.parcelservice.domain.model.Parcel;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ParcelMapper {

    ParcelMapper INSTANCE = Mappers.getMapper(ParcelMapper.class);

    Parcel toEntity(ParcelRequest request);

    ParcelResponse toResponse(Parcel parcel);
}