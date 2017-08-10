import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CardParser
{
   
   private final List<String> whiteCards;
   private final List<String> blackCards;
   
   private static final boolean BLACK_CARDS_FIRST = true;
   private static final String BLANK_REPLACE = "______";
   
   public CardParser(String filePath) throws IOException
   {
      
      whiteCards = new ArrayList<String>();
      blackCards = new ArrayList<String>();
      
      boolean fillBlack = BLACK_CARDS_FIRST;
      
      try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
         String line;
         while ((line = br.readLine()) != null) {
            
            
            if(line.length() == 0)
               fillBlack = !fillBlack;
            else if(fillBlack)
            {
               //Only 1 space or less black cards allowed
               if(line.length() - line.replace("_",  "").length() <= 1)
               {
                  line = line.replace("_", BLANK_REPLACE);
                  blackCards.add(line);
               }
            }
            else
            {
               line = line.replace("_", BLANK_REPLACE);
               whiteCards.add(line);
            }
            
         }
      }
      
   }
   
   public List<String> getBlackCards()
   {
      return copy(blackCards);
   }
   
   public List<String> getWhiteCards()
   {
      return copy(whiteCards);
   }
   
   
   private List<String> copy(List<String> list)
   {
      List<String> out = new ArrayList<String>();
      for(String s: list)
         out.add(s);
      return out;
   }
}
