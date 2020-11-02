public class ResponseStat implements Comparable<ResponseStat>{

  private long startTime;
  private String type;
  private long latency;
  private int responseCode;

  public ResponseStat(long startTime, String type, long latency, int responseCode) {
    this.startTime = startTime;
    this.type = type;
    this.latency = latency;
    this.responseCode = responseCode;
  }

  @Override
  public int compareTo(ResponseStat responseStat) {
    if(this.latency-responseStat.latency>0)
      return 1;
    else if(this.latency-responseStat.latency==0)
      return  0;
    else
      return -1;
  }

  public String getType() { return  type; }

  public long getLatency() {
    return latency;
  }

  @Override
  public String toString() {
    return startTime + "," +
        type + "," +
        latency + "," +
        responseCode + "\n";
  }

}