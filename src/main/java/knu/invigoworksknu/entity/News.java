package knu.invigoworksknu.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class News {

    @Id
    private String id;

    @Column(nullable = false)
    private int score; // 감성 분석 점수

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static News of(String id, int score, LocalDateTime createdAt) {
        return News.builder()
                .id(id)
                .score(score)
                .createdAt(createdAt)
                .build();
    }
}
