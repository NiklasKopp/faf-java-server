package com.faforever.server.chat;

import com.faforever.server.client.ClientService;
import com.faforever.server.config.ServerProperties;
import com.faforever.server.config.ServerProperties.Chat;
import com.faforever.server.entity.GroupAssociation;
import com.faforever.server.entity.User;
import com.faforever.server.error.ProgrammingError;
import com.faforever.server.player.PlayerOnlineEvent;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class ChatService {

  private final NickCoreRepository nickCoreRepository;
  private final ServerProperties properties;
  private final ClientService clientService;

  public ChatService(NickCoreRepository nickCoreRepository, ServerProperties properties, ClientService clientService) {
    this.nickCoreRepository = nickCoreRepository;
    this.properties = properties;
    this.clientService = clientService;
  }

  public void updateIrcPassword(String username, String password) {
    log.debug("Updating IRC password for user: {}", username);
    try {
      nickCoreRepository.updatePassword(username, Hashing.md5().hashString(password, StandardCharsets.UTF_8).toString());
    } catch (BadSqlGrammarException e) {
      log.warn("IRC password for user '{}' could not be updated ({})", username, e.getMessage());
    }
  }

  @EventListener
  public void onPlayerOnlineEvent(PlayerOnlineEvent event) {
    Chat chat = properties.getChat();

    Set<String> channels = new HashSet<>(3, 1);
    channels.addAll(chat.getDefaultChannels());

    Optional.ofNullable(event.getPlayer().getClan())
      .map(clan -> (String.format(chat.getClanChannelFormat(), clan.getTag())))
      .ifPresent(channels::add);

    User user = event.getPlayer().getUser();
    GroupAssociation groupAssociation = user.getGroupAssociation();
    if (groupAssociation != null) {
      switch (groupAssociation.getGroup()) {
        case ADMIN:
          channels.addAll(chat.getAdminChannels());
          break;
        case MODERATOR:
          channels.addAll(chat.getModeratorChannels());
          break;
        default:
          throw new ProgrammingError("Uncovered group: " + groupAssociation.getGroup());
      }
    }

    clientService.sendChatChannels(channels, event.getPlayer());
  }
}
