
import java.util.ArrayList;

public class Main {
  public static void main(String[] args) {
   boolean running = true;
   ArrayList<Task> tasks = new ArrayList<Task>();
   for(int i = 0 ; i < tasks.size() ; i++){
    tasks.get(i).initialize();
   }
   while(running){
     for(int i = 0 ; i < tasks.size() ; i++)
    tasks.get(i).execute();
   }
  }
}