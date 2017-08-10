
public interface ClientEventListener
{
   /**
    * Called upon unexpected disconnection of client
    * @param source
    */
   public void deactivation(Client source);
   
   /**
    * Called upon reconnection of client after 'deactivation'
    * @param source
    */
   public void reactivation(Client source);
   
   /**
    * Called when player permanently leaves game
    * @param source
    */
   public void exitGame(Client source);
   
   
   
   /**
    * Card selected from hand during Picking round
    * @param source
    * @param card
    */
   public void cardPlayed(Client source, int card);
   
   /**
    * Card selected by judge during judging round
    * @param source
    * @param card
    */
   public void cardSelected(Client source, int card);
}
