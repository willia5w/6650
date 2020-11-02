import com.sun.javaws.exceptions.InvalidArgumentException;
import java.util.concurrent.ExecutionException;

public class IkkyoneLoadTest {
  public static void main(String[] args)
      throws InterruptedException, IllegalAccessException, InvalidArgumentException,
          ExecutionException {
    RunClient runClient = new RunClient(args);
    runClient.run();
  }
}