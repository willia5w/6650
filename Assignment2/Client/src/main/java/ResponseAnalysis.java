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

