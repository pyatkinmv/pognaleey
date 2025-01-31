package ru.pyatkinmv.pognaleey.service;

import static ru.pyatkinmv.pognaleey.mapper.TravelMapper.toImage;
import static ru.pyatkinmv.pognaleey.mapper.TravelMapper.toImageDto;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.pyatkinmv.pognaleey.client.ImageSearchHttpClient;
import ru.pyatkinmv.pognaleey.dto.ImageDto;
import ru.pyatkinmv.pognaleey.exception.EntityNotFoundException;
import ru.pyatkinmv.pognaleey.mapper.TravelMapper;
import ru.pyatkinmv.pognaleey.model.Image;
import ru.pyatkinmv.pognaleey.repository.ImageRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {
  private final ImageRepository imageRepository;
  private final ImageSearchHttpClient<?> imageSearchHttpClient;

  public Optional<ImageDto> searchImage(String title, String searchQuery) {
    return imageSearchHttpClient
        .searchImage(searchQuery)
        .map(it -> toImage(it, title))
        .map(TravelMapper::toImageDto);
  }

  public ImageDto saveImage(ImageDto imageDto) {
    Image saved = imageRepository.save(toImage(imageDto));
    log.info("Saved image {}", saved.getId());

    return toImageDto(saved);
  }

  public List<ImageDto> saveAll(List<ImageDto> dtos) {
    var images = dtos.stream().map(TravelMapper::toImage).toList();
    return imageRepository.saveAllFromIterable(images).stream()
        .map(TravelMapper::toImageDto)
        .toList();
  }

  public ImageDto findByIdOrThrow(Long imageId) {
    return imageRepository
        .findById(imageId)
        .map(TravelMapper::toImageDto)
        .orElseThrow(() -> new EntityNotFoundException(imageId, Image.class));
  }

  public List<ImageDto> findAll(Collection<Long> imageIds) {
    return imageRepository.findAllByIdIn(imageIds).stream().map(TravelMapper::toImageDto).toList();
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
