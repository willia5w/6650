import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import connection.ChannelPool;
import dal.LiftRidesDao;
import io.swagger.client.model.LiftRide;
import io.swagger.client.model.SkierVertical;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


@WebServlet(name = "SkierServlet")
public class SkierServlet extends HttpServlet {
  LiftRidesDao liftRidesDao = new LiftRidesDao();
  private final static String QUEUE_NAME = "IKKYONE_POSTS";
  private Connection rmqConn;
  private ObjectPool<Channel> channelPool;

  private static final JedisPoolConfig poolConfig = buildPool();
  //  private static final JedisPool POOL = new JedisPool(poolConfig, "localhost", 6379, Protocol.DEFAULT_TIMEOUT);
  private static final JedisPool POOL = new JedisPool("ikkyonecache.prdtv6.ng.0001.usw2.cache.amazonaws.com", 6379);

  private static JedisPoolConfig buildPool() {
    final JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(60); // MAX Connections
    poolConfig.setMaxIdle(60);
    poolConfig.setMinIdle(10);
    return poolConfig;
  }


  public void init(ServletConfig config) throws ServletException  {
    super.init(config);
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("54.185.77.27");
    factory.setVirtualHost("ikkyone_vhost");
    factory.setUsername("ikkyoneuser");
    factory.setPassword("ikkyonepass");
    factory.setPort(5672); // Default

    try {
      rmqConn = factory.newConnection();
    } catch (IOException | TimeoutException e) {
      e.printStackTrace();
      throw new ServletException("RMQ exception");
    }
    channelPool = new GenericObjectPool<>(new ChannelPool(rmqConn, QUEUE_NAME));
  }

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
        String message = mapper.writeValueAsString(liftRide);

        Channel channel = channelPool.borrowObject();

        channel.basicPublish(
            "",
            QUEUE_NAME,
            MessageProperties.PERSISTENT_TEXT_PLAIN,
            message.getBytes());

        channelPool.returnObject(channel);

//        AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
//
//        SendMessageRequest send_msg_request = new SendMessageRequest()
//            .withQueueUrl("https://sqs.us-west-2.amazonaws.com/417514485731/IkkyoneQueue")
//            .withMessageBody(message)
//            .withDelaySeconds(1);
//        sqs.sendMessage(send_msg_request);
//        connection.SimpleQueueSetup.sendMsgToSQS(message);

        res.setStatus(HttpServletResponse.SC_CREATED);
      } catch (Exception e) {
        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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

    String message = "{\"message\": \"no content\"}";;

    if (validateGet(urlPath) == GetCase.RESORT) {
      String skierId = urlParts[1];
      String queryResort = req.getParameter("resort");
      try {
        SkierVertical skierVertical = jedisCachedResortVert(skierId, queryResort);
        res.setStatus(HttpServletResponse.SC_OK);
        message = skierVertical.toString();
        if (skierVertical.getTotalVert() == 0) {
          res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          message = "{\"message\": \"no content\"}";
        }
      } catch (SQLException e) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        message = "{\"message\": \"no content\"}";
      } catch (IllegalArgumentException ie) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      }
    } else if (validateGet(urlPath) == GetCase.DAY) {
      String resortName = urlParts[1];
      String dayId = urlParts[3];
      String skierId = urlParts[5];

      try {
        SkierVertical skierVertical = jedisCachedDayVert(resortName, dayId, skierId);
        res.setStatus(HttpServletResponse.SC_OK);
        message = skierVertical.toString();
        if (skierVertical.getTotalVert() == 0) {
          res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          message = "{\"message\": \"no content\"}";
        }
      } catch (SQLException e) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      } catch (IllegalArgumentException ie) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      }
    } else if (validateGet(urlPath) == GetCase.INVALID) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
    writer.write(message);
  }

  enum GetCase {
    RESORT, DAY, INVALID;
  }

  private GetCase validateGet(String urlPath) {
    String[] urlParts = urlPath.split("/");
    if (Pattern.matches("/\\w+/vertical", urlPath)) {
      return GetCase.RESORT;
    } else if (urlParts.length > 4 && urlParts[2].equals("days") && urlParts[4].equals("skiers")) {
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

  private SkierVertical jedisCachedResortVert(String skierID, String resortID) throws SQLException {
    Gson gson = new Gson();
    SkierVertical skierVertical = new SkierVertical();

//    try (Jedis jedis = RedisCacheSetup.getJedisResource()) {
      try (Jedis jedis = POOL.getResource()) {
      String key = skierID + resortID;

      if (jedis.exists(key)) {
        String json = jedis.get(key); // GET Cached
        skierVertical = gson.fromJson(json, SkierVertical.class);

      } else {
        skierVertical.setTotalVert(liftRidesDao.getTotalVerticalForResort(skierID, resortID));
        skierVertical.setResortID(resortID);
        String json = gson.toJson(skierVertical);
        jedis.set(key,json); // SET Cached
      }
    }
    return skierVertical;
  }

  private SkierVertical  jedisCachedDayVert(String resortID, String dayID, String skierID) throws SQLException {
    Gson gson = new Gson();
    SkierVertical skierVertical = new SkierVertical();

//    try (Jedis jedis = RedisCacheSetup.getJedisResource()) {
    try (Jedis jedis = POOL.getResource()) {
      String key = skierID + resortID + dayID;

      if (jedis.exists(key)) {
        String json = jedis.get(key); // GET Cached
        skierVertical = gson.fromJson(json, SkierVertical.class);

      } else {
        skierVertical.setTotalVert(liftRidesDao.getSkierVerticalForSkiDay(resortID, dayID, skierID));
        skierVertical.setResortID(resortID);
        String json = gson.toJson(skierVertical);
        jedis.set(key,json); // SET Cached
      }
    }
    return skierVertical;
  }
}

