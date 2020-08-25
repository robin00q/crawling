package me.sjlee.crawling.repository;

import me.sjlee.crawling.domain.SongChart;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

public interface SongChartRepository extends CrudRepository<SongChart, String> {
}
