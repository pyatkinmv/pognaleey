package ru.pyatkinmv.pognaleey.service;

import java.util.Locale;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import ru.pyatkinmv.pognaleey.model.Language;

@Slf4j
public class LanguageContextHolder {

  private static final ThreadLocal<Language> localeThreadLocal = new InheritableThreadLocal<>();

  public static void setLanguage(Language language) {
    localeThreadLocal.set(language);
  }

  public static Language getLanguageOrDefault() {
    return Optional.ofNullable(localeThreadLocal.get()).orElseGet(Language::getDefault);
  }

  public static Locale getLanguageLocaleOrDefault() {
    return Locale.forLanguageTag(getLanguageOrDefault().name());
  }

  public static void clear() {
    localeThreadLocal.remove();
  }
}
