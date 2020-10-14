import static java.lang.Thread.MAX_PRIORITY;

import com.sun.javaws.exceptions.InvalidArgumentException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class RunClient {

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

    int phaseOneSkierIDRange = numSkiers/phaseOneThreads;
    int phaseOneStart = 1;
    int phaseOneEnd = 90;
    int phaseOnePosts = 100;
    int phaseOneGets = 5;

    try {
      int phaseOneMin = 1;
      final CountDownLatch phaseOneLatch = new CountDownLatch((int)Math.ceil(phaseOneThreads/10f));
      for (int i = 0; i < phaseOneThreads; i++) {
        Runnable newPhaseOneThread = new RunThread(phaseOneLatch, succeeded,
            failed, address, phaseOneMin, phaseOneMin + phaseOneSkierIDRange-1, phaseOneStart,
            phaseOneEnd, phaseOnePosts, phaseOneGets, numSkiLifts, skiDay, resortName);
        pool.execute(newPhaseOneThread);
      }
      phaseOneLatch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    int phaseTwoStart = 91;
    int phaseTwoEnd = 360;
    int phaseTwoSkierIDRange = numSkiers/phaseTwoThreads;
    int phaseTwoPosts = 100;
    int phaseTwoGets = 5;

    try {
      int phaseTwoMin = 1;
      final CountDownLatch phaseTwoLatch = new CountDownLatch((int)Math.ceil(phaseTwoThreads/10f));
      for (int i = 0; i < phaseTwoThreads; i++) {
        Runnable newPhaseTwoThread = new RunThread(phaseTwoLatch, succeeded,
            failed, address, phaseTwoMin, phaseTwoMin + phaseTwoSkierIDRange-1, phaseTwoStart,
            phaseTwoEnd, phaseTwoPosts, phaseTwoGets, numSkiLifts, skiDay, resortName);
        pool.execute(newPhaseTwoThread);
      }
      phaseTwoLatch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    int phaseThreeStart = 361;
    int phaseThreeEnd = 420;
    int phaseThreeSkierIDRange = numSkiers/phaseThreeThreads;
    int phaseThreePosts = 100;
    int phaseThreeGets = 10;

    try {
      int phaseThreeMin = 1;
      final CountDownLatch phaseThreeLatch = new CountDownLatch((int)Math.ceil(phaseThreeThreads/10f));
      for (int i = 0; i < phaseThreeThreads; i++) {
        Runnable newPhaseThreeThread = new RunThread(phaseThreeLatch, succeeded,
            failed, address, phaseThreeMin, phaseThreeMin + phaseThreeSkierIDRange-1, phaseThreeStart,
            phaseThreeEnd, phaseThreePosts, phaseThreeGets, numSkiLifts, skiDay, resortName);
        pool.execute(newPhaseThreeThread);
      }
      phaseThreeLatch.await();
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

    System.out.println(maxThreads + " Threads, " + numSkiers + " Skiers, " + numSkiLifts + " Lifts");
    System.out.println("*****************");
    System.out.println("Threads Succeeded: " + succeeded);
    System.out.println("Threads Failed: " + failed);
    System.out.println("Wall Time: " + wallTime + " seconds");
    System.out.println("Throughput: " + throughput + " requests per second");
    System.out.println("*****************");
  }
}
