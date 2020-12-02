package dal;

import static connection.CloseConnection.close;

import connection.ConnectionManager;
import io.swagger.client.model.ResortsList;
import io.swagger.client.model.ResortsListResorts;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ResortsDao {

  protected ConnectionManager connectionManager;

  private static ResortsDao instance = null;
  protected ResortsDao () {
    connectionManager = new ConnectionManager();
  }
  public static ResortsDao getInstance() {
    if(instance == null) {
      instance = new ResortsDao();
    }
    return instance;
  }

  public ResortsList getAllResorts() throws SQLException {

    ResortsList resortsList = new ResortsList();
    String selectResorts = "SELECT * FROM Resorts;";
    Connection connection = null;
    PreparedStatement selectStmt = null;
    ResultSet results = null;
    try {
//      connection = connectionManager.getConnection();
      selectStmt = connection.prepareStatement(selectResorts);
      results = selectStmt.executeQuery();

      while(results.next()) {
        String resortName = results.getString("ResortName");
        ResortsListResorts r = new ResortsListResorts();
        r.setResortName(resortName);
        resortsList.addResortsItem(r);
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw e;
    } finally {
      close(connectionManager, connection, selectStmt, results);
    }
    return resortsList;
  }

  public ResortsListResorts getResortByResortId(int resortId) throws SQLException {
    ResortsListResorts resort = null;

    String selectResortById =
        "SELECT * FROM Resorts WHERE ResortId=?;";
    Connection connection = null;
    PreparedStatement selectStmt = null;
    ResultSet results = null;
    try {
//      connection = connectionManager.getConnection();
      selectStmt = connection.prepareStatement(selectResortById);
      selectStmt.setInt(1, resortId);
      results = selectStmt.executeQuery();
      if(results.next()) {
        String resortName = results.getString("ResortName");
        resort = new ResortsListResorts();
        resort.setResortName(resortName);
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw e;
    } finally {
      close(connectionManager,connection,selectStmt,results);
    }
    return resort;
  }
}