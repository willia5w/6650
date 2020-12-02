import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import dal.LiftRidesDao;
import io.swagger.client.model.LiftRide;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class QueueListener implements ServletContextListener {
  private Thread thread = null;
  private int pool = 60;
  private final static String QUEUE_NAME = "IKKYONE_POSTS";
  LiftRidesDao liftRidesDao = new LiftRidesDao();
  ObjectMapper mapper = new ObjectMapper();

  public void contextInitialized(ServletContextEvent sce) {
    if (thread == null || (!thread.isAlive())) {
      thread = new Thread(() -> {
        ExecutorService executorService = Executors.newFixedThreadPool(pool);
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("54.213.50.74");

        for (int i = 0; i < pool; i++) {
          executorService.execute(() -> {
            while (true) {
              try {
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();
                boolean durable = true;
                channel.queueDeclare(QUEUE_NAME, durable, false, false, null);
                int prefetchCount = 1;
                channel.basicQos(prefetchCount);

                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                  String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                  LiftRide liftRide = mapper.readValue(message, LiftRide.class);

                  try {
                    liftRidesDao.insertLiftRide(liftRide);
                  } catch (SQLException e) {
                    e.printStackTrace();
                  } finally {
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                  }
                };

                boolean autoAck = false;
                channel.basicConsume(QUEUE_NAME, autoAck, deliverCallback, consumerTag -> { });
              } catch (IOException | TimeoutException e) {
                e.printStackTrace();
              }

              try {
                Thread.sleep(1);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            }
          });
        }
      });

      thread.start();
    }
  }

  public void contextDestroyed(ServletContextEvent sce) {
    try {
      thread.interrupt();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
