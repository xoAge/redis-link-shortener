package io.github.xoage.shortener.link;

import io.github.xoage.shortener.exception.DuplicateCodeException;
import io.github.xoage.shortener.exception.StorageException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Optional;

public class PostgresLinkRepository implements LinkRepository {

    private static final String UNIQUE_VIOLATION = "23505";

    private static final String INSERT = """
            INSERT INTO links (code, url, expires_at)
            VALUES (?, ?, now() + make_interval(secs => ?))
            """;
    private static final String SELECT_URL = """
            SELECT url FROM links WHERE code = ? AND expires_at > now()
            """;
    private static final String SELECT_TTL = """
            SELECT CEIL(EXTRACT(EPOCH FROM (expires_at - now())))::bigint
            FROM links WHERE code = ? AND expires_at > now()
            """;

    private final DataSource dataSource;

    public PostgresLinkRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(String code, String url, Duration ttl) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT)) {
            statement.setString(1, code);
            statement.setString(2, url);
            statement.setDouble(3, ttl.toSeconds());
            statement.executeUpdate();
        } catch (SQLException e) {
            if (UNIQUE_VIOLATION.equals(e.getSQLState())) {
                throw new DuplicateCodeException(code, e);
            }
            throw new StorageException("Не удалось сохранить ссылку в PostgreSQL", e);
        }
    }

    @Override
    public Optional<String> findUrl(String code) {
        return queryOne(SELECT_URL, code, rs -> rs.getString(1));
    }

    @Override
    public Optional<Duration> remainingTtl(String code) {
        return queryOne(SELECT_TTL, code, rs -> Duration.ofSeconds(rs.getLong(1)));
    }

    private <T> Optional<T> queryOne(String sql, String code, RowMapper<T> mapper) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, code);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapper.map(resultSet))
                        : Optional.empty();
            }
        } catch (SQLException e) {
            throw new StorageException("Не удалось прочитать ссылку из PostgreSQL", e);
        }
    }

    @FunctionalInterface
    private interface RowMapper<T> {
        T map(ResultSet resultSet) throws SQLException;
    }
}
