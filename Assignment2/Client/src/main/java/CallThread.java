import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import io.swagger.client.model.SkierVertical;
import java.net.SocketTimeoutException;
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
    int success = 0;
    int fail = 0;

    for (int i = 0; i < posts; i++) {
      int lift = ThreadLocalRandom.current().nextInt(1, numSkiLifts + 1);
      int time = ThreadLocalRandom.current().nextInt(startTime, endTime + 1);

      LiftRide liftRide = new LiftRide();
      liftRide.setResortID(resortName);
      liftRide.setDayID(Integer.toString(skiDay));
      liftRide.setSkierID(String.valueOf(ThreadLocalRandom.current().nextInt(minId, maxId + 1)));
      liftRide.setTime(Integer.toString(time));
      liftRide.setLiftID(String.valueOf(lift));
//
      long requestTime = System.currentTimeMillis();
      try {
        ApiResponse<Void> resp = apiInstance.writeNewLiftRideWithHttpInfo(liftRide);
        result.add(
            new ResponseStat(
                requestTime,
                "POST",
                System.currentTimeMillis() - requestTime,
                resp.getStatusCode()));

        success++;
      } catch (ApiException e) {
          e.printStackTrace();
          fail++;
          logger.trace(e);
          e.printStackTrace();
          result.add(
              new ResponseStat(
                  requestTime, "POST", System.currentTimeMillis() - requestTime, e.getCode()));
      }
//     --------------------TESTING--------------------------------------------------
//      ApiResponse<Void> liftResp = null; // response needs to be a JSON like "{\"message\": \"no content\"}"
//      AtomicInteger liftTries = new AtomicInteger(0);
//      while (liftResp == null && liftTries.get() <= gets) {
//        try {
//          liftResp = apiInstance.writeNewLiftRideWithHttpInfo(liftRide);
//
//          result.add(
//                  new ResponseStat(
//                      requestTime,
//                      "POST",
//                      System.currentTimeMillis() - requestTime,
//                      liftResp.getStatusCode()));
//        } catch (ApiException e) {
//          if (e.getCause() instanceof SocketTimeoutException) {
//            liftTries.getAndIncrement();
//            System.out.println("Retrying POST " + liftTries);
//            try {
//              Thread.sleep(1000);
//            } catch (InterruptedException interruptedException) {
//              interruptedException.printStackTrace();
//              fail++;
//              logger.trace(interruptedException);
//              result.add(
//              new ResponseStat(
//                  requestTime, "POST", System.currentTimeMillis() - requestTime, 404));
//            }
//          }
//        }
//      }
    }
//     --------------------TESTING--------------------------------------------------

    for (int j = 0; j < gets; j++) {
      long requestTime = System.currentTimeMillis();
      try {
        ApiResponse<SkierVertical> dayResp =
            apiInstance.getSkierDayVerticalWithHttpInfo(
                resortName,
                String.valueOf(skiDay),
                String.valueOf(ThreadLocalRandom.current().nextInt(minId, maxId + 1)));
        result.add(
            new ResponseStat(
                requestTime,
                "GET",
                System.currentTimeMillis() - requestTime,
                dayResp.getStatusCode()));
        success++;
      } catch (ApiException e) {
        if (e.getCause() instanceof SocketTimeoutException) {
          result.add(retryDayResponse(requestTime));
        } else if (e.getCode() == 400) {
          result.add(
              new ResponseStat(
                  requestTime,
                  "GET",
                  System.currentTimeMillis() - requestTime,
                  e.getCode()));
          success++;
        } else {
          System.out.println("Code : "+ e.getCode());

          fail++;
          logger.trace(e);
          e.printStackTrace();
          result.add(
              new ResponseStat(
                  requestTime, "GET", System.currentTimeMillis() - requestTime, e.getCode()));
        }
      }

// ----------------------------------------------

//    ApiResponse<SkierVertical> resortResp = null; // response needs to be a JSON like "{\"message\": \"no content\"}"
//    List<String> resortList = new ArrayList<>();
//    resortList.add(resortName);
//    AtomicInteger tries = new AtomicInteger(0);
//    while (resortResp == null && tries.get() <= gets) {
//      try {
//        resortResp =
//                apiInstance.getSkierResortTotalsWithHttpInfo(
//                    String.valueOf(ThreadLocalRandom.current().nextInt(minId, maxId + 1)),
//                    resortList);
//      } catch (ApiException e) {
//        if (e.getCause() instanceof SocketTimeoutException) {
//          tries.getAndIncrement();
//          try {
//            Thread.sleep(5000);
//          } catch (InterruptedException interruptedException) {
//            interruptedException.printStackTrace();
//          }
//        }
//      }
    }


    successCount.addAndGet(success);
    failCount.addAndGet(fail);
    countDown.countDown();
    return result;
  }

  private ResponseStat retryDayResponse(long requestTime) {
    ApiResponse<SkierVertical> dayResp = null; // response needs to be a JSON like "{\"message\": \"no content\"}"
    AtomicInteger dayTries = new AtomicInteger(0);
    while (dayResp == null && dayTries.get() <= 5) {
      try {
        dayResp =
            apiInstance.getSkierDayVerticalWithHttpInfo(
                resortName,
                String.valueOf(skiDay),
                String.valueOf(ThreadLocalRandom.current().nextInt(minId, maxId + 1)));

           return new ResponseStat(
                requestTime,
                "GET",
                System.currentTimeMillis() - requestTime,
               dayResp.getStatusCode());
      } catch (ApiException e) {
        if (e.getCause() instanceof SocketTimeoutException) {
          System.out.println("SLEEEEPING");
          dayTries.getAndIncrement();
          try {
            Thread.sleep(3000);
          } catch (InterruptedException ei) {
            ei.printStackTrace();
            failCount.incrementAndGet();
            logger.trace(ei);
            return new ResponseStat(
                    requestTime, "GET", System.currentTimeMillis() - requestTime, e.getCode());
          }
        } else if (e.getCode() == 400) {
          successCount.incrementAndGet();
          return new ResponseStat(
              requestTime, "GET", System.currentTimeMillis() - requestTime, 400);
        }
      }
    }
    return new ResponseStat(
        requestTime, "GET", System.currentTimeMillis() - requestTime, 404);
  }
}