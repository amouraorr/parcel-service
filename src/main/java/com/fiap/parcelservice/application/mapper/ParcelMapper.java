package com.fiap.parcelservice.application.mapper;

import com.fiap.parcelservice.application.dto.ParcelRequest;
import com.fiap.parcelservice.application.dto.ParcelResponse;
import com.fiap.parcelservice.domain.model.Parcel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ParcelMapper {

    ParcelMapper INSTANCE = Mappers.getMapper(ParcelMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "notified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Parcel toEntity(ParcelRequest request);

    ParcelResponse toResponse(Parcel parcel);
}