const SITE_URL = "45.55.137.94"

const GAME_CREATED = "GameCreated";
const NO_GAME_FOUND = "NoGameFound";
const CLIENT_FOUND = "ClientFound";
const NO_CLIENT_FOUND = "NoClientFound";
const ISSUED_ID = "IssueID";
const ISSUE_WHITE_CARD_SET = "IssueWhiteCardSet";
const ISSUE_BLACK_CARD_SET = "IssueBlackCardSet";

const PICKING = "Picking";
const PICKING_HAND = "Hand";
const PICKING_BLACK = "Black";

const SHOW = "Show";
const SHOW_WHITE = "White"
const SHOW_TWO = "Two";
const SHOW_BLACK = "Black";

const UPDATE = "Update";
const UPDATE_SCORE = "Score";
const UPDATE_HIGH_SCORE = "HighScore";

const WINNER = "Winner";

const WAIT = "Wait";

const CARD_MARGIN = 60;
const CARD_OFFSET = 500;

var numberOfCards = 0;
var score = 0;
var highScore = 0;
var whiteCards;
var blackCards;
var ws;
var gameId;
var clientId;

$(document).ready(function() {    
    if (getCookie('clientId') !== '') {
        clientId = getCookie('clientId');
        gameId = getCookie('gameId');
        alert("rejoin");
        rejoinGame();
    } else if (window.location.hash !== "") {
        gameId = decodeURIComponent(window.location.hash.substring(1));
        joinGame();
    } else {
        $("#createGameButton").click(function() {
            createGame()
        });
        $("#joinGameButton").click(function() {
            gameId = $("#gameIdInput").val();
            joinGame();
        });
    }
});


function createGame() {
    $("#startGame").hide();
    openConnection(true);
}

function joinGame() {
    $("#startGame").hide();
    openConnection(false, gameId);
    setCookie("gameId", gameId, 1);
}

function rejoinGame() {
    $("#startGame").hide();
    openConnection(false, undefined, true);
}

function displayHand(hand) {
    for (var i = 0; i < hand.length; i++) {
        addWhiteCard(hand[i]);
        $("#card" + hand[i]).click(cardClicked);
    }
}

function showBlackAndWhiteCard(blackCard, whiteCard) {
    addWhiteCard(whiteCard);
    addBlackCard(blackCard);
}

function addBlackCard(card) {
    numberOfCards++;
    addCard(blackCards[card], false, card, CARD_MARGIN + (numberOfCards - 1) * CARD_OFFSET);
}

function addWhiteCard(card) {
    numberOfCards++;
    addCard(whiteCards[card % whiteCards.length], true, card, CARD_MARGIN + (numberOfCards - 1) * CARD_OFFSET);
}

function addCard(text, isWhite, idNum, offset) {
    var colorClass;
    if (isWhite) {
        colorClass = "white";
    } else {
        colorClass = "black";
    }
    var newCard = $("<div></div>").attr('id', 'card' + idNum).addClass('card').addClass(colorClass).append("<p>" + text + "</p>");
    newCard.css("top", offset);
    $("body").append(newCard);
}

function removeCards() {
    $(".card").remove();
    numberOfCards = 0;
}

function removeMessages() {
    $(".message").remove();
}

function clear() {
    removeCards();
    removeMessages();
}

function showGameInfo() {
    hideGameInfo();
    var gameInfo = $("<div id='topSpace'></div>").attr('id', 'gameInfo').addClass('card').addClass('gameInfo')
    gameInfo.append("<div class='center'><p class='pCenter'>Your score: <span id='score'>" + score + "</span></p></div>");
    gameInfo.append("<div class='center'><p class='pCenter'>High score: <span id='highScore'>" + highScore + "</span></p></div>");
    gameInfo.append($("<div id='qrcode'></div>"));
    gameInfo.append("<div class='center'><p class='pCenter'>" + gameId + "</p></div>");
    gameInfo.append("<div class='center'><p class='pCenter'><button id='leaveGameButton'>Leave game</button></p></div>");
    gameInfo.css("top", CARD_MARGIN + (numberOfCards * CARD_OFFSET));
    $("body").append(gameInfo);
    
    new QRCode(document.getElementById("qrcode"), SITE_URL + "/#" + encodeURIComponent(gameId));
    
    $("#leaveGameButton").click(function() {
        leaveGame();
    });
}

function hideGameInfo() {
    $(".gameInfo").remove();
}

function updateGameInfo() {
    $("#score").text(score);
    $("#highScore").text(highScore);
}

function selectCard(whiteCardNumber) {
    ws.send(gameId + " | " + clientId + " | Show | " + whiteCardNumber);
}

function cardClicked(evt) {
    var id;
    if (evt.target.nodeName === "DIV") {
        id = evt.target.id;
    } else if (evt.target.nodeName === "P") {
        id = evt.target.parentElement.id;
    }
    
    if ($("#" + id).hasClass("whiteSelected")) {
        $("#" + id).removeClass("whiteSelected");
        $(".card").off("click");
        flipCard(id);
        ws.send(gameId + " | " + clientId + " | " + PICKING + " | " + id.substring(4));
    } else {
        $(".card").removeClass("whiteSelected");
        $("#" + id).addClass("whiteSelected");    
    }
}

function flipCard(cardId) {
    $(".card:not(#" + cardId + ")").remove();
    $("#" + cardId).css("top", CARD_MARGIN + "px");
    $("#" + cardId).css("transition", "all 1.0s linear");
    $("#" + cardId).css("transform", "rotateY(90deg)");
    $("#" + cardId).css("text-align", "center");
    $("#" + cardId + " p").html("Bad Apples<br />Against<br />Humanity");
    $("#" + cardId + " p").addClass("backOfCard");
    $("#" + cardId).css("transform", "rotateY(180deg)");
}

