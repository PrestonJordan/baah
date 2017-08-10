import java.io.IOException;

public class BAAHserver
{
   
   private static final int SOCKET = 443;
   
   public static void main(String[] args) throws IOException
   {
      new GameServer(SOCKET).start();
   }

}
