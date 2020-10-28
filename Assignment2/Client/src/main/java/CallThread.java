import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class CallThread implements Callable<List<ResponseStat>> {

  private final Logger logger = (Logger) LogManager.getLogger(CallThread.class);

  private static CountDownLatch countDown;
  private AtomicInteger successCount;
  private AtomicInteger failCount;

  private SkiersApi apiInstance;
  private ApiClient client;
  private int minId;
  private int maxId;
  private int startTime;
  private int endTime;
  private int numSkiLifts;
  private int posts;
  private int gets;
  private int skiDay;
  private String resortName;

  public CallThread(CountDownLatch countDown, AtomicInteger successCount,
      AtomicInteger failCount, String urlBase, int minId, int maxId, int startTime,
      int endTime, int posts, int gets, int numSkiLifts, int skiDay, String resortName) {
    this.countDown = countDown;
    this.successCount = successCount;
    this.failCount = failCount;
    this.minId = minId;
    this.maxId = maxId;
    this.startTime = startTime;
    this.endTime = endTime;
    this.posts = posts;
    this.gets = gets;
    this.numSkiLifts = numSkiLifts;
    this.skiDay = skiDay;
    this.resortName = resortName;

    this.apiInstance = new SkiersApi();
    this.client = apiInstance.getApiClient();
    client.setBasePath(urlBase);
  }

  @Override
  public List<ResponseStat> call() {
    List<ResponseStat> result = new ArrayList<>();
    int success = 0;
    int fail = 0;


    for (int i=0; i<posts; i++) {
      int lift = ThreadLocalRandom.current().nextInt(1, numSkiLifts + 1);
      int time = ThreadLocalRandom.current().nextInt(startTime, endTime + 1);

      LiftRide liftRide = new LiftRide();
      liftRide.setResortID(resortName);
      liftRide.setDayID(Integer.toString(skiDay));
      liftRide.setSkierID(String.valueOf(ThreadLocalRandom.current().nextInt(minId, maxId + 1)));
      liftRide.setTime(Integer.toString(time));
      liftRide.setLiftID(String.valueOf(lift));

      long requestTime = System.currentTimeMillis();
      try {
        ApiResponse<Void> resp = apiInstance
            .writeNewLiftRideWithHttpInfo(liftRide);

        result.add(new ResponseStat(requestTime, "POST", System.currentTimeMillis() - requestTime,
            resp.getStatusCode()));

        success++;
      } catch (ApiException e) {
        fail++;
//        System.out.println(e.getCode());  // Throwing 500 code
        logger.trace(e);
        e.printStackTrace();
        result.add(
            new ResponseStat(requestTime, "POST",System.currentTimeMillis() - requestTime, e.getCode()));
      }
    }

//    for (int j=0; j<gets; j++) {
//      long requestTime = System.currentTimeMillis();
//      try {
//        ApiResponse<SkierVertical> resp = apiInstance.getSkierDayVerticalWithHttpInfo(resortName, String.valueOf(skiDay),
//            String.valueOf(ThreadLocalRandom.current().nextInt(minId, maxId + 1)));
//        result.add(new ResponseStat(requestTime, "GET", System.currentTimeMillis() - requestTime,
//            resp.getStatusCode()));
//        success++;
//      } catch (ApiException e) {
//        fail++;
//        logger.trace(e);
//        e.printStackTrace();
//        result.add(
//            new ResponseStat(requestTime, "GET",System.currentTimeMillis() - requestTime, e.getCode()));
//      }
//    }
    successCount.addAndGet(success);
    failCount.addAndGet(fail);
    countDown.countDown();
    return result;
  }
}
