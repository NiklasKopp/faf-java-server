package com.faforever.server.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "game_player_stats")
@Immutable
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Cacheable
public class GamePlayerStats {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @ManyToOne
  @JoinColumn(name = "playerId")
  private Player player;

  @Column(name = "AI")
  private boolean ai;

  @Column(name = "faction")
  private int faction;

  @Column(name = "color")
  private int color;

  @Column(name = "team")
  private int team;

  @Column(name = "place")
  private int startSpot;

  @Column(name = "mean")
  private double mean;

  @Column(name = "deviation")
  private double deviation;

  @Column(name = "after_mean")
  private Double afterMean;

  @Column(name = "after_deviation")
  private Double afterDeviation;

  @Column(name = "score")
  private Integer score;

  @Column(name = "scoreTime")
  private Instant scoreTime;

  @ManyToOne
  @JoinColumn(name = "gameId")
  private Game game;

  public GamePlayerStats(Game game, Player player) {
    this.game = game;
    this.player = player;
  }
}
