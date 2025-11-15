package com.fiap.parcelservice.application.service.impl;

import com.fiap.parcelservice.application.dto.ParcelRequest;
import com.fiap.parcelservice.application.dto.ParcelResponse;
import com.fiap.parcelservice.application.mapper.ParcelMapper;
import com.fiap.parcelservice.application.service.ParcelService;
import com.fiap.parcelservice.domain.model.Parcel;
import com.fiap.parcelservice.domain.model.ParcelStatus;
import com.fiap.parcelservice.domain.repository.ParcelRepository;
import com.fiap.parcelservice.infrastructure.messaging.ParcelKafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParcelServiceImpl implements ParcelService {

    private static final Logger log = LoggerFactory.getLogger(ParcelServiceImpl.class);

    private final ParcelRepository parcelRepository;
    private final ParcelMapper parcelMapper;
    private final ParcelKafkaProducer kafkaProducer;

    public ParcelServiceImpl(ParcelRepository parcelRepository, ParcelMapper parcelMapper, ParcelKafkaProducer kafkaProducer) {
        this.parcelRepository = parcelRepository;
        this.parcelMapper = parcelMapper;
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public ParcelResponse receiveParcel(ParcelRequest request) {

        Parcel parcel = parcelMapper.toEntity(request);

        parcel.setStatus(ParcelStatus.RECEIVED);

        Parcel saved = parcelRepository.save(parcel);
        log.info("Parcel received and saved with id={}", saved.getId());

        kafkaProducer.sendParcelReceivedEvent(saved);

        return parcelMapper.toResponse(saved);
    }

    @Override
    public ParcelResponse getParcel(Long id) {
        return parcelRepository.findById(id)
                .map(parcelMapper::toResponse)
                .orElse(null);
    }

    @Override
    public List<ParcelResponse> listParcels() {
        return parcelRepository.findAll().stream().map(parcelMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public ParcelResponse markPickedUp(Long id, String pickedBy) {
        Parcel parcel = parcelRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Parcel not found"));
        parcel.setStatus(ParcelStatus.PICKED_UP);

        Parcel saved = parcelRepository.save(parcel);

        kafkaProducer.sendParcelPickedUpEvent(saved, pickedBy);
        return parcelMapper.toResponse(saved);
    }
}