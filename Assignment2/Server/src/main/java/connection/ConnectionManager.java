package connection;

import org.apache.commons.dbcp2.BasicDataSource;

public class ConnectionManager {
  /** Basic Data Source connection. */
  private static BasicDataSource dataSource;

//  TODO: store them as system properties in TOMCAT_HOME/conf/catalina.properties like (System.getProperty("MySQL_IP_ADDRESS")
  /** RDS settings for DB Instance Identifier: "database-1". */
  private static final String user = "admin";
  private static final String hostName = "database-1.cx8vo0f7wekn.us-west-2.rds.amazonaws.com";
  private static final String password = "password";

  private static final int port = 3306;
  private static final String schema = "IkkyoneDB";
  private final String timezone = "UTC";

  /** Local MySQL settings. */
//  private static final String user = "root";
//  private static final String hostName = "127.0.0.1";
//  private static final String password = "password";

  static {
    dataSource = new BasicDataSource();
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
//    String JDBC_URL = "jdbc:mysql://" + hostName + ":" + port + "/" + schema + "?useSSL=False";
//    dataSource.setDriverClassName("com.mysql.jdbc.Driver");
    String url = String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC", hostName, port, schema);
    dataSource.setUrl(url);
    dataSource.setUsername(user);
    dataSource.setPassword(password);
//    dataSource.addConnectionProperty("serverTimezone", "UTC");
    dataSource.setInitialSize(10);
    dataSource.setMaxTotal(60);
    dataSource.setMaxIdle(100);
  }


  public static BasicDataSource getDataSource() {
    return dataSource;
  }
}