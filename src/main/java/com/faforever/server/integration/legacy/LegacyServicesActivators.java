package com.faforever.server.integration.legacy;

import com.faforever.server.chat.ChatService;
import com.faforever.server.client.ClientConnection;
import com.faforever.server.client.ClientService;
import com.faforever.server.client.ConnectionAware;
import com.faforever.server.client.ListCoopRequest;
import com.faforever.server.client.LoginMessage;
import com.faforever.server.client.SessionResponse;
import com.faforever.server.entity.Player;
import com.faforever.server.error.ErrorCode;
import com.faforever.server.error.RequestException;
import com.faforever.server.error.Requests;
import com.faforever.server.geoip.GeoIpService;
import com.faforever.server.integration.ChannelNames;
import com.faforever.server.player.PlayerService;
import com.faforever.server.security.FafUserDetails;
import com.faforever.server.security.UniqueIdService;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import static com.faforever.server.integration.MessageHeaders.CLIENT_CONNECTION;


@MessageEndpoint
public class LegacyServicesActivators {

  private final AuthenticationManager authenticationManager;
  private final ClientService clientService;
  private final UniqueIdService uniqueIdService;
  private final GeoIpService geoIpService;
  private final PlayerService playerService;
  private final ChatService chatService;

  @Inject
  public LegacyServicesActivators(AuthenticationManager authenticationManager, ClientService clientService, UniqueIdService uniqueIdService, GeoIpService geoIpService, PlayerService playerService, ChatService chatService) {
    this.authenticationManager = authenticationManager;
    this.clientService = clientService;
    this.uniqueIdService = uniqueIdService;
    this.geoIpService = geoIpService;
    this.playerService = playerService;
    this.chatService = chatService;
  }

  @ServiceActivator(inputChannel = ChannelNames.LEGACY_SESSION_REQUEST, outputChannel = ChannelNames.CLIENT_OUTBOUND)
  public SessionResponse askSession() {
    return SessionResponse.INSTANCE;
  }

  @ServiceActivator(inputChannel = ChannelNames.LEGACY_LOGIN_REQUEST)
  @Transactional
  public void loginRequest(LoginMessage loginRequest, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    Requests.verify(!playerService.isPlayerOnline(loginRequest.getLogin()), ErrorCode.USER_ALREADY_CONNECTED);

    try {
      UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loginRequest.getLogin(), loginRequest.getPassword());
      token.setDetails((ConnectionAware) () -> clientConnection);

      Authentication authentication = authenticationManager.authenticate(token);
      FafUserDetails userDetails = (FafUserDetails) authentication.getPrincipal();

      clientConnection.setUserDetails(userDetails);
      Player player = userDetails.getPlayer();
      player.setClientConnection(clientConnection);
      geoIpService.lookupCountryCode(clientConnection.getClientAddress()).ifPresent(player::setCountry);
      geoIpService.lookupTimezone(clientConnection.getClientAddress()).ifPresent(player::setTimeZone);

      uniqueIdService.verify(player, loginRequest.getUniqueId());
      chatService.updateIrcPassword(userDetails.getUsername(), loginRequest.getPassword());

      clientService.sendPlayerDetails(player, clientConnection.getUserDetails().getPlayer());
    } catch (BadCredentialsException e) {
      throw new RequestException(ErrorCode.INVALID_LOGIN, e);
    }
  }

  @ServiceActivator(inputChannel = ChannelNames.LEGACY_COOP_LIST)
  public void listCoopMissions(ListCoopRequest request, @Header(CLIENT_CONNECTION) ClientConnection clientConnection) {
    clientService.sendCoopList(clientConnection);
  }
}
