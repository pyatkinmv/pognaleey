package ru.pyatkinmv.pognaleey.util.converter;

import java.io.InputStream;
import java.io.PipedOutputStream;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.coobird.thumbnailator.Thumbnails;

@RequiredArgsConstructor
class ThumbnailsConverter extends PipedResourceConverter<InputStream> {
  private static final String FILENAME_FORMAT = "%s-%d.%s";

  private final Integer width;
  private final String extension;
  private final Double quality;

  @SneakyThrows
  void doConvert(InputStream input, PipedOutputStream outputStream) {
    Thumbnails.of(input)
        .width(Objects.requireNonNull(width))
        .outputFormat(extension)
        .outputQuality(Objects.requireNonNull(quality))
        .toOutputStream(outputStream);
  }

  @Override
  public String buildResourceName(String base) {
    return String.format(FILENAME_FORMAT, base, width, extension);
  }
}
