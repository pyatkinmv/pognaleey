package ru.pyatkinmv.where_to_go;


import java.sql.Timestamp;

public record FormData(Long id, Timestamp createdAt, Long userId, String content) {
}