function gameNotFound() {
    messageUser("you're outta luck, fam. Game couldn't be found.");
    exit();
}

function exit() {
    ws.close();
    setCookie("clientId", "", -1);
    setCookie("gameId", "", -1);
    setTimeout(function(){ window.location = "http://" + SITE_URL; }, 2000); 
}

function leaveGame() {
    ws.send(gameId + " | " + clientId + " | Leaving");
    clear();
    messageUser("You have left the game!");
    exit();
}

function openConnection(isGameCreator, gameIdInput, isRejoining) {
    if ("WebSocket" in window)
    {
       // Let us open a web socket
       ws = new WebSocket("ws://" + SITE_URL + ":443/");
        //ws = new WebSocket("ws://localhost:8887")
        
        if (isGameCreator) {
            ws.onopen = function() {
                onOpenCreateGame();
            }
        } else {
            if (isRejoining) {
                ws.onopen = function() {
                    onOpenRejoinGame();
                }
            } else {
                ws.onopen = function() {
                    onOpenJoinGame(gameIdInput);
                }
            }
        }

       ws.onmessage = processMessage;

       ws.onclose = function()
       { 
          // websocket is closed.
          console.log("Connection is closed..."); 
       };
    }

    else
    {
       // The browser doesn't support WebSocket
       alert("WebSocket NOT supported by your Browser!");
    }
}

function processMessage(evt) {
    var cmds = evt.data.split("|");
    
    console.log("Command: " + cmds);
    
    if (cmds[0].includes(GAME_CREATED)) {
        showGameInfo();
        var gameIdFromServer = cmds[1];
        gameId = gameIdFromServer;
        setCookie("gameId", gameId, 1);
    } else if (cmds[0].includes(ISSUED_ID)) {
        showGameInfo();
        var clientIdFromServer = cmds[1];
        clientId = clientIdFromServer;
        setCookie("clientId", clientId, 1);
    } else if (cmds[0].includes(NO_CLIENT_FOUND)) {
        messageUser("Sorry, could not find your game!");
        setCookie("clientId", "", -1);
        setCookie("gameId", "", -1);
        exit();
    } else if (cmds[0].includes(CLIENT_FOUND)) {
        messageUser("Resuming game...");
    } else if (cmds[0].includes(NO_GAME_FOUND)) {
        gameNotFound();
    } else if (cmds[0].includes(ISSUE_WHITE_CARD_SET)) {
        var cardsArray = cmds.slice(1);
        whiteCards = cardsArray;
    } else if (cmds[0].includes(ISSUE_BLACK_CARD_SET)) {
        var cardsArray = cmds.slice(1);
        blackCards = cardsArray;
    } else if (cmds[0].includes(PICKING)) {
        clear();
        if (cmds[1].includes(PICKING_HAND)) {
            var hand = cmds.slice(2);
            displayHand(hand);
        } else if (cmds[1].includes(PICKING_BLACK)) {
            var blackCardNumber = parseInt(cmds[2]);
            addBlackCard(blackCardNumber);
        }
        showGameInfo();
    } else if (cmds[0].includes(SHOW)) {
        clear();
        hideGameInfo();
        if(cmds[1].includes(SHOW_WHITE)) {
            var whiteCardNumber = parseInt(cmds[2]);
            addWhiteCard(whiteCardNumber);
            $("#card" + whiteCardNumber).on("taphold", function() {
                selectCard(whiteCardNumber);
                clear();
            })
        } else if(cmds[1].includes(SHOW_TWO)) {
            var blackCardNumber = parseInt(cmds[2]);
            var whiteCardNumber = parseInt(cmds[3]);
            showBlackAndWhiteCard(blackCardNumber, whiteCardNumber);
            $("#card" + whiteCardNumber).on("taphold", function() {
                selectCard(whiteCardNumber);
                clear();
            })
        }
    } else if (cmds[0].includes(UPDATE)) {
        if (cmds[1].includes(UPDATE_HIGH_SCORE)) {
            highScore = parseInt(cmds[2]);
        } else if (cmds[1].includes(UPDATE_SCORE)) {
            score = parseInt(cmds[2]);
        }
        updateGameInfo();
    } else if (cmds[0].includes(WINNER)) {
        clear();
        hideGameInfo();
        if (cmds[1].includes("T")) {
            messageUser("You won! Score: " + score + "<br/>High score: " + highScore, "goldenPoop");
        } else {
            messageUser("Score: " + score + "<br/>High score: " + highScore, "brownPoop");
        }
    } else if (cmds[0].includes(WAIT)) {
        clear();
        messageUser("Please wait for the next round.");
    }
}

function parseCardSet(cardsString) {
    return cardsString.split(" | ");
}

function messageUser(text, messageClass) {
    var message = $("<div></div>").addClass('message').append("<p>" + text + "</p>");
    if (!(messageClass === undefined)) {
        message.addClass(messageClass);
    }
    $("body").append(message);
}

function onOpenCreateGame() {
    ws.send("CreateGame");
}

function onOpenJoinGame(gameIdInput) {
    ws.send("JoinGame | " + gameIdInput);
}

function onOpenRejoinGame() {
    ws.send("RejoinGame | " + clientId);
}


// thanks W3Schools
function setCookie(cname, cvalue, exdays) {
    var d = new Date();
    d.setTime(d.getTime() + (exdays*24*60*60*1000));
    var expires = "expires="+ d.toUTCString();
    document.cookie = cname + "=" + cvalue + "; " + expires;
} 

function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for(var i = 0; i <ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0)==' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length,c.length);
        }
    }
    return "";
} 