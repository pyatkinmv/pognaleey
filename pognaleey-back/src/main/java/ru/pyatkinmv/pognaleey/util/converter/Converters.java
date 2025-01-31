package ru.pyatkinmv.pognaleey.util.converter;

import static ru.pyatkinmv.pognaleey.util.converter.Base64Converter.JPG;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Converters {
  JPG_1024(new ThumbnailsConverter(1024, JPG, 1.0)),
  JPG_512(new ThumbnailsConverter(512, JPG, 1.0)),
  AS_IS(new AsIsConverter()),
  BASE64(new Base64Converter());

  private final ResourceConverter<?> converter;

  public <T> ResourceConverter<T> get() {
    //noinspection unchecked
    return (ResourceConverter<T>) converter;
  }
}
