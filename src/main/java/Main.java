import java.util.ArrayList;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
public class Main {
  public static void main(String[] args) {
    boolean running = true;
    ArrayList<Task> tasks = new ArrayList<Task>();
    NetworkTable.setClientMode();
    NetworkTable.setTeam(6644);
    NetworkTable.initialize();
    tasks.add(new DetectSwitch());
    for (int i = 0; i < tasks.size(); i++) {
      tasks.get(i).initialize();
    }
    while (running) {
      for (int i = 0; i < tasks.size(); i++)
        tasks.get(i).execute();
    }
  }
}