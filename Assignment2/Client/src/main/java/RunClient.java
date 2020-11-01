import static java.lang.Thread.MAX_PRIORITY;

import com.sun.javaws.exceptions.InvalidArgumentException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class RunClient {

  static final int NUMPOSTS = 1000;

  public static void main(String[] args) throws InterruptedException, IllegalAccessException, InvalidArgumentException {

    Parser parameters = Parser.getArgs(args);
    Integer maxThreads = parameters.getMaxThreads();
    Integer numSkiers = parameters.getNumSkiers();
    Integer numSkiLifts = parameters.getNumSkiLifts();
    Integer skiDay = parameters.getSkiDay();
    String resortName = parameters.getResortName();
    String address = parameters.getAddress();

    AtomicInteger succeeded = new AtomicInteger(0);
    AtomicInteger failed = new AtomicInteger(0);

    long startTimer = System.currentTimeMillis()/1000;

    int phaseOneThreads = maxThreads/4;
    int phaseTwoThreads = maxThreads;
    int phaseThreeThreads = maxThreads/4;

    ExecutorService pool = Executors
        .newFixedThreadPool(phaseOneThreads + phaseTwoThreads + phaseThreeThreads);
    List<Future<List<ResponseStat>>> response = new ArrayList<>();

    int phaseOneStart = 1;
    int phaseOneEnd = 90;
    int phaseOneSkierIDRange = numSkiers/phaseOneThreads;
    int phaseOneGets = 5;

    int phaseTwoStart = 91;
    int phaseTwoEnd = 360;
    int phaseTwoSkierIDRange = numSkiers/phaseTwoThreads;
    int phaseTwoGets = 5;

    int phaseThreeStart = 361;
    int phaseThreeEnd = 420;
    int phaseThreeSkierIDRange = numSkiers/phaseThreeThreads;
    int phaseThreeGets = 10;

    try {
      System.out.println("Phase 1 Start");
    runPhase(numSkiLifts, skiDay, resortName, address, succeeded, failed, phaseOneThreads, pool,
        response, phaseOneSkierIDRange, phaseOneStart, phaseOneEnd, NUMPOSTS, phaseOneGets);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    try {
      System.out.println("Phase 2 Start");
    runPhase(numSkiLifts, skiDay, resortName, address, succeeded, failed, phaseTwoThreads, pool,
        response, phaseTwoSkierIDRange, phaseTwoStart, phaseTwoEnd, NUMPOSTS, phaseTwoGets);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    try {
      System.out.println("Phase 3 Start");
    runPhase(numSkiLifts, skiDay, resortName, address, succeeded, failed, phaseThreeThreads, pool,
        response, phaseThreeSkierIDRange, phaseThreeStart, phaseThreeEnd, NUMPOSTS,
        phaseThreeGets);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    pool.shutdown();

    try {
      pool.awaitTermination(MAX_PRIORITY, TimeUnit.HOURS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    long endTimer = System.currentTimeMillis()/1000;
    long wallTime = endTimer - startTimer;
    long throughput = succeeded.intValue()/wallTime;

    try {
      ResponseAnalysis ResponseAnalysis = new ResponseAnalysis(response);
      System.out.println("Mean Response: " + ResponseAnalysis.meanResponse() + " ms");
      System.out.println("Median Response: " + ResponseAnalysis.medianResponse() + " ms");
      System.out.println("Throughput " + (double)ResponseAnalysis.getNum()/(double)wallTime + " requests/second");
      System.out.println("P99 Response: " + ResponseAnalysis.getP99() + " ms");
      System.out.println("Max Response: " + ResponseAnalysis.getMax() + " ms");
    } catch (ExecutionException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (IllegalStateException e) {
      e.printStackTrace();
    }

    System.out.println(maxThreads + " Threads, " + numSkiers + " Skiers, " + numSkiLifts + " Lifts");
    System.out.println("*****************");
    System.out.println("Threads Succeeded: " + succeeded);
    System.out.println("Threads Failed: " + failed);
    System.out.println("Wall Time: " + wallTime + " seconds");
    System.out.println("Throughput: " + throughput + " requests per second");
    System.out.println("*****************");

    try {
      FileWriter fileWriter = new FileWriter(String.valueOf(
          Paths.get("", "TestOutput-" + maxThreads + "Threads.csv")));
      fileWriter.write("\"Start Time\",\"Type\",\"Latency\",\"Code\"\n");
      for (Future<List<ResponseStat>> list : response) {
        for (ResponseStat stat : list.get()) {
          fileWriter.write(stat.toString());
        }
      }
      fileWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (IllegalStateException e) {
      e.printStackTrace();
    }
  }

  private static void runPhase(
      Integer numSkiLifts,
      Integer skiDay,
      String resortName,
      String address,
      AtomicInteger succeeded,
      AtomicInteger failed,
      int phaseThreads,
      ExecutorService pool,
      List<Future<List<ResponseStat>>> response,
      int phaseSkierIDRange,
      int phaseStart,
      int phaseEnd,
      int phasePosts,
      int phaseGets) throws InterruptedException {

    System.out.println("Phase Start " + phaseStart);

    try {
      int phaseMin = 1;
      final CountDownLatch phaseLatch = new CountDownLatch((int)Math.ceil(phaseThreads/10f));
      for (int i = 0; i < phaseThreads; i++) {
        Callable<List<ResponseStat>> newPhaseThread = new CallThread(
            phaseLatch,
            succeeded,
            failed,
            address,
            phaseMin,
            phaseMin + phaseSkierIDRange-1,
            phaseStart,
            phaseEnd,
            phasePosts,
            phaseGets,
            numSkiLifts,
            skiDay,
            resortName);
        phaseMin+=phaseSkierIDRange;
        response.add(pool.submit(newPhaseThread));
      }
      phaseLatch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
