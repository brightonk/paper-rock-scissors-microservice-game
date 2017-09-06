'use strict';

var usernamePage = document.querySelector('#username-page');
var gamePage = document.querySelector('#game-page');
var usernameForm = document.querySelector('#usernameForm');
var messageArea = document.querySelector('#messageArea');
var recentResultsArea = document.querySelector('#recentResultsArea');
var errorMessageArea = document.querySelector('#errorMessageArea');
var connectingElement = document.querySelector('.connecting');

var board1 = document.querySelector('#player1-board');
var board2 = document.querySelector('#player2-board');
var thisUserPlayer1 = false;

var stompClient = null;
var username = null;
var requestIds = [];
var boardId = null;
var playerId = null;
var computerSection = null;

var player1Pits = [
  '#pit-01',
  '#pit-02',
  '#pit-03'
];
function enablePlayer1BoardClicks() {
  'use strict';
  var arrayLength = player1Pits.length;
  for (var i = 0; i < arrayLength; i++) {
    var pit = document.querySelector(player1Pits[i]);
    pit.addEventListener('click', submitSelection);
  }
}
function connect(event) {
  'use strict';
  username = document.querySelector('#name').value.trim();
  if (username) {
    usernamePage.classList.add('hidden');
    gamePage.classList.remove('hidden');
    let socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, onConnected, onError);
  }
  event.preventDefault();
}
function createRequestId(username) {
  'use strict';
  let requestId = username + '_' + (window.performance.timing.navigationStart + window.performance.now()).toFixed(3);
  requestIds.push(requestId);
  return requestId;
}
function onAppError(erroMessage) {
  'use strict';
  usernamePage.classList.remove('hidden');
  gamePage.classList.add('hidden');
  displayOnMessageArea(erroMessage.body);
}
function displayOnMessageArea(message, displayArea) {
  'use strict';
  if (!isValid(displayArea)) {
    displayArea = messageArea;
  }
  updateMessageArea(message, 'info', displayArea);
}
function onConnected() {
  'use strict';
  stompClient.subscribe("/user/queue/errors", onAppError);
  // Subscribe to the Public Channel
  stompClient.subscribe('/topic/public', onMessageReceived);
  // Tell your username to the server
  stompClient.send("/app/game.addPlayer",
          {},
          JSON.stringify({username: username, type: 'JOIN', requestId: createRequestId(username)})
          );
  connectingElement.classList.add('hidden');
}
function onError(error) {
  'use strict';
  usernamePage.classList.remove('hidden');
  gamePage.classList.add('hidden');
  connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
  connectingElement.style.color = 'red';
}
function onLeaveMessage(message) {
  'use strict';
//  console.log("onLeaveMessage(message): " + JSON.stringify(message, null, 4));
}
function isValid(object) {
  'use strict';
  if (object === null) {
    return false;
  }
  if (typeof object === undefined) {
    return false;
  }
  return true;
}
function getPlayerLabel(player) {
  'use strict';
  var label = 'Waiting for player to join...';
  if (!isValid(player)) {
    console.log("player NOT valid");
    return label;
  }
  if (!isValid(player.username)) {
    console.log("player.username NOT valid. Found: " + player.username);
    return label;
  }
  label = player.username;
  return label;
}
function updateMessageArea(message, type, displayArea) {
  'use strict';
  var content = message;
  if (type === 'JOIN') {
    content = content + ' joined!';
  } else if (type === 'LEAVE') {
    content = content + ' left!';
  }
  var messageText = document.createTextNode(content);
  var textElement = document.createElement('p');
  textElement.appendChild(messageText);
  var messageElement = document.createElement('li');
  messageElement.classList.add('event-message');
  messageElement.appendChild(textElement);
  if (isValid(displayArea)) {
    displayArea.innerHTML = messageElement.outerHTML + displayArea.innerHTML;
    displayArea.scrollTop = displayArea.scrollHeight;
  } else {
    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
  }
}
function onJoinMessage(message) {
  'use strict';
  console.log("onJoinMessage(message) " + JSON.stringify(message, null, 4));
  if (username === null) {
    console.log('username is null');
    return;
  }
  if (typeof username === undefined) {
    console.log('username is undefined');
    return;
  }
  if (message.board === null) {
    console.log('message.board is null');
    return;
  }
  if (typeof message.board === undefined) {
    console.log('message.board is undefined');
    return;
  }
  boardId = message.board.boardId;
  console.log("boardId: " + boardId);
  playerId = message.board.player1.playerId;
  thisUserPlayer1 = true;
  computerSection = message.board.pits["pit-05"];
  enablePlayer1BoardClicks();
  var player1Name = document.querySelector('#player1-name');
  player1Name.innerText = getPlayerLabel(message.board.player1);
  updateMessageArea(message.username, message.type, messageArea);
}
function onPlayMessage(message) {
  'use strict';
  console.log("onPlayMessage(message) " + JSON.stringify(message, null, 4));
  if (!isValid(boardId)) {
    console.log('board is not valid');
    return;
  }
  if (!isValid(message.board.boardId)) {
    console.log('boardId is not valid');
    return;
  }
  if (boardId !== message.board.boardId) {
    console.log('boardId NOT matched');
    return;
  }
  computerSection = message.board.pits["pit-05"];
  if (isValid(message.board.winnerPlayer)) {
    updateWinner(message);
    return;
  }
  updateBoardCounts(message);
}
function updateWinner(message) {
  'use strict';
  if (message.board.winnerPlayer.playerId === playerId) {
    displayOnMessageArea('You WON');
  } else {
    displayOnMessageArea('You LOST');
  }
}
function updateBoardCounts(message) {
  'use strict';
  console.log('updateBoardCounts');
  for (var key in message.board.pits) {
    if (key === 'pit-05') {
      // do not display computer selection
      continue;
    }
    if (message.board.pits.hasOwnProperty(key)) {
      var value = message.board.pits[key];
      var pit = document.querySelector('#' + key);
      pit.innerText = value;
    }
  }
}
function onMessageReceived(payload) {
  'use strict';
//  console.log("onMessageReceived(payload): " + JSON.stringify(payload, null, 4));
  var message = JSON.parse(payload.body);
  switch (message.type) {
    case 'LEAVE':
      window.console.log('message type: ' + message.type);
      onLeaveMessage(message);
      break;
    case 'JOIN':
      window.console.log('message type: ' + message.type);
      onJoinMessage(message);
      break;
    case 'PLAY':
      window.console.log('message type: ' + message.type);
      onPlayMessage(message);
      break;
    default:
      window.console.log('Unsupported message type: ' + message.type);
  }
}
function submitSelection(event) {
  'use strict';
  window.console.log('clicked');
  var pitId = event.target.id;
  var selection = event.target.innerText;
  window.console.log('clicked by username:' + username + '. pitId: ' + pitId + " selection: " + selection);
  displayOnMessageArea('computer: ' + computerSection + ' - ' + selection + ' :human', recentResultsArea);
  if (stompClient) {
    var playerMoveMessage = {
      playerId: playerId,
      boardId: boardId,
      pitId: pitId,
      selection: selection,
      type: 'PLAY',
      requestId: createRequestId(username)
    };
    var payload = JSON.stringify(playerMoveMessage);
    console.log('SEND payload: ' + payload);
    stompClient.send("/app/game.playerMove", {}, payload);
  } else {
    displayOnMessageArea('Error. Selection not submitted to server. connection lost.');
  }
}
usernameForm.addEventListener('submit', connect, true);