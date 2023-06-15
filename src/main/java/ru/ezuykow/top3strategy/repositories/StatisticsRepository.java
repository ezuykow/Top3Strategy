package ru.ezuykow.top3strategy.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ezuykow.top3strategy.entities.Statistics;

/**
 * @author ezuykow
 */
@Repository
public interface StatisticsRepository extends JpaRepository<Statistics, Integer> {
}
