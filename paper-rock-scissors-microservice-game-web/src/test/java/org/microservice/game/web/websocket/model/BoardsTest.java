package org.microservice.game.web.websocket.model;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

/**
 *
 * @author Brighton Kukasira <brighton.kukasira@gmail.com>
 */
public class BoardsTest {

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  public BoardsTest() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of applyMove method, of class Boards.
   */
  @Test
  public void testApplyMove() {
    System.out.println("applyMove");
    Long playerId = 1L;
    Player player = new Player();
    player.setPlayerId(playerId);
    player.setUsername("player 1");
    Board board = Boards.assignAnyBoard(player);
    //computer selection
    board.getPits().put("pit-05", "Paper");
    String pitId = "pit-01";
    Boards.applyMove(board, playerId, pitId);
    assertEquals(new Integer(1), (Integer) board.getPits().get("pit-04"));
  }

  /**
   * Test of assignAnyBoard method, of class Boards.
   */
  @Test
  public void testAssignAnyBoard() {
    System.out.println("assignAnyBoard");
    Long playerId = 1L;
    Player player = new Player();
    player.setPlayerId(playerId);
    player.setUsername("player 1");
    Board result = Boards.assignAnyBoard(player);
    assertNotNull(result);
  }

  /**
   * Test of get method, of class Boards.
   */
  @Test
  public void testGet() {
    System.out.println("get");
    Long playerId = 1L;
    Player player = new Player();
    player.setPlayerId(playerId);
    player.setUsername("player 1");
    Board board = Boards.assignAnyBoard(player);
    Long boardId = board.getBoardId();
    Board result = Boards.get(boardId);
    assertNotNull(result);
  }

  /**
   * Test of getOrCreateBoard method, of class Boards.
   */
  @Test
  public void testGetOrCreateBoard() {
    System.out.println("getOrCreateBoard");
    Player player = null;
    SimpMessageHeaderAccessor headerAccessor = null;
    Board result = Boards.getOrCreateBoard(player, headerAccessor);
    assertNotNull(result);
  }

  /**
   * Test of getResult method, of class Boards.
   */
  @Test
  public void testGetResultDraw() {
    System.out.println("getResult");
    String computerSelection = "rock";
    String playerSelection = "rock";
    Result expResult = Result.DRAW;
    Result result = Boards.getResult(computerSelection, playerSelection);
    assertEquals(expResult, result);
  }

  /**
   * Test of getResult method, of class Boards.
   */
  @Test
  public void testGetResultWon() {
    System.out.println("getResult");
    String computerSelection = "rock";
    String playerSelection = "paper";
    Result expResult = Result.WON;
    Result result = Boards.getResult(computerSelection, playerSelection);
    assertEquals(expResult, result);
  }

  /**
   * Test of getResult method, of class Boards.
   */
  @Test
  public void testGetResultLost() {
    System.out.println("getResult");
    String computerSelection = "paper";
    String playerSelection = "rock";
    Result expResult = Result.LOST;
    Result result = Boards.getResult(computerSelection, playerSelection);
    assertEquals(expResult, result);
  }

  /**
   * Test of removePlayer method, of class Boards.
   */
  @Test
  public void testRemovePlayer() {
    System.out.println("removePlayer");
    Long playerId = 1L;
    Player player = new Player();
    player.setPlayerId(playerId);
    player.setUsername("player 1");
    Board board = Boards.assignAnyBoard(player);
    Boards.removePlayer(playerId);
    assertNull(board.getPlayer1());
  }

  /**
   * Test of removePlayerFromBoard method, of class Boards.
   */
  @Test
  public void testRemovePlayerFromBoard() {
    System.out.println("removePlayerFromBoard");
    Long playerId = 1L;
    Player player = new Player();
    player.setPlayerId(playerId);
    player.setUsername("player 1");
    Board board = Boards.assignAnyBoard(player);
    Long boardId = board.getBoardId();
    Board result = Boards.removePlayerFromBoard(boardId, playerId);
    assertNotNull(result);
  }

  /**
   * Test of toLowercase method, of class Boards.
   */
  @Test
  public void testToLowercase() {
    System.out.println("toLowercase");
    String string = "Paper";
    String expResult = "paper";
    String result = Boards.toLowercase(string);
    assertEquals(expResult, result);
  }

  /**
   * Test of updateComputerSelection method, of class Boards.
   */
  @Test
  public void testUpdateComputerSelection() {
    System.out.println("updateComputerSelection");
    Board board = new Board();
    Boards.updateComputerSelection(board);
    assertNotNull(board.getPits().get("pit-05"));
  }

  /**
   * Test of updateScores method, of class Boards.
   */
  @Test
  public void testUpdateScores() {
    System.out.println("updateScores");
    Long playerId = 1L;
    Player player = new Player();
    player.setPlayerId(playerId);
    player.setUsername("player 1");
    Board board = Boards.assignAnyBoard(player);
    Result result_2 = Result.DRAW;
    Boards.updateScores(board, playerId, result_2);
    assertEquals(new Integer(1), board.getPits().get("pit-00"));
    assertEquals(new Integer(1), board.getPits().get("pit-04"));
  }

}
