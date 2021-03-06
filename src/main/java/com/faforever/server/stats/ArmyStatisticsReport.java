package com.faforever.server.stats;

import com.faforever.server.common.ClientMessage;
import lombok.Data;

import java.util.List;

@Data
public class ArmyStatisticsReport implements ClientMessage {
  private final List<ArmyStatistics> armyStatistics;
}
