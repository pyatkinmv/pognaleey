package ru.pyatkinmv.pognaleey.service;

import ru.pyatkinmv.pognaleey.model.Language;

import java.util.Locale;

public class LanguageContextHolder {

    private static final ThreadLocal<Language> localeThreadLocal = new InheritableThreadLocal<>();

    public static void setLanguage(Language language) {
        localeThreadLocal.set(language);
    }

    public static Language getLanguage() {
        return localeThreadLocal.get();
    }

    public static Locale getLanguageLocale() {
        return Locale.forLanguageTag(getLanguage().name());
    }

    public static void clear() {
        localeThreadLocal.remove();
    }

}