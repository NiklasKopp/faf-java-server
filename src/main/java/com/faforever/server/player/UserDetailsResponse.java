package com.faforever.server.player;

import com.faforever.server.common.ServerMessage;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.TimeZone;

@Getter
@RequiredArgsConstructor
@ToString
public class UserDetailsResponse implements ServerMessage {
  private final int userId;
  private final String username;
  private final String country;
  private final TimeZone timeZone;
  private final Player player;

  @Data
  public static class Player {
    private final Rating globalRating;
    private final Rating ladder1v1Rating;
    private final int numberOfGames;
    private final Avatar avatar;
    private final String clanTag;

    @Data
    public static class Rating {
      private final double mean;
      private final double deviation;
    }

    @Data
    public static class Avatar {
      private final String url;
      private final String tooltip;
    }
  }
}
