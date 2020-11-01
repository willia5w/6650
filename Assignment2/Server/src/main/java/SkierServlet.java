import com.fasterxml.jackson.databind.ObjectMapper;
import dal.LiftRidesDao;
import io.swagger.client.model.LiftRide;
import io.swagger.client.model.SkierVertical;
import java.io.IOException;
import java.io.PrintWriter;
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

  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    String urlPath = req.getPathInfo();

    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("{\"message\": \"bad request\"}");
      return;
    }

    if (validatePost(urlPath) == PostCase.INVALID) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("{\"message\": \"bad request\"}");
    } else {
      try {
        ObjectMapper mapper = new ObjectMapper();
        LiftRide liftRide = mapper.readValue(req.getInputStream(), LiftRide.class);
        liftRidesDao.insertLiftRide(liftRide);
        res.setStatus(HttpServletResponse.SC_CREATED);
      } catch (SQLException e) {
        res.setStatus(HttpServletResponse.SC_NO_CONTENT);
        res.getWriter().write("{\"message\": \"no content\"}");
      }
    }
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException, IllegalArgumentException {
    PrintWriter writer = res.getWriter();
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    String urlPath = req.getPathInfo();
    String[] urlParts = urlPath.split("/");


    if (validateGet(urlPath) == GetCase.RESORT) {
      int skierId = Integer.valueOf(urlParts[1]);
      String queryResort = req.getParameter("resort");
      SkierVertical skierVertical = new SkierVertical();
      skierVertical.setResortID(queryResort);
      try {
        int vert = liftRidesDao.getTotalVerticalForResort(skierId, queryResort);
        if (vert > 0) {
          skierVertical.setTotalVert(vert);
          res.setStatus(HttpServletResponse.SC_OK);
          writer.write(skierVertical.toString());
        } else {
          res.setStatus(HttpServletResponse.SC_NO_CONTENT);
          writer.write("{\"message\": \"no content\"}");
        }
      } catch (SQLException e) {
        res.setStatus(HttpServletResponse.SC_NO_CONTENT);
        writer.write("{\"message\": \"no content\"}");
      } catch (IllegalArgumentException ie) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        writer.write("{\"message\": \"illegal argument\"}");
      }
    } else if (validateGet(urlPath) == GetCase.DAY) {
      String resortName = urlParts[1];
      int dayId = Integer.valueOf(urlParts[3]);
      int skierId = Integer.valueOf(urlParts[5]);
      SkierVertical skierVertical = new SkierVertical();
      skierVertical.setResortID(resortName);
      try {
        int vert = liftRidesDao.getSkierVerticalForSkiDay(resortName, dayId, skierId);
        res.setStatus(HttpServletResponse.SC_OK);
        skierVertical.setTotalVert(vert);
        if (vert > 0) {
          skierVertical.setTotalVert(vert);
          res.setStatus(HttpServletResponse.SC_OK);
          writer.write(skierVertical.toString());
        } else {
          res.setStatus(HttpServletResponse.SC_NO_CONTENT);
          writer.write("{\"message\": \"no content\"}");
        }
      } catch (SQLException e) {
        res.setStatus(HttpServletResponse.SC_NO_CONTENT);
        writer.write("{\"message\": \"no content\"}");
      } catch (IllegalArgumentException ie) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        writer.write("{\"message\": \"illegal argument\"}");
      }
    } else if (validateGet(urlPath) == GetCase.INVALID) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      writer.print("{\"message\": \"invalid request\"}");
    } else {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      writer.print("{\"message\": \"not found\"}");
    }
  }


  enum GetCase {
    RESORT, DAY, INVALID;
  }

  private GetCase validateGet(String urlPath) {
    String[] urlParts = urlPath.split("/");
    if (Pattern.matches("/\\w+/vertical", urlPath)) {
      return GetCase.RESORT;
    } else if (urlParts[2].equals("days") && urlParts[4].equals("skiers")) {
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

