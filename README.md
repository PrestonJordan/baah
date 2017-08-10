# Hackathon-BAAH
#### Made for the Fall 2016 [HackISU](http://hackisu.org/) hackathon.
Contributors: [William Fries](https://github.com/FriesW), [Isaac Speed](https://github.com/isaacspeed), [Preston Jordan](https://github.com/PrestonJordan), [Lewis Amos](https://github.com/LewisII)

## Why?
Frequently we find ourselves without a CAH deck when we want to play, and all the online versions lack necessary traits of the dead tree version, thus leaving us socially unsatisfied with the mediocre compromise. This is our half-ass solution for anytime play.

## What does it do?
It uses smartphones to emulate the behavior of cards. Each player has their own phone. When in "selection" mode, each player's phone displays their hand, except for the judge, whose phone displays the black card. Players select a card and then set down their phones in a circle, as if it were a card. "Judging" mode then begins, in which each phone on the table shows a played card, and the judge taps the phone with the winning card. Phones go back to players, black card moves to a new phone. Repeat.

## How does it do it?
Players visit a webpage on their phone and join the gameroom by an offensively memorable title. Javascript then talks to the game server through websockets. Game server is written in Java. Websockets is super convenient as it handles keep-alive, sessions, and other things. Static HTML, CSS, and Javascript is served by your favorite webserver, probably Apache.

## I want to play!
Don't. The software is suuuuper buggy and has lots of issues. The game server likes to miss when a player disconnects, and then locks the game up waiting for a non-existent player to pick a card. There is a workaround for getting a fresh hand. Connections aren't properly threaded, and thus one slow client ruins the game for everyone. Special characters aren't supported in the text of the cards, and instead of properly handling it we just did some quick regex to strip out anything more advanced than your abc's and 123's. Judge selection is unpredicatble and frequently unfair. No support for playing multiple white cards on a black card. We plan on revisiting the project and making it in a more sensible manner, but this has yet to happen.

## Libraries, Dependencies, and Resources
* Server
  * [Java-WebSocket](https://github.com/TooTallNate/Java-WebSocket) for management of websockets.
* Web client
  * [jQuery](https://jquery.com/) because who doesn't.
  * [JQuery Mobile](https://jquerymobile.com/) because we're lazy.
  * [jquery-qrcode](https://github.com/jeromeetienne/jquery-qrcode) for creating QR codes in browser.
  * [EmojiOne](http://emojione.com/) for the poo emoji.

## Want more?
A more wordy, sleep-deprivation inspired description of the project can be seen on the [original devpost](https://devpost.com/software/bad-apples-against-humanity).
