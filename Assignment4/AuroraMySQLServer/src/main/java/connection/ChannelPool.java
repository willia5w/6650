package connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class ChannelPool extends BasePooledObjectFactory<Channel> {
  private final Connection conn;
  private final String queueName;

  public ChannelPool(Connection conn, String queueName) {
    super();
    this.conn = conn;
    this.queueName = queueName;
  }

  @Override
  public Channel create() throws Exception {
    Channel c =  conn.createChannel();
    boolean durable = true;
    c.queueDeclare(queueName, durable, false, false, null);
    return c;
  }

  @Override
  public PooledObject<Channel> wrap(Channel channel) {
    return new DefaultPooledObject<>(channel);
  }
}
