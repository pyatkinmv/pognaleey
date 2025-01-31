package ru.pyatkinmv.pognaleey.dto;

import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

public record AdminUploadImageDto(
    MultipartFile file,
    Long guideId,
    boolean aiGenerated,
    boolean keepOriginal,
    @Nullable String authorName,
    @Nullable String authorUrl) {}
