import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Deck
{

   private final int whiteSize;
   private int whiteExpansions;
   private ArrayList<Integer> white;
   private ArrayList<Integer> whiteDiscard;
   private ArrayList<Integer> black;
   private ArrayList<Integer> blackDiscard;

   public static final int HAND_SIZE = 7;
   
   public Deck(int whiteSize, int blackSize)
   {
      this.whiteSize = whiteSize;
      whiteExpansions = 0;
      white = new ArrayList<Integer>();
      whiteDiscard = new ArrayList<Integer>();
      black = new ArrayList<Integer>();
      blackDiscard = new ArrayList<Integer>();
      populateWhite();
      for(int i = 0; i < blackSize; i++)
         black.add(i);
      shuffle(white);
      shuffle(black);
   }
   
   public int[] drawHand()
   {
      int[] out = new int[HAND_SIZE];
      for(int i = 0; i < out.length; i++)
         out[i] = drawWhite();
      return out;
   }
   
   public int drawWhite()
   {
      if(white.isEmpty())
      {
         if(whiteDiscard.isEmpty())
         {
            populateWhite();
         }
         else
         {
            white = whiteDiscard;
            shuffle(white);
            whiteDiscard = new ArrayList<Integer>();
         }
      }
      return white.remove(0);
   }
   
   public void discardWhite(int card)
   {
      whiteDiscard.add(card);
   }
   
   public void discardHand(int[] hand)
   {
      for(int c: hand)
         discardWhite(c);
   }
   
   public int drawBlack()
   {
      if(black.isEmpty())
      {
         black = blackDiscard;
         shuffle(black);
         blackDiscard = new ArrayList<Integer>();
      }
      return black.remove(0);
   }
   
   public void discardBlack(int card)
   {
      blackDiscard.add(card);
   }
   
   private void populateWhite()
   {
      for(int i = whiteSize * whiteExpansions; i < whiteSize * (whiteExpansions + 1); i++)
         white.add(i);
      whiteExpansions += 1;
   }
   
   private void shuffle(@SuppressWarnings("rawtypes") ArrayList list)
   {
      Collections.shuffle(list, new Random(System.nanoTime()));//Might pose an issue...
   }
   
}
