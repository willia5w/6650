package connection;

import org.apache.commons.dbcp2.BasicDataSource;

public class ConnectionManager {
  /** Basic Data Source connection. */
  private static BasicDataSource dataSource;


//  private static final String hostName = "ikkyonedb.cfojihtgnbpc.us-west-2.rds.amazonaws.com";  // RDS
private static final String hostName = "ikkyone-aurora-serverless.cluster-cfojihtgnbpc.us-west-2.rds.amazonaws.com";  // Aurora Serverless ikkyone-aurora-cluster-instance-1.cfojihtgnbpc.us-west-2.rds.amazonaws.com
//private static final String hostName = "ikkyone-aurora-cluster.cluster-cfojihtgnbpc.us-west-2.rds.amazonaws.com";  // Aurora
  private static final String user = "admin";
  private static final String password = "password";

  private static final int port = 3306;
  private static final String schema = "IkkyoneAuroraDB";
  private final String timezone = "UTC";

  static {
    dataSource = new BasicDataSource();
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

//    String url = String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC", hostName, port, schema);
    String url = String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC", hostName, port, schema);
    dataSource.setUrl(url);
    dataSource.setUsername(user);
    dataSource.setPassword(password);
//    dataSource.addConnectionProperty("serverTimezone", "UTC");
    dataSource.setInitialSize(10);
    dataSource.setMaxTotal(60);  // Changed from 100, Free RDS MAX
    dataSource.setMaxIdle(60);
  }


  public static BasicDataSource getDataSource() {
    return dataSource;
  }
}