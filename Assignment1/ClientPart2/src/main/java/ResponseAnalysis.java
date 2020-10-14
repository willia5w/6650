import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ResponseAnalysis {

  private List<ResponseStat> stats;

  public ResponseAnalysis(List<Future<List<ResponseStat>>> input)
      throws ExecutionException, InterruptedException {
    stats = new ArrayList<>();
    for (Future<List<ResponseStat>> list : input) {
      for (ResponseStat responseStat : list.get()) {
        stats.add(responseStat);
      }
    }
    Collections.sort(stats);
  }

  double meanResponse() {
    long sum = 0;
    for(ResponseStat ResponseStat:stats) {
      sum+=ResponseStat.getLatency();
    }
    return (double)sum/(double)stats.size();
  }
  
  double medianResponse(){
    if(stats.size()%2==1) {
      return stats.get(stats.size()/2).getLatency();
    }
    return (stats.get(stats.size()/2).getLatency() + stats.get(stats.size()/2-1).getLatency())*0.5;
  }
  
  long getNum(){
    return stats.size();
  }
  
  long getP99() {
    return stats.get((int)Math.ceil(stats.size()*0.99)).getLatency();
  }

  long getMax() {
    return stats.get(stats.size()-1).getLatency();
  }

}