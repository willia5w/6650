import dal.LiftRidesDao;
import io.swagger.client.JSON;
import io.swagger.client.model.LiftRide;
import io.swagger.client.model.SkierVertical;
import java.io.BufferedReader;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "SkierServlet")
public class SkierServlet extends HttpServlet {

  LiftRidesDao liftRidesDao = new LiftRidesDao();

//  protected LiftRidesDao liftRidesDao;
//  public void init() throws ServletException {
//      liftRidesDao = LiftRidesDao.getInstance();
//  }

  //  TODO: POSTMAN test http://localhost:8080/Server_war_exploded/skiers/liftrides
  //  http://35.165.105.125:8080/Server_war/skiers/liftrides

  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    String urlPath = req.getPathInfo();

    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("{\"message\": \"bad request\"}");
      return;
    }

    String jsonLiftRide = "";
    BufferedReader reader = req.getReader();
    try {
      for (String line; (line = reader.readLine()) != null; jsonLiftRide += line);
//      jsonLiftRide = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
      System.out.println("JSON of LiftRide:\n" + jsonLiftRide);
    } catch(IOException e) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("{\"message\": \"bad request\"}");
    }

    LiftRide liftRide = new LiftRide();

    try {
      JSON json = new JSON();
      json.setLenientOnJson(true);
      liftRide = json.deserialize(jsonLiftRide, LiftRide.class);
    } catch (InvalidParameterException e) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("{\"message\": \"bad request\"}");
      return;
    }

    if (validatePost(urlPath) == PostCase.INVALID) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("{\"message\": \"bad request\"}");
    } else {
      try {
        liftRidesDao.insertLiftRide(liftRide);
        res.setStatus(HttpServletResponse.SC_CREATED);
      } catch (SQLException e) {
        res.setStatus(HttpServletResponse.SC_NO_CONTENT);
        res.getWriter().write("{\"message\": \"no content\"}");
      }
    }
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {

    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    String urlPath = req.getPathInfo();

//    String queryString = req.getQueryString();

    if (validateGet(urlPath) == GetCase.RESORT) {
      res.setStatus(HttpServletResponse.SC_OK);
      String[] urlParts = urlPath.split("/");

      System.out.println("Resort URL Parts:\n" + urlPath);

      String skierId = urlParts[1];
      String queryResort = req.getParameter("resort");
      SkierVertical skierVertical = new SkierVertical();
      skierVertical.setResortID(queryResort);
      try {
        int vert = liftRidesDao.getTotalVerticalForResort(skierId, queryResort);
        skierVertical.setTotalVert(vert);
      } catch (SQLException e) {
        res.setStatus(HttpServletResponse.SC_NO_CONTENT);
        res.getWriter().write("{\"message\": \"no content\"}");
      }

      JSON json = new JSON();
      json.setLenientOnJson(true);
      res.getWriter().write(skierVertical.toString());

    } else if (validateGet(urlPath) == GetCase.DAY) {
      res.setStatus(HttpServletResponse.SC_OK);
      String[] urlParts = urlPath.split("/");

      System.out.println("Day URL Parts:\n" + urlPath);

      String resortName = urlParts[1];

      int dayId = Integer.valueOf(urlParts[5]);
      int skierId = Integer.valueOf(urlParts[7]);
      SkierVertical skierVertical = new SkierVertical();
      skierVertical.setResortID(resortName);
      try {
        int vertical = liftRidesDao.getSkierVerticalForSkiDay(resortName, dayId, skierId);
        res.getWriter().write(String.valueOf(vertical));
      } catch (SQLException e) {
        res.setStatus(HttpServletResponse.SC_NO_CONTENT);
        res.getWriter().write("{\"message\": \"no content\"}");
      }

    } else {  // Invalid Case
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("{\"message\": \"not found\"}");
    }
  }


  enum GetCase {
    RESORT, DAY, INVALID;
  }

  private GetCase validateGet(String urlPath) {
    if (Pattern.matches("/\\w+/vertical", urlPath)) {
      return GetCase.RESORT;
    } else if (Pattern.matches("/\\w+/days/\\d+/skiers/\\w+", urlPath)) {
      return GetCase.DAY;
    } else {
      return GetCase.INVALID;
    }
  }

  enum PostCase {
    RIDE, INVALID;
  }

  private PostCase validatePost(String urlPath) {
    if (Pattern.matches("/liftrides", urlPath)) {
      return PostCase.RIDE;
    } else {
      return PostCase.INVALID;
    }
  }
}

