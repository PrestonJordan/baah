
public class Key
{
   //Client issue commands
   public static final Key joinGame = new Key("JoinGame");
   public static final Key createGame = new Key("CreateGame");
   public static final Key rejoinGame = new Key("RejoinGame");
   public static final Key leaving = new Key("Leaving");
   
   //Server status
   public static final Key noGameFound = new Key("NoGameFound");
   public static final Key gameCreated = new Key("GameCreated");
   public static final Key clientFound = new Key("ClientFound");
   public static final Key noClientFound = new Key("NoClientFound");
   
   //Server issue commands
   public static final Key issueID = new Key("IssueID");
   public static final Key cardSetWhite = new Key("IssueWhiteCardSet");
   public static final Key cardSetBlack = new Key("IssueBlackCardSet");
   
   //Modes
   public static final Key pickingMode = new Key("Picking");
   public static final Key showMode = new Key("Show");
   public static final Key winnerMode = new Key("Winner");
   public static final Key endMode = new Key("End");
   public static final Key waitMode = new Key("Wait");
   
   //constants
   public static final Key hand = new Key("Hand");
   public static final Key whiteCard = new Key("White");
   public static final Key blackCard = new Key("Black");
   public static final Key twoCards = new Key("Two");
   public static final Key tYes = new Key("T");
   public static final Key fNo = new Key("F");
   
   //Updates
   public static final Key update = new Key("Update");
   public static final Key scoreUpdate = new Key("Score");
   public static final Key highScoreUpdate = new Key("HighScore");
   
   private final String selfKey;
   
   private Key(String selfKey)
   {
      this.selfKey = selfKey;
   }
   
   @Override
   public String toString()
   {
      return selfKey;
   }
   
   /**
    * Matches either literal or first part before '|'
    * @param key
    * @param message
    * @return
    */
   public static boolean match(Key key, String message)
   {
      int loc = message.indexOf('|');
      if (loc != -1)
         message = message.substring(0, loc);
      message = message.trim();
      return message.equals(key.toString());
   }
   
}
