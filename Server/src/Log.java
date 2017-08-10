import org.java_websocket.WebSocket;

public class Log
{
   
   public static int LEVEL = 0;
   
   public static void l(int level, String message)
   {
      if(level > LEVEL)
         l(message);
   }
   
   public static void l(String message)
   {
      System.out.println("\tLOGGER: " + message);
   }
   
   public static void conn(WebSocket conn, String data)
   {
      String p = data.substring(0, Math.min(data.length(), 50));
      System.out.println("write: " + p);
      conn.send(data);
   }
   
   public static void recv(String message)
   {
      System.out.println("recieve: " + message);
   }
}
