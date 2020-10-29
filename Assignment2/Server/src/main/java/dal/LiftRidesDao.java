package dal;

import connection.ConnectionManager;
import io.swagger.client.model.LiftRide;
import io.swagger.client.model.SkierVertical;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.dbcp2.BasicDataSource;

public class LiftRidesDao {

  private static BasicDataSource dataSource;

  public LiftRidesDao() {
    dataSource = ConnectionManager.getDataSource();
  }

  public void insertLiftRide (LiftRide newLiftRide) throws SQLException {
    
    Connection conn = null;
    PreparedStatement insertStatement = null;
    String insertLiftRideQueryStatement = "INSERT INTO LiftRides(ResortName, DayId, SkierId, StartTime, LiftId, Vertical) "
        + "VALUES(?,?,?,?,?,?);";

    try {
      conn = dataSource.getConnection();
      insertStatement = conn.prepareStatement(insertLiftRideQueryStatement);
      insertStatement.setString(1, newLiftRide.getResortID());
      insertStatement.setInt(2, Integer.parseInt(newLiftRide.getDayID()));
      insertStatement.setInt(3, Integer.parseInt(newLiftRide.getSkierID()));
      insertStatement.setInt(4, Integer.parseInt(newLiftRide.getTime()));
      insertStatement.setInt(5, Integer.parseInt(newLiftRide.getLiftID()));
      insertStatement.setInt(6, 10 * Integer.parseInt(newLiftRide.getLiftID()));
      insertStatement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
        if (insertStatement != null) {
          insertStatement.close();
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

//   get the total vertical for the skier the specified resort.
  public int getTotalVerticalForResort (String skierId, String resortName) throws SQLException {
    int totalVertical = 0;
    SkierVertical skierVertical = new SkierVertical();
    Connection conn = null;
    PreparedStatement selectStmt = null;
    ResultSet results = null;
    String selectVertical = "SELECT SUM(Vertical) AS TotalVertical " +
        "FROM (SELECT Vertical FROM LiftRides " +
        "WHERE ResortName=? AND SkierId=?) AS ResortVert;";

    try {
      conn = dataSource.getConnection();
      selectStmt = conn.prepareStatement(selectVertical);
      selectStmt.setString(1, resortName);
      selectStmt.setInt(  2, Integer.parseInt(skierId));
      results = selectStmt.executeQuery();
      while(results.next()) {
        totalVertical = results.getInt("TotalVertical");
      }
      skierVertical.setTotalVert(totalVertical);
      skierVertical.setResortID(resortName);
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
        if (selectStmt != null) {
          selectStmt.close();
        }
      } catch (SQLException se) {
        se.printStackTrace();
      }
    }
    return totalVertical;
  }

  public int getSkierVerticalForSkiDay (String resortName, int dayId, int skierId) throws SQLException {
    int vertical = 0;
    Connection conn = null;
    PreparedStatement selectStmt = null;
    ResultSet results = null;
    String selectVertical = "SELECT SUM(Vertical) AS TotalVertical " +
        "FROM (SELECT Vertical FROM LiftRides " +
        "WHERE ResortName=? AND DayId=? AND SkierId=?) AS DayVert;";

    try {
      conn = dataSource.getConnection();
      selectStmt = conn.prepareStatement(selectVertical);
      selectStmt.setString(1, resortName);
      selectStmt.setInt(2, dayId);
      selectStmt.setInt(3, skierId);
      results = selectStmt.executeQuery();

      if(results.next()) {
        vertical = results.getInt("TotalVertical");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
        if (selectStmt != null) {
          selectStmt.close();
        }
      } catch (SQLException se) {
        se.printStackTrace();
      }
    }
    return vertical;
  }
}