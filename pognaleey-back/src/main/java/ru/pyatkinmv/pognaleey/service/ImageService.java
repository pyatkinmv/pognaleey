package ru.pyatkinmv.pognaleey.service;

import static ru.pyatkinmv.pognaleey.mapper.TravelMapper.toImage;
import static ru.pyatkinmv.pognaleey.mapper.TravelMapper.toImageDto;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.pyatkinmv.pognaleey.client.ImageSearchHttpClient;
import ru.pyatkinmv.pognaleey.client.KandinskyImageGenerateHttpClient;
import ru.pyatkinmv.pognaleey.dto.ImageDto;
import ru.pyatkinmv.pognaleey.exception.EntityNotFoundException;
import ru.pyatkinmv.pognaleey.mapper.TravelMapper;
import ru.pyatkinmv.pognaleey.model.Image;
import ru.pyatkinmv.pognaleey.repository.ImageRepository;
import ru.pyatkinmv.pognaleey.repository.ResourceRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {
  private final ImageRepository imageRepository;
  private final ResourceRepository resourceRepository;
  private final ImageSearchHttpClient<?> imageSearchHttpClient;
  private final TaskSchedulerService taskSchedulerService;
  private final KandinskyImageGenerateHttpClient imageGenerateHttpClient;
  private final ExecutorService executorService;

  @Value("${app.domain-name}")
  private String domainName;

  @Value("${server.servlet.context-path}")
  private String contextPath;

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

  //   @SneakyThrows
  //    public static void main(String[] args) {
  //        var filename = "base64";
  //        var ext = "png";
  //        var outputExt = "png";
  //        var width = 1024;
  //        var q = 1.0;
  //        String path = String.format("%s%s", "C:\\Users\\maxim\\IdeaProjects\\pognaleey\\",
  // filename);
  //        String output = String.format("%s%s-w%d-q%f.%s",
  // "C:\\Users\\maxim\\IdeaProjects\\pognaleey\\", filename, width, q, outputExt);
  //
  //        StringBuilder base64Builder = new StringBuilder();
  //        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
  //            String line;
  //            while ((line = reader.readLine()) != null) {
  //                base64Builder.append(line);
  //            }
  //        }
  //
  //        // Декодируем содержимое файла (Base64 строку) в массив байтов
  //        byte[] imageBytes = Base64.getDecoder().decode(base64Builder.toString());
  //
  //        // Преобразуем байты в BufferedImage
  //        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes)) {
  //            BufferedImage image = ImageIO.read(bis);
  //
  //            // Сохраняем изображение в указанный формат (JPG или PNG)
  //            ImageIO.write(image, outputExt, new File(output));
  //        }
  //
  //
  ////        Thumbnails.of(new FileInputStream(path))
  ////                .width(width)
  ////                .outputFormat(outputExt)
  ////                .outputQuality(q)
  ////                .toOutputStream(new FileOutputStream(output));
  //    }
}
