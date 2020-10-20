//import com.google.gson.Gson;
//import java.util.stream.Collectors;
//import model.*;
import java.io.IOException;
import java.io.StringWriter;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "SkierServlet")
public class SkierServlet extends HttpServlet {

  static final Integer VERT = 10000;
  static final String RESORT = "Vail";

//  TODO: POSTMAN test http://localhost:8080/Server_war_exploded/skiers/liftrides
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    String urlPath = req.getPathInfo();

    if (urlPath == null) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("missing parameters");
      return;
    }

    String jsonLiftRide = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

    if (!isUrlValidLiftRide(urlPath, jsonLiftRide)) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    } else {
      res.setStatus(HttpServletResponse.SC_CREATED);
    }
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {

    String urlPath = req.getPathInfo();
    String[] urlParts = urlPath.split("/");
    res.setContentType("application/json");

    JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();

    if (urlPath == null) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("missing parameters\n" + urlPath + "\n");
    }

    String queryResort = req.getParameter("resort");

    if (!(queryResort == null)) {
      jsonBuilder.add("resortID", queryResort);
      jsonBuilder.add("totalVert", VERT);
      res.setStatus(HttpServletResponse.SC_OK);
    } else if (isUrlValidRecord(urlPath)){
      jsonBuilder.add("resortID", RESORT);
      jsonBuilder.add("totalVert", VERT);
      res.setStatus(HttpServletResponse.SC_OK);
    }  else {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("records not found\n" + urlPath + "\n");
    }

      JsonObject empObj = jsonBuilder.build();
      StringWriter strWtr = new StringWriter();
      JsonWriter jsonWtr = Json.createWriter(strWtr);
      jsonWtr.writeObject(empObj);
      jsonWtr.close();
      res.getWriter().write(strWtr.toString());
    }

    private boolean isUrlValidLiftRide(String urlPath, String body) {
//    LiftRide convertedObject = new Gson().fromJson(jsonLiftRide, LiftRide.class);  // Must import LiftRide from Client to compare
//    if (convertedObject.getLiftID() == null || convertedObject.getTime() == null)
     return urlPath.equals("/liftrides") && body != null;
    }

  private boolean isUrlValidRecord(String urlPath) {
    String[] urlParts = urlPath.split("/");
    if (urlParts.length > 4) {
      return urlParts[2].equals("days") && urlParts[4].equals("skiers");
    } else {
      return false;
    }
  }
}

