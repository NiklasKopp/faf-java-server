package com.faforever.server.game;

import com.faforever.server.common.ClientMessage;
import lombok.Data;

@Data
public class GameModsCountReport implements ClientMessage {
  private final int count;
}
