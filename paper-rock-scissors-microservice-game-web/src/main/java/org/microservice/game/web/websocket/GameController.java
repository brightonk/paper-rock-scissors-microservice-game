package org.microservice.game.web.websocket;

/**
 *
 * @author Brighton Kukasira <brighton.kukasira@gmail.com>
 */
import org.microservice.game.web.exceptions.BoardNotFoundException;
import org.microservice.game.web.websocket.model.AddPlayerMessage;
import org.microservice.game.web.websocket.model.Board;
import org.microservice.game.web.websocket.model.Boards;
import static org.microservice.game.web.websocket.model.Boards.updateComputerSelection;
import org.microservice.game.web.websocket.model.MessageType;
import org.microservice.game.web.websocket.model.PlayerMoveMessage;
import org.microservice.game.web.websocket.model.PlayerResponse;
import org.microservice.game.web.websocket.model.Players;
import static org.microservice.game.web.websocket.model.Players.createPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
public class GameController {

  private static final Logger LOGGER = LoggerFactory.getLogger(GameController.class);

  public static PlayerResponse createResponse(final Board board,
          MessageType type, String username, String requestId) {
    final PlayerResponse response = new PlayerResponse();
    response.setBoard(board);
    response.setRequestId(requestId);
    response.setType(type);
    response.setUsername(username);
    return response;
  }

  public static Long getOrCreatePlayerId(SimpMessageHeaderAccessor headerAccessor) {
    Long playerId = (Long) headerAccessor.getSessionAttributes().get("playerId");
    if (playerId == null) {
      playerId = Players.ID_GENERATOR.incrementAndGet();
      headerAccessor.getSessionAttributes().put("playerId", playerId);
    }
    return playerId;
  }

  @MessageMapping("/game.addPlayer")
  @SendTo("/topic/public")
  public PlayerResponse addPlayer(@Payload AddPlayerMessage message,
          SimpMessageHeaderAccessor headerAccessor) {
    try {
      Players.checkUsernameUniqueness(message.getUsername());
      // Add username in web socket session
      headerAccessor.getSessionAttributes().put("username", message.getUsername());
      Long playerId = getOrCreatePlayerId(headerAccessor);
      Boards.removePlayer(playerId);
      Board board = Boards.getOrCreateBoard(createPlayer(playerId, message.getUsername()), headerAccessor);
      updateComputerSelection(board);
      return createResponse(board, message.getType(), message.getUsername(), message.getRequestId());
    } catch (Exception ex) {
      ex.printStackTrace();
      throw ex;
    }
  }

  @MessageExceptionHandler
  @SendToUser("/queue/errors")
  public String handleException(Throwable exception) {
    return exception.getMessage();
  }

  @MessageMapping("/game.playerMove")
  @SendTo("/topic/public")
  public PlayerResponse playerMove(@Payload PlayerMoveMessage message,
          SimpMessageHeaderAccessor headerAccessor) {
    Board board = null;
    String username = null;
    try {
      board = Boards.get(message.getBoardId());
      if (board == null) {
        throw new BoardNotFoundException();
      }
      username = (String) headerAccessor.getSessionAttributes().get("username");
      Boards.applyMove(board, message.getPlayerId(), message.getPitId());
    } catch (Exception ex) {
      ex.printStackTrace();
      throw ex;
    }
    updateComputerSelection(board);
    return createResponse(board, message.getType(), username, message.getRequestId());
  }
}
