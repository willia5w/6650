package connection;

import org.apache.commons.dbcp2.BasicDataSource;

public class ConnectionManager {
  /** Basic Data Source connection. */
  private static BasicDataSource dataSource;

//  TODO: store them as system properties in TOMCAT_HOME/conf/catalina.properties like (System.getProperty("MySQL_IP_ADDRESS")
  /** RDS settings for DB Instance Identifier: "ikkyonedb". */
  private static final String user = "IkkyoneDBAdmin";
  private static final String hostName = "ikkyonedb.cfojihtgnbpc.us-west-2.rds.amazonaws.com";
  private static final String password = "IkkyoneDBPassword";

  private static final int port = 3306;
  private static final String schema = "IkkyoneDB";
  private final String timezone = "UTC";

  static {
    dataSource = new BasicDataSource();
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

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