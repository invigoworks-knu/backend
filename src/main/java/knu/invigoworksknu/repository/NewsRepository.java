package knu.invigoworksknu.repository;

import knu.invigoworksknu.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<News, String> {
}
