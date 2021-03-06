package com.faforever.server.player;

public class LoginDetailsResponse extends PlayerResponse {

  public LoginDetailsResponse(PlayerResponse playerResponse) {
    super(
      playerResponse.getUserId(),
      playerResponse.getUsername(),
      playerResponse.getCountry(),
      playerResponse.getPlayer()
    );
  }
}
