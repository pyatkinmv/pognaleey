package ru.pyatkinmv.pognaleey.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
@RequiredArgsConstructor
public class ResourceRepository {
    private static final String FIND_BY_ID = "SELECT name, data FROM resource WHERE id = ?";
    private static final String INSERT = "INSERT INTO resource (name, data) VALUES (?, ?)";
    private final JdbcTemplate jdbcTemplate;

    public Long save(String name, InputStream data) {
        return jdbcTemplate.execute((PreparedStatementCreator) connection -> {
            // Указываем, что нужно возвращать сгенерированный ключ (ID)
            PreparedStatement ps = connection.prepareStatement(INSERT, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setBinaryStream(2, data);
            return ps;
        }, preparedStatement -> {
            preparedStatement.executeUpdate();
            // Получаем сгенерированный ключ
            try (ResultSet keys = preparedStatement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1); // Возвращаем сгенерированный ID
                }
            }
            throw new RuntimeException("Failed to retrieve generated ID");
        });
    }

    public ResourceData findById(Long id) {
        return jdbcTemplate.queryForObject(FIND_BY_ID, ResourceDataRowMapper.RESOURCE_DATA_ROW_MAPPER, id);
    }

    private static class ResourceDataRowMapper implements RowMapper<ResourceData> {

        private static final RowMapper<ResourceData> RESOURCE_DATA_ROW_MAPPER = new ResourceDataRowMapper();

        @Nullable
        @Override
        public ResourceData mapRow(ResultSet rs, int rowNum) throws SQLException {
            String name = rs.getString("name");
            InputStream data = rs.getBinaryStream("data");

            return new ResourceData(name, data);
        }
    }

    public record ResourceData(String name, InputStream data) {
    }
}
