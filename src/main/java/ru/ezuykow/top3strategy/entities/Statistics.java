package ru.ezuykow.top3strategy.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ezuykow
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Statistics {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "bets_count")
    private int betsCount;

    @Column(name = "looses")
    private int looses;

    @Column(name = "bank_status")
    private int bankStatus;
}
