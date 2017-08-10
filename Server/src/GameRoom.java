import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Random;

public class GameRoom
{
   private static final int TIME_PLAYED_TO_VIEW = 3 * 1000;
   private static final int TIME_WIN_TO_HAND = 8 * 1000;
   
   //TODO Remove game upon cleanup!!!
   private static final Hashtable<String, GameRoom> games = new Hashtable<String, GameRoom>();
   
   private final String id;
   
   private final Object eventSequence;
   private final ArrayList<Client> activeClients;
   private final ArrayList<Client> inactiveClients;
   private int cardsPlayed;
   private final Deck deck;
   private final Random rand;
   
   private int blackCard;
   private final Hashtable<Client, Integer> judgeAge;
   
   /*
    * T: Picking mode -> Selected -> Display
    * F: Display -> Winner -> Picking mode 
    */
   private boolean cycleMode;
   
   public GameRoom(Deck deck)
   {
      id = RoomName.reserveName();
      Log.l("Game room " + getID() + " created");
      //Log.l("Pre hash table add: " + games);
      games.put(getID(), this);
      //Log.l("Post hash table add: " + games);
      this.deck = deck;
      eventSequence = new Object();
      activeClients = new ArrayList<Client>();
      inactiveClients = new ArrayList<Client>();
      rand = new Random();
      cardsPlayed = 0;
      cycleMode = true;
      
      judgeAge = new Hashtable<Client, Integer>();
   }
   
   public static boolean hasGameRoom(String name)
   {
      name = name.toLowerCase();
      Log.l("Checking for game room '" + name + "' in table: " + games);
      return games.containsKey(name);
   }
   
   public static GameRoom lookup(String name)
   {
      name = name.toLowerCase();
      return games.get(name);
   }
   
   public String getID()
   {
      return id;
   }
   
   public void addClient(Client client)
   {
      Log.l("Client " + client.getID() + " added to game room " + getID());
      synchronized(eventSequence)
      {
         if (activeClients.size() == 0)
         {
            blackCard = deck.drawBlack();
            client.setJudge(blackCard);
            judgeAge.put(client, 0);
         }
         else
         {
            judgeAge.put(client, 1);
         }
         activeClients.add(client);
         client.setHand(deck.drawHand());
         if(client.isPlayer())
         {
            if (cycleMode == true)
            {
               client.displayHand();
            }
            else
            {
               Log.l("Display wait upon connection.");
               client.displayWait();
            }
         }
         
         client.setEventListener(new Listener());
      }
   }
   
   
   
   private class Listener implements ClientEventListener
   {
      
      @Override
      public void deactivation(Client source)
      {
         Log.l("Client " + source.getID() + " unexpectedly disconnected.");
         synchronized(eventSequence)
         {
            //Free
            inactiveClients.add(source);
            activeClients.remove(source);
            
            //Leaving
            clientLeaving(source);
            
         }
         
      }

      @Override
      public void reactivation(Client source)
      {
         Log.l("Client " + source.getID() + " has reconnected.");
         synchronized(eventSequence)
         {
            activeClients.add(source);
            inactiveClients.remove(source);
            
            //Picking round
            if (cycleMode == true)
            {
               source.displayHand();
            }
            
            //Judging round
            else
            {
               //If white card/player
               if(source.isPlayer())
               {
                  Log.l("Display wait after reactivation.");
                  source.displayWait();
               }
               //If judge
               if(source.isJudge())
               {
                  source.setJudge(blackCard); //Display black card
                  for(Client c: activeClients) //Remove duplicate black card
                     if(c.isPlayer() & c.isDisplayingBlack())
                        c.displayCard(c.lastPlayedCard());
               }
            }
         }
         
      }

      @Override
      public void exitGame(Client source)
      {
         Log.l("Client " + source.getID() + " has left the game.");
         synchronized(eventSequence)
         {
            //Free
            activeClients.remove(source);
            deck.discardHand(source.getHand());
            judgeAge.remove(source);
            
            //Leaving
            clientLeaving(source);
         }
      }

