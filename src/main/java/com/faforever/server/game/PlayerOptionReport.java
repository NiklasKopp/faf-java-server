package com.faforever.server.game;

import com.faforever.server.common.ClientMessage;
import lombok.Data;

@Data
public class PlayerOptionReport implements ClientMessage {
  private final int playerId;
  private final String key;
  private final Object value;
}
