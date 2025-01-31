package ru.pyatkinmv.pognaleey.util.converter;

import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import org.springframework.lang.Nullable;

class AsIsConverter implements ResourceConverter<InputStream> {

  @Override
  public InputStream convert(InputStream inputStream) {
    return inputStream;
  }

  @Override
  public String buildResourceName(@Nullable String originalFileName) {
    return Optional.ofNullable(originalFileName).orElseGet(() -> UUID.randomUUID().toString());
  }
}
