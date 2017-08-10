import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RoomName
{
   
   private static final String ADJECTIVES_FILENAME = "config/adjectives.txt";
   private static final String NOUNS_FILENAME = "config/nouns.txt";
   private static List<String> adjectives; 
   private static List<String> nouns;
   private static final List<String> onLoan; 
   private static final Random rand;
   
   
   
   static
   {
      onLoan = new ArrayList<String>();
      rand = new Random();
      try
      {
         adjectives = readFile(ADJECTIVES_FILENAME);
         Log.l("'Adjectives' file loaded for Room Names.");
         nouns = readFile(NOUNS_FILENAME);
         Log.l("'Nouns' file loaded for Room Names.");
      }
      catch (FileNotFoundException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      
   }
         
   private static List<String> readFile(String fileName) throws FileNotFoundException, IOException
   {
      List<String> out = new ArrayList<String>();
      try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
         String line;
         while ((line = br.readLine()) != null) {
            out.add(line);
         }
      }
      return out;
   }
   
   public static String reserveName()
   {
      String out;
      do
      {
         out = adjectives.get(rand.nextInt(adjectives.size())) +
               " " +
               nouns.get(rand.nextInt(nouns.size()));
      }
      while(onLoan.contains(out));
      return out;
   }
   
   public static void unreserveName(String name)
   {
      onLoan.remove(name);
   }
}
