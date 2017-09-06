package org.microservice.game.web.websocket.model;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.microservice.game.web.websocket.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

/**
 *
 * @author Brighton Kukasira <brighton.kukasira@gmail.com>
 */
public interface Boards {

  // TODO: add clean up so that unused boards are removed from collection
  public static final ConcurrentHashMap<Long, Board> BOARDS
          = new ConcurrentHashMap<>(0);
  public static final String COMPUTER_PIT = "pit-05";
  public static final AtomicLong ID_GENERATOR = new AtomicLong(0);
  public static final Logger LOGGER = LoggerFactory.getLogger(Boards.class);
  public static final String PLAYER1_STORE = "pit-04";
  public static final String PLAYER2_STORE = "pit-00";

  static final String PAPER = "paper";
  static final String ROCK = "rock";
  static final String SCISSORS = "scissors";

  public static void applyMove(Board board, Long playerId, String pitId) {
    final String computerSelection = toLowercase((String) board.getPits().get(COMPUTER_PIT));
    if (computerSelection == null) {
      return;
    }
    final String playerSelection = toLowercase((String) board.getPits().get(pitId));
    if (playerSelection == null) {
      return;
    }
    Result result = getResult(computerSelection, playerSelection);
    if (result == null) {
      return;
    }
    updateScores(board, playerId, result);
  }

  public static Board assignAnyBoard(Player player) {
    Board selectedBoard = null;
    for (Iterator<Map.Entry<Long, Board>> iterator
            = BOARDS.entrySet().iterator(); iterator.hasNext();) {
      final Board board = iterator.next().getValue();
      if (board.getPlayer1() != null) {
        continue;
      }
      final Player existingPlayer1 = board.setIfNullPlayer1(player);
      if (existingPlayer1 == null) {
        selectedBoard = board;
        break;
      }
    }
    if (selectedBoard == null) {
      selectedBoard = new Board();
      selectedBoard.setBoardId(ID_GENERATOR.incrementAndGet());
      selectedBoard.setPlayer1(player);
      BOARDS.put(selectedBoard.getBoardId(), selectedBoard);
    }
    return selectedBoard;
  }

  public static Board get(Long boardId) {
    return BOARDS.get(boardId);
  }

  public static Board getOrCreateBoard(final Player player, SimpMessageHeaderAccessor headerAccessor) {
    final Board board = Boards.assignAnyBoard(player);
    if (headerAccessor != null) {
      headerAccessor.getSessionAttributes().put("boardId", board.getBoardId());
    }
    return board;
  }

  public static Result getResult(final String computerSelection,
          final String playerSelection) {
    if (computerSelection.equals(playerSelection)) {
      return Result.DRAW;
    }
    Result result = null;
    switch (computerSelection) {
      case PAPER:
        result = playerSelection.equals(SCISSORS) ? Result.WON : Result.LOST;
        break;
      case ROCK:
        result = playerSelection.equals(PAPER) ? Result.WON : Result.LOST;
        break;
      case SCISSORS:
        result = playerSelection.equals(ROCK) ? Result.WON : Result.LOST;
        break;
      default:
        LOGGER.error("Unsupported computer selection. Found " + computerSelection);
    }
    return result;
  }

  public static void removePlayer(Long playerId) {
    for (Iterator<Map.Entry<Long, Board>> iterator
            = BOARDS.entrySet().iterator(); iterator.hasNext();) {
      iterator.next().getValue().removePlayer(playerId);
    }
  }

  public static Board removePlayerFromBoard(Long boardId, Long playerId) {
    if (boardId == null) {
      return null;
    }
    if (playerId == null) {
      return null;
    }
    final Board board = BOARDS.get(boardId);
    board.removePlayer(playerId);
    return board;
  }

  public static String toLowercase(String string) {
    if (string == null) {
      return string;
    }
    return string.toLowerCase();
  }

  public static void updateComputerSelection(Board board) {
    board.getPits().put("pit-05", Select.random("Paper", "Rock", "Scissors"));
  }

  public static void updateScores(Board board, Long playerId, Result result) {
    boolean iamPlayerOne = board.getPlayer1().getPlayerId().equals(playerId);
    String myStore = iamPlayerOne ? PLAYER1_STORE : PLAYER2_STORE;
    String opponetStore = iamPlayerOne ? PLAYER2_STORE : PLAYER1_STORE;
    Integer myScore = (Integer) board.getPits().get(myStore);
    Integer opponentScore = (Integer) board.getPits().get(opponetStore);
    switch (result) {
      case DRAW:
        board.getPits().put(myStore, myScore + 1);
        board.getPits().put(opponetStore, opponentScore + 1);
        break;
      case LOST:
        board.getPits().put(opponetStore, opponentScore + 1);
        break;
      case WON:
        board.getPits().put(myStore, myScore + 1);
        break;
      default:
        LOGGER.error("Unsupported result. Found " + result);
    }
  }
  public String[] PLAYER1_PITS = {
    "pit-01",
    "pit-02",
    "pit-03"
  };
  public String[] PLAYER1_PITS_REVERSED = {
    "pit-03",
    "pit-02",
    "pit-01"
  };
}
