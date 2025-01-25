package ru.pyatkinmv.pognaleey.dto;

import org.springframework.lang.Nullable;

import java.util.List;

public record FlickrImagesResponseDto(Photos photos) {
    public record Photos(List<Photo> photo) {

    }

    public record Photo(Long id, String ownername, String title, int licence,
                        @Nullable String url_m,
                        @Nullable Integer height_m,
                        @Nullable Integer width_m,
                        @Nullable String url_l,
                        @Nullable Integer height_l,
                        @Nullable Integer width_l) {

    }
}