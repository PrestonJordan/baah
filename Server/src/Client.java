import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import org.java_websocket.WebSocket;

public class Client
{
   
   private static final Hashtable<String, Client> clients = new Hashtable<String, Client>();
   
   //Backend
   private final String clientID;
   private ClientEventListener listener;
   
   //Connection state
   private WebSocket lastConn;
   private boolean connected;
   private boolean hasLeft;
   
   //Attribute
   private List<Integer> hand;
   private int score;
   
   //Display state
   private int lastPlayedCard;
   private boolean displayingBlack;
   private boolean isPlayer; // == !isJudge
   
   
   public Client(WebSocket conn)
   {
      clientID = UUID.randomUUID().toString();
      listener = null;
      clients.put(getID(), this);
      
      lastConn = conn;
      connected = true;
      hasLeft = false;
      
      hand = new ArrayList<Integer>();
      score = 0;
      
      lastPlayedCard = -1;
      displayingBlack = false;
      isPlayer = true; //??
   }
   
   public static boolean hasClient(String name)
   {
      return clients.containsKey(name);
   }
   
   public static Client lookup(String name)
   {
      return clients.get(name);
   }
   
   public static void deleteClient(Client client)
   {
      clients.remove(client.getID());
   }
   
   public String getID()
   {
      return clientID;
   }
   
   public void setEventListener(ClientEventListener listener)
   {
      this.listener = listener;
   }
   
   public int lastPlayedCard()
   {
      return lastPlayedCard;
   }
   
   public boolean isDisplayingBlack()
   {
      return displayingBlack;
   }
   
   public boolean isJudge()
   {
      return !isPlayer;
   }
   
   public boolean isPlayer()
   {
      return isPlayer;
   }
   
   public void setHand(int[] hand)
   {
      this.hand = new ArrayList<Integer>();
      for(int i: hand)
         this.hand.add(i);
   }
   
   public void setHand(List<Integer> hand)
   {
      this.hand = new ArrayList<Integer>();
      for(int i: hand)
         this.hand.add(i);
   }
   
   public int[] getHand()
   {
      int[] out = new int[hand.size()];
      for(int i = 0; i < out.length; i++)
         out[i] = hand.get(i);
      return out;
   }
   
   public void dealCard(int card)
   {
      hand.add(card);
   }
   
   /**
    * Client is no longer a judge, and now is a player 
    */
   public void displayHand()
   {
      isPlayer = true;
      displayingBlack = false;
      String out = "";
      for(Integer c: hand)
         out += '|' + c.toString();
      Log.conn(lastConn,
            Key.pickingMode.toString() + '|' + Key.hand.toString() + out
            );
   }
   
   public int getHandSize()
   {
      return hand.size();
   }
   
   public void setJudge(int card)
   {
      isPlayer = false;
      displayingBlack = true;
      Log.conn(lastConn,
            Key.pickingMode.toString() + '|' + Key.blackCard.toString() + '|' + card
            );
   }
   
   public void displayCard(int white)
   {
      //isPlayer = true; //Implied
      displayingBlack = false;
      Log.conn(lastConn,
            Key.showMode.toString() + '|' + Key.whiteCard.toString() + '|' + white
            );
   }
   
   public void displayCards(int white, int black)
   {
      //isPlayer = true; //Implied
      displayingBlack = true;
      Log.conn(lastConn,
            Key.showMode.toString() + '|' + Key.twoCards.toString() + '|' + black + '|' + white
            );
   }
   
   public void declareRound(boolean winner)
   {
      String out = winner ? Key.tYes.toString() : Key.fNo.toString();
      Log.conn(lastConn,
            Key.winnerMode.toString() + '|' + out
            );
   }
   
   public void displayWait()
   {
      Log.conn(lastConn,
            Key.waitMode.toString()
            );
   }
   
   public void incScore()
   {
      score ++;
      Log.conn(lastConn,
            Key.update.toString() + '|' + Key.scoreUpdate.toString() + '|' + score
            );
   }
   
   public void endGame()//FIXME
   {
      Log.conn(lastConn,
            Key.endMode.toString()
            );
   }
   
   public int getScore()
   {
      return score;
   }
   
   public void setHighScore(int highScore)
   {
      Log.conn(lastConn,
            Key.update.toString() + '|' + Key.highScoreUpdate.toString() + '|' + highScore
            );
   }
   
   public void unexpectedDisconnection()
   {
      connected = false;
      listener.deactivation(this);
   }
   
   public void reconnetion(WebSocket conn)
   {
      connected = true;
      lastConn = conn;
      listener.reactivation(this);
   }
   
   public boolean isConnected()
   {
      return connected;
   }
   
   public boolean hasLeftGame()
   {
      return hasLeft;
   }
   
   public static void routeToClient(String message)
   {
      Log.l("Routing message to client: " + message);
      String[] parts = message.split("[|]");
      for(int i = 0; i < parts.length; i++)
         parts[i] = parts[i].trim();
      if(hasClient(parts[1]))
         lookup(parts[1]).inputRoutedMessage(parts);
   }
   
   private void inputRoutedMessage(String[] parts)
   {
      Log.l("Message routed to client: " + this.getID());
      //0 is gameID
      //1 is clientID
      //2 is mode
      //3+ is data
      
      String mode = parts[2];
      String data = "";
      if(parts.length > 3)
         data = parts[3];
      
      //Picked card from hand to be played
      if(Key.match(Key.pickingMode, mode))
      {
         try
         {
            int card = Integer.parseInt(data);
            lastPlayedCard = card;
            hand.remove((Object) card);
            listener.cardPlayed(this, card);
         }
         catch (NumberFormatException e)
         {
            Log.l("Client sent malformed data in request'" + Key.pickingMode.toString() + "'.");
            e.printStackTrace();
         }
      }
      
      //Card picked by judge
      else if(Key.match(Key.showMode, mode))
      {
         try
         {
            int card = Integer.parseInt(data);
            listener.cardSelected(this, card);
         }
         catch (NumberFormatException e)
         {
            Log.l("Client sent malformed data in request'" + Key.showMode.toString() + "'."); 
            e.printStackTrace();
         }
      }
      
      //Client wishes to leave game
      else if(Key.match(Key.leaving, mode))
      {
         hasLeft = true;
         clients.remove(this.getID());
         listener.exitGame(this);
      }
   }
   
   
}
