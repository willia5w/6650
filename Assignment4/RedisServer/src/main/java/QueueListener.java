import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import dal.LiftRidesDao;
import io.swagger.client.model.LiftRide;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
        factory.setHost("54.185.77.27");
        factory.setVirtualHost("ikkyone_vhost");
        factory.setUsername("ikkyoneuser");
        factory.setPassword("ikkyonepass");
        factory.setPort(5672);

        Connection connection = null;
        try {
          connection = factory.newConnection();
        } catch (IOException e) {
          e.printStackTrace();
        } catch (TimeoutException e) {
          e.printStackTrace();
        }
        Channel channel = null;
        try {
          channel = connection.createChannel();
        } catch (IOException e) {
          e.printStackTrace();
        }

        for (int i = 0; i < pool; i++) {
          Channel finalChannel = channel;
          executorService.execute(() -> {
            while (true) {
              try {

                finalChannel.queueDeclare(QUEUE_NAME, true, false, true, null);
                int prefetchCount = 1;
                finalChannel.basicQos(prefetchCount);

                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                  String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                  LiftRide liftRide = mapper.readValue(message, LiftRide.class);

                  try {
                    liftRidesDao.insertLiftRide(liftRide);
                  } finally {
                    finalChannel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                  }
                };

                boolean autoAck = false;
                finalChannel.basicConsume(QUEUE_NAME, autoAck, deliverCallback, consumerTag -> { });
              } catch (IOException e) {
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