      @Override
      public void cardPlayed(Client source, int card)
      {
         Log.l("Client " + source.getID() + " selected a card to be judgged.");
         synchronized(eventSequence)
         {
            //Only picking mode
            if(cycleMode == false)
               return;
            
            if(source.getHandSize() + 1 >= Deck.HAND_SIZE)//Prevent double playing
               cardsPlayed ++;
            if(cardsPlayed == activeClients.size() - 1)
            {
               //Delay time till next cycle
               try
               {
                  Thread.sleep(TIME_PLAYED_TO_VIEW);
               }
               catch (InterruptedException e)
               {
                  e.printStackTrace();
               }
               
               //Gather card list
               ArrayList<Integer> cards = new ArrayList<Integer>();
               for(Client c: activeClients)
                  if(c.isPlayer())
                     cards.add(c.lastPlayedCard());
               Log.l("List of played cards made and filled: " + cards);
               
               //Shuffle list
               Collections.shuffle(cards, new Random(System.nanoTime()));
               Log.l("List of cards shuffled: " + cards);
               
               //Distribute list
               for(Client c: activeClients)
                  if(c.isPlayer())
                  {
                     Log.l("Serving card " + cards.get(0) + " to " + c.getID());
                     c.displayCard(cards.get(0));
                     cards.remove(0);
                  }
               
               //Reset
               cardsPlayed = 0;
               //Next cycle
               cycleMode = false;
            }
         }
         
      }

      @Override
      public void cardSelected(Client source, int card)
      {
         Log.l("Client " + source.getID() + " reported a card as selected by the judge.");
         //Judging round only
         if(cycleMode == true)
            return;
         
         synchronized(eventSequence)
         {
            //Find winner and max value
            int highScore = 0;
            Client winner = null;
            for(Client c: activeClients)
               if(c.isPlayer())
               {
                  if(c.lastPlayedCard() == card)
                  {
                     c.incScore(); //Updates score for each client
                     winner = c;
                  }
                  deck.discardWhite(c.lastPlayedCard());
                  highScore = Math.max(highScore, c.getScore());
               }
            
            //Updates max value and displays winner status
            for(Client c: activeClients)
            {
               c.setHighScore(highScore);
               c.declareRound(c == winner);
            }
            
            //Delay time till next cycle
            try
            {
               Thread.sleep(TIME_WIN_TO_HAND);
            }
            catch (InterruptedException e)
            {
               e.printStackTrace();
            }
            
            //Prepare for next round
            
            //TODO end game if score limit reached
            
            //Get new black card
            deck.discardBlack(blackCard);
            blackCard = deck.drawBlack();
            
            //Deal into hands
            for(Client c: activeClients)
               while(c.getHandSize() < Deck.HAND_SIZE)
                  c.dealCard(deck.drawWhite());
            
            //Select judge
            Client judge = getJudge();
            
            //Set hands/display for next round
            for(Client c: activeClients)
            {
               if (c == judge)
                  judge.setJudge(blackCard);
               else
                  c.displayHand();
            }
            
            
            //Begin next cycle
            cycleMode = true;
         }
      }

   }
   
   private Client getJudge()
   {
      //Select judge
      Client judge = null;
      int oldest = -1;
      for(Client c: activeClients)
      {
         if(judgeAge.get(c) > oldest)
         {
            oldest = judgeAge.get(c);
            judge = c;
         }
      }
      //Reset judge age, increment rest
      judgeAge.put(judge, 0);
      for(Client c: activeClients)
         if(c != judge)
            judgeAge.put(c, judgeAge.get(c) + 1);
      return judge;
   }
   
   private void clientLeaving(Client source)
   {
      //If there are people, continue
      if(activeClients.size() > 0)
      {
      
         //Selection round
         if (cycleMode == true)
         {
            if(source.isJudge())
            {
               Client judge = getJudge();
               judge.setJudge(blackCard);
            }
         }
         
         //Judging round
         else
         {
            //If displaying black
            if(source.isDisplayingBlack())
            {
               Client c = activeClients.get(rand.nextInt(activeClients.size()));
               c.displayCards(c.lastPlayedCard(), blackCard);
            }
            
            //If player with white card
            if(source.isPlayer())
            {
               deck.discardWhite(source.lastPlayedCard());
            }
            
            //If one person is left is judge, then change mode 
            if(activeClients.size() == 1 & activeClients.get(0).isJudge())
            {
               cycleMode = true;
               deck.discardBlack(blackCard);
               blackCard = deck.drawBlack();
               activeClients.get(0).setJudge(blackCard);
            }
         }
      
      }
      //If there are no people, end game
      else
      {
         cleanupAndClose();
      }
   }
   
   private void cleanupAndClose()
   {
      //Remove clients
      for(Client c: inactiveClients)
         Client.deleteClient(c);
      
      //Remove game room
      games.remove(getID());
      
      //Release game name
      RoomName.unreserveName(getID());
      
      Log.l("Game room '" + getID() + "' has been destroyed.");
   }
   
   
}
