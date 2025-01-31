package ru.pyatkinmv.pognaleey.model;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum Language {
  RU,
  EN;

  public static Optional<Language> from(String language) {
    try {
      return Optional.of(valueOf(language.toUpperCase()));
    } catch (IllegalArgumentException e) {
      log.warn("Couldn't parse language from {}", language);
      return Optional.empty();
    }
  }

  public static Language getDefault() {
    return RU;
  }
}
