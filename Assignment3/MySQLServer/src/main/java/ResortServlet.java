import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet(name = "ResortServlet")
public class ResortServlet extends HttpServlet {

  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

    res.setContentType("application/json");
    String urlPath = req.getPathInfo();
    String resort = req.getParameter("resort");
    String day = req.getParameter("dayID");


    if (isValid(urlPath)) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("missing parameters\n" + urlPath + "\n");
    } else if  (resort == null || day == null) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("records not found\n" + urlPath + "\n");
    } else {
      res.setStatus(HttpServletResponse.SC_OK);

      JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
      JsonArrayBuilder plnArrBld = Json.createArrayBuilder();

      List<String> skiers = new ArrayList<String>();  // Empty for now
      for (String skier : skiers) {
        plnArrBld.add(skier);
      }

      JsonArray arr = plnArrBld.build();
      jsonBuilder.add("topTenSkiers", arr);
      JsonObject empObj = jsonBuilder.build();

      StringWriter strWtr = new StringWriter();
      JsonWriter jsonWtr = Json.createWriter(strWtr);

      jsonWtr.writeObject(empObj);
      jsonWtr.close();
      res.getWriter().write(strWtr.toString());
    }
  }

  private boolean isValid (String urlPath) {
    String[] urlParts = urlPath.split("/");
    return urlParts[0].equals("day") && urlParts[1].equals("top10vert");
  }
}
