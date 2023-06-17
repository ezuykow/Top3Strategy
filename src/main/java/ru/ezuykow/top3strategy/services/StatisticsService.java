package ru.ezuykow.top3strategy.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ezuykow.top3strategy.entities.Statistics;
import ru.ezuykow.top3strategy.repositories.StatisticsRepository;

/**
 * @author ezuykow
 */
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StatisticsRepository repository;


    //-----------------API START-----------------

    public Statistics getStatistics() {
        return repository.findAll().get(0);
    }

    public void save(Statistics statistics) {
        repository.save(statistics);
    }

    public String createStatsMsg() {
        final Statistics stats = getStatistics();
        return "Bets count: " + stats.getBetsCount() + "\n" +
                "Looses count: " + stats.getLooses() + "\n" +
                "Bank status: " + stats.getBankStatus() / 1000 + "." +
                stats.getBankStatus() % 1000 + " x nominal\n";
    }

    //-----------------API END-----------------

}
