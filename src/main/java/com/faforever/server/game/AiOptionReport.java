package com.faforever.server.game;

import com.faforever.server.common.ClientMessage;
import lombok.Data;

@Data
public class AiOptionReport implements ClientMessage {
  private final String aiName;
  private final String key;
  private final Object value;
}
