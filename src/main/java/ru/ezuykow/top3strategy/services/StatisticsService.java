package ru.ezuykow.top3strategy.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ezuykow.top3strategy.entities.Statistics;
import ru.ezuykow.top3strategy.repositories.StatisticsRepository;

import java.util.List;

/**
 * @author ezuykow
 */
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StatisticsRepository repository;


    //-----------------API START-----------------

    public List<Statistics> findAll() {
        return repository.findAll();
    }

    public void save(Statistics statistics) {
        repository.save(statistics);
    }

    //-----------------API END-----------------

}
