
import java.util.concurrent.ExecutionException;

public class IkkyoneLoadTest {
  public static void main(String[] args)
      throws InterruptedException, IllegalAccessException,
          ExecutionException {
    RunClient runClient = new RunClient(args);
    runClient.run();
  }
}