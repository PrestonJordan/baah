import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Hashtable;
import java.util.List;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class GameServer extends WebSocketServer
{
   
   private static final String CARD_FILE = "config/cards.txt";
   
   private static List<String> WHITE_DECK;
   private static List<String> BLACK_DECK;
   private static final boolean DEBUG = false;
   
   private static final Hashtable<WebSocket, Client> clients = new Hashtable<WebSocket, Client>();
   
   static
   {
      WebSocketImpl.DEBUG = DEBUG;
   }
   
   public GameServer(int socket) throws IOException
   {
      super(new InetSocketAddress(socket));
      Log.l("Success!!!");
      Log.l("Attempting to load cards...");
      CardParser cp = new CardParser(CARD_FILE);
      WHITE_DECK = cp.getWhiteCards();
      Log.l("White cards loaded.");
      BLACK_DECK = cp.getBlackCards();
      Log.l("Black cards loaded.");
   }
   
   @Override
   public void onOpen(WebSocket conn, ClientHandshake handshake)
   {
      Log.l("onOpen called");
      //Do nothing
   }

   @Override
   public void onClose(WebSocket conn, int code, String reason, boolean remote)
   {
      Log.l("onClose called with code " + code + " and reason " + reason + ".");
      if(clients.containsKey(conn))
      {
         Client c = clients.get(conn);
         if(!c.hasLeftGame())
            c.unexpectedDisconnection();
         clients.remove(conn);
      }
   }

   @Override
   public void onMessage(WebSocket conn, String message)
   {
      Log.l("onMessage called");
      Log.recv(message);
      
      //If client wants to create a game
      if (Key.match(Key.createGame, message))
      {
         GameRoom gr = new GameRoom(new Deck(WHITE_DECK.size(), BLACK_DECK.size()));
         Client c = new Client(conn);
         
         Log.l("Create game request processed. Make room " + gr.getID() + " and assined id " + c.getID() + " to client.");
         Log.conn(conn, Key.gameCreated.toString() + '|' + gr.getID());
         Log.conn(conn, Key.issueID.toString() + '|' + c.getID());
         
         issueCards(conn);//Issue cards!
         gr.addClient(c);
         
         clients.put(conn, c);
      }
      
      //If client wants to join a game
      else if (Key.match(Key.joinGame, message))
      {
         Log.l("Client looking for game with " + message);
         int pos = message.indexOf('|');
         if(pos == -1)
         {
            Log.l("No game found case 1: parse failure");
            Log.conn(conn, "NoGameFound");
         }
         else
         {
            String gameName = message.substring(pos + 1).trim();
            if(!GameRoom.hasGameRoom(gameName))
            {
               Log.l("No game found case 2: room name '" + gameName + "' doesn't exist");
               Log.conn(conn, "NoGameFound");
            }
            else
            {
               
               Client c = new Client(conn);
               GameRoom gr = GameRoom.lookup(gameName);
               
               Log.l("Game found. Issued ClientID " + c.getID() + " and added to game " + gr.getID());
               Log.conn(conn, Key.issueID.toString() + '|' + c.getID());
               Log.conn(conn, Key.joinGame.toString() + '|' + gr.getID());
               
               issueCards(conn);//Issue cards!
               gr.addClient(c);
               
               clients.put(conn, c);
            }
         }
      }
      
      //Reconnecting client
      else if (Key.match(Key.rejoinGame, message))
      {
         String[] parts = message.split("[|]");
         if (parts.length > 1)
         {
            String name = parts[1].trim();
            if(Client.hasClient(name))
            {
               Client c = Client.lookup(name);
               if(!c.isConnected())
               {
                  Log.conn(conn, Key.clientFound.toString());
                  
                  issueCards(conn);//Issue cards!
                  clients.put(conn, c);
                  
                  c.reconnetion(conn);
               }
               else
                  Log.conn(conn, Key.noClientFound.toString());
            }
            else
               Log.conn(conn, Key.noClientFound.toString());
         }
         else
            Log.conn(conn, Key.noClientFound.toString());
      }
      
      else
         
         Client.routeToClient(message);

   }
   
   @Override
   public void onError(WebSocket conn, Exception ex)
   {
      Log.l("onError called");
      ex.printStackTrace();
      //if(!conn.isOpen())
         //Do stuff...
   }
   
   private void issueCards(WebSocket conn)
   {
      Log.l("Serving cardsets.");
      String white = Key.cardSetWhite.toString();
      String black = Key.cardSetBlack.toString();
      for(String s: WHITE_DECK)
         white += '|' + s;
      for(String s: BLACK_DECK)
         black += '|' + s;
      Log.conn(conn, white);
      Log.conn(conn, black);
   }

}
