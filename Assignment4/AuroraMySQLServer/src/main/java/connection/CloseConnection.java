package connection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CloseConnection {
  public static void close(ConnectionManager connectionManager, Connection connection, PreparedStatement statement, ResultSet results) throws SQLException {
//    if(connection != null) {
//      connectionManager.closeConnection(connection);
//    }
    if(statement != null) {
      statement.close();
    }
    if(results != null) {
      results.close();
    }
  }
}
