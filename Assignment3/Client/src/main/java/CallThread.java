import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import io.swagger.client.model.SkierVertical;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class CallThread implements Callable<List<ResponseStat>> {

  private static final int SC_BAD_REQUEST = 400;
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

  public CallThread(
      CountDownLatch countDown,
      AtomicInteger successCount,
      AtomicInteger failCount,
      String urlBase,
      int minId,
      int maxId,
      int startTime,
      int endTime,
      int posts,
      int gets,
      int numSkiLifts,
      int skiDay,
      String resortName) {
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

    for (int i = 0; i < posts; i++) {
      LiftRide liftRide = new LiftRide();  // Too expensive to make here?
      liftRide.setResortID(resortName);
      liftRide.setDayID(Integer.toString(skiDay));
      liftRide.setSkierID(String.valueOf(ThreadLocalRandom.current().nextInt(minId, maxId + 1)));
      liftRide.setTime(Integer.toString(ThreadLocalRandom.current().nextInt(startTime, endTime + 1)));
      liftRide.setLiftID(String.valueOf(ThreadLocalRandom.current().nextInt(1, numSkiLifts + 1)));
      result.add(tryPost(liftRide));
    }

//    for (int j = 0; j < gets; j++) {
//      result.add(tryDayResponse());
//      result.add(tryResortResponse());
//    }

    countDown.countDown();
    return result;
  }


  private ResponseStat tryPost(LiftRide liftRide) {
    ResponseStat returnRes = null;
    AtomicInteger attempts = new AtomicInteger();
    long requestTime = System.currentTimeMillis();
    while (returnRes == null && attempts.get()<=5) {
      try {
        ApiResponse<Void> postResp = apiInstance.writeNewLiftRideWithHttpInfo(liftRide);
        successCount.incrementAndGet();
        returnRes = new ResponseStat(
            requestTime, "POST", System.currentTimeMillis() - requestTime, postResp.getStatusCode());
      } catch (ApiException e) {
        if (e.getCause() instanceof SocketTimeoutException) {
          try{
            Thread.sleep(2000 * attempts.getAndIncrement());  // MAX = 10,0000ms
          } catch (InterruptedException i) {
            i.printStackTrace();
          }
        } else {
          e.printStackTrace();
          failCount.incrementAndGet();
          logger.trace(e);
          returnRes = new ResponseStat(
              requestTime,
              "POST",
              System.currentTimeMillis() - requestTime,
              404);
        }
      }
    }
    return returnRes;
  }


  private ResponseStat tryDayResponse() {
    ResponseStat returnRes = null;
    AtomicInteger attempts = new AtomicInteger();
    long requestTime = System.currentTimeMillis();
    while (returnRes == null && attempts.get()<=5) {
      try {
        ApiResponse<SkierVertical> dayResp =
            apiInstance.getSkierDayVerticalWithHttpInfo(
                resortName,
                String.valueOf(skiDay),
                String.valueOf(ThreadLocalRandom.current().nextInt(minId, maxId + 1)));
        successCount.incrementAndGet();
        returnRes = new ResponseStat(
            requestTime,
            "GET",
            System.currentTimeMillis() - requestTime,
            dayResp.getStatusCode());
      } catch (ApiException e) {
        if (e.getCause() instanceof SocketTimeoutException) {
          try{
            Thread.sleep(2000 * attempts.getAndIncrement());  // MAX = 10,0000ms
            System.out.println(attempts.get());
          } catch (InterruptedException i) {
            returnRes = new ResponseStat(
                requestTime,
                "GET",
                System.currentTimeMillis() - requestTime,
                404);
          }
        } else if (e.getCode() == SC_BAD_REQUEST) {
          successCount.incrementAndGet();
          returnRes = new ResponseStat(
              requestTime,
              "GET",
              System.currentTimeMillis() - requestTime,
              SC_BAD_REQUEST);
        } else {
          e.printStackTrace();
          failCount.incrementAndGet();
          logger.trace(e);
          returnRes = new ResponseStat(
              requestTime,
              "GET",
              System.currentTimeMillis() - requestTime,
              404);
        }
      }
    }
    return returnRes;
  }


  private ResponseStat tryResortResponse() {
    ResponseStat returnRes = null;
    AtomicInteger attempts = new AtomicInteger();
    long requestTime = System.currentTimeMillis();
    while (returnRes == null && attempts.get()<=5) {
      try {
      ApiResponse<SkierVertical> resortResp =
          apiInstance.getSkierResortTotalsWithHttpInfo(
              String.valueOf(ThreadLocalRandom.current().nextInt(minId, maxId + 1)),
              Collections.singletonList(resortName));
      successCount.incrementAndGet();
      returnRes = new ResponseStat(
          requestTime,
          "GET",
          System.currentTimeMillis() - requestTime,
          resortResp.getStatusCode());
    } catch (ApiException e) {
      if (e.getCause() instanceof SocketTimeoutException) {
        try{
          Thread.sleep(2000 * attempts.getAndIncrement());  // MAX = 10,0000ms
          System.out.println(attempts.get());
        } catch (InterruptedException i) {
          returnRes = new ResponseStat(
              requestTime,
              "GET",
              System.currentTimeMillis() - requestTime,
              404);
        }
      } else if (e.getCode() == SC_BAD_REQUEST) {
        successCount.incrementAndGet();
        returnRes = new ResponseStat(
            requestTime,
            "GET",
            System.currentTimeMillis() - requestTime,
            SC_BAD_REQUEST);
      } else {
        e.printStackTrace();
        failCount.incrementAndGet();
        logger.trace(e);
        returnRes = new ResponseStat(
            requestTime,
            "GET",
            System.currentTimeMillis() - requestTime,
            404);
      }
    }
  }
  return returnRes;
}
}