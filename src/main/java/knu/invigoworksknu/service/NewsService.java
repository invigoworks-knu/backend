package knu.invigoworksknu.service;

import knu.invigoworksknu.dto.NewsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsService {

    public final JdbcTemplate jdbcTemplate;

    @Async
    @Transactional
    public void saveNewsListInBatch(List<NewsDto> newsList, LocalDateTime createdAt) {
        String sql = """
                INSERT INTO news (id, score, created_at) VALUES (?, ?, ?)
                """;

        jdbcTemplate.batchUpdate(sql,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        NewsDto news = newsList.get(i);
                        ps.setString(1, news.getId());
                        ps.setInt(2, news.getScore());
                        ps.setObject(3, createdAt);
                    }

                    @Override
                    public int getBatchSize() {
                        return newsList.size();
                    }
                });
    }
}
