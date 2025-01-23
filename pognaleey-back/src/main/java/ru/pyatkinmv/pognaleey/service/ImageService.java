package ru.pyatkinmv.pognaleey.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.pyatkinmv.pognaleey.client.ImageSearchHttpClient;
import ru.pyatkinmv.pognaleey.dto.ImageDto;
import ru.pyatkinmv.pognaleey.exception.EntityNotFoundException;
import ru.pyatkinmv.pognaleey.mapper.TravelMapper;
import ru.pyatkinmv.pognaleey.model.Image;
import ru.pyatkinmv.pognaleey.repository.ImageRepository;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final ImageRepository imageRepository;
    private final ImageSearchHttpClient<?> imageSearchHttpClient;

    public Optional<ImageDto> searchImageAndSave(String title, String searchQuery) {
        return imageSearchHttpClient.searchImage(searchQuery)
                .map(it -> TravelMapper.toImage(it, title))
                .map(imageRepository::save)
                .map(TravelMapper::toImageDto);
    }

    public ImageDto findByIdOrThrow(Long imageId) {
        return imageRepository.findById(imageId).map(TravelMapper::toImageDto)
                .orElseThrow(() -> new EntityNotFoundException(imageId, Image.class));
    }

    public Map<Long, ImageDto> getIdToImageMap(Set<Long> imagesIds) {
        if (imagesIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return StreamSupport.stream(imageRepository.findAllById(imagesIds).spliterator(), false)
                .map(TravelMapper::toImageDto)
                .collect(Collectors.toMap(ImageDto::id, it -> it));
    }
}
