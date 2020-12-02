import com.sun.javaws.exceptions.InvalidArgumentException;

public class Parser {

  Integer maxThreads;
  Integer numSkiers;
  Integer numSkiLifts;
  Integer skiDay;
  String resortName ;
  String address;
  static final Integer MINALLOWEDLIFTS = 5;
  static final Integer MAXALLOWEDLIFTS= 60;
  static final Integer DEFAULTMAXTHREADS = 256;
  static final Integer DEFAULTMAXSKIERS = 50000;
  static final Integer DEFAULTNUMSKILIFTS = 40;
  static final Integer DEFAULTSKIDAY = 1;
  static final String DEFAULTRESORTNAME = "SilverMt";
  static final String DEFAULTADDRESS = "http://35.165.105.125:8080/Server_war";

  public Parser() {
    this.maxThreads = maxThreads;
    this.numSkiers = numSkiers;
    this.numSkiLifts = numSkiLifts;
    this.skiDay = skiDay;
    this.resortName = resortName;
    this.address = address;
  }
  public Parser(Integer maxThreads, Integer numSkiers, Integer numSkiLifts, Integer skiDay,
      String resortName, String address) {
    this.maxThreads = maxThreads;
    this.numSkiers = numSkiers;
    this.numSkiLifts = numSkiLifts;
    this.skiDay = skiDay;
    this.resortName = resortName;
    this.address = address;
  }

  public Integer getMaxThreads() {
    return maxThreads;
  }

  public void setMaxThreads(Integer maxThreads) {
    this.maxThreads = maxThreads;
  }

  public Integer getNumSkiers() {
    return numSkiers;
  }

  public void setNumSkiers(Integer numSkiers) {
    this.numSkiers = numSkiers;
  }

  public Integer getNumSkiLifts() {
    return numSkiLifts;
  }

  public void setNumSkiLifts(Integer numSkiLifts) {
    this.numSkiLifts = numSkiLifts;
  }

  public Integer getSkiDay() {
    return skiDay;
  }

  public void setSkiDay(Integer skiDay) {
    this.skiDay = skiDay;
  }

  public String getResortName() {
    return resortName;
  }

  public void setResortName(String resortName) {
    this.resortName = resortName;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public static Parser getArgs(String[] commandLineArgs) throws InvalidArgumentException {

    if (commandLineArgs[0].equals("default")) {
      return new Parser(
          DEFAULTMAXTHREADS,
          DEFAULTMAXSKIERS,
          DEFAULTNUMSKILIFTS,
          DEFAULTSKIDAY,
          DEFAULTRESORTNAME,
          DEFAULTADDRESS);
    } else if (Integer.parseInt(commandLineArgs[2]) >= MINALLOWEDLIFTS
        && Integer.parseInt(commandLineArgs[2]) <= MAXALLOWEDLIFTS) {
      return new Parser(
          Integer.parseInt(commandLineArgs[0]),
          Integer.parseInt(commandLineArgs[1]),
          Integer.parseInt(commandLineArgs[2]),
          Integer.parseInt(commandLineArgs[3]),
          commandLineArgs[4],
          commandLineArgs[5]
      );
    } else {
      throw new InvalidArgumentException(commandLineArgs);
    }
  }


}



