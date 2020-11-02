import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ResponseAnalysis {

  private List<ResponseStat> statsReady;

  public ResponseAnalysis(List<Future<List<ResponseStat>>> input)
      throws ExecutionException, InterruptedException {
    statsReady = new ArrayList<>();
    for (Future<List<ResponseStat>> list : input) {
      for (ResponseStat stat : list.get()) {
    statsReady.add(stat);
      }
    }
    Collections.sort(statsReady);
  }

  double meanResponse() {
    long sum = 0;
    for(ResponseStat stat:statsReady) {
      sum+=stat.getLatency();
    }
    return (double)sum/(double)statsReady.size();
  }
  double medianResponse(){
    if(statsReady.size()%2==1) {
      return statsReady.get(statsReady.size()/2).getLatency();
    }
    return (statsReady.get(statsReady.size()/2).getLatency()
        + statsReady.get(statsReady.size()/2-1).getLatency())*0.5;
  }
  long getNum(){
    return statsReady.size();
  }
  long getP99() {
    return statsReady.get((int)Math.ceil(statsReady.size()*0.99)-1).getLatency();
  }
  long getMax() {
    return statsReady.get(statsReady.size()-1).getLatency();
  }
}

//public class ResponseAnalysis implements Callable<List<ResponseStat>> {
//  private List<Future<List<ResponseStat>>> results;
//  private List<ResponseStat> statsReady;
//  private long wallTime;
//  private Integer maxThreads;
//  private static CountDownLatch latch;
//
//  public ResponseAnalysis(List<Future<List<ResponseStat>>> input, long time, Integer threads, CountDownLatch downLatch) {
//    this.wallTime = time;
//    this.results = input;
//    this.maxThreads = threads;
//    this.latch = downLatch;
//  }
//
//  public void setStatsReady (List<ResponseStat> ready) {
//    this.statsReady = ready;
//  }


//  @Override
//  public List<ResponseStat> call () throws ExecutionException, InterruptedException {
//    List<ResponseStat> stats = new ArrayList<>();
//    for (Future<List<ResponseStat>> list : results) {
//      stats.addAll(list.get());
//    }
//    Collections.sort(stats);
//    this.setStatsReady(stats);
//    this.outputFile();
////    Print Results
//    System.out.println("Mean Response: " + this.meanResponse() + " ms");
//    System.out.println("Median Response: " + this.medianResponse() + " ms");
//    System.out.println("Throughput " + (double)this.getNum()/(double)wallTime + " requests/second");
//    System.out.println("P99 Response: " + this.getP99() + " ms");
//    System.out.println("Max Response: " + this.getMax() + " ms");
//    latch.countDown();
//    return stats;
//  }


//  public void outputFile() {
//    try {
//      FileWriter fileWriter = new FileWriter(String.valueOf(
//          Paths.get("", "TestOutput-" + maxThreads + "Threads.csv")));
//      fileWriter.write("\"Start Time\",\"Type\",\"Latency\",\"Code\"\n");
//      for (Future<List<ResponseStat>> stats : results) { // Skip because redundant
//        for (ResponseStat stat : stats.get()) {
//          fileWriter.write(stat.toString());
//        }
//      }
//      fileWriter.close();
//    } catch (IOException e) {
//      e.printStackTrace();
//    } catch (ExecutionException e) {
//      e.printStackTrace();
//    } catch (InterruptedException e) {
//      e.printStackTrace();
//    } catch (IllegalStateException e) {
//      e.printStackTrace();
//    }

