package connection;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import software.amazon.awssdk.utils.Logger;
// com.google.common.util.concurrent.RateLimiter; // Guava Rate limiter to control rate API is called

public class RedisCacheSetup {
  // -Dcom.amazonaws.sdk.enableDefaultMetrics=credentialFile=/path/aws.properties,cloudwatchRegion=us-west-2
  private Logger logger;
  private Jedis jedisCli;
  private static JedisPool pool; //Thread pool object

  private static String ADDR = "ikkyonecache.prdtv6.ng.0001.usw2.cache.amazonaws.com"; 	//The server address where redis is located (in this case, ElastiCache Hosted)
  private static int PORT = 6379; 		// Port number
  private static String AUTH = "";		// Password (I didn't set it)
  private static int MAX_IDLE = 10;		// Maximum number of idle (free) connections in connection pool (maximum retention)
  private static int MIN_IDLE = 10;		// Minimum number of idle (free) connections in connection pool (maximum retention)
  private static int MAX_ACTIVE = 50;		// Maximum number of active connections (how many connections are available)
  private static int MAX_WAIT = 100000;		// Maximum time to wait for available connections(Millisecond)，Default value-1，It means never timeout. If the waiting time is exceeded, then throw JedisConnectionException
  private static int TIMEOUT = 10000;		// Timeout of link pool#When using a connection, check if the connection is successful
  private static boolean TEST_ON_BORROW = true;	//When using a connection, test whether the connection is available
  private static boolean TEST_ON_RETURN = true;

  static {
    GenericObjectPoolConfig config = new GenericObjectPoolConfig();
    config.setMaxIdle(MAX_IDLE);
    config.setMinIdle(MIN_IDLE);
//    config.setMaxActive(MAX_ACTIVE);
//    config.setMaxWait(MAX_WAIT);
//    config.setTestOnBorrow(TEST_ON_BORROW);
//    config.setTestOnReturn(TEST_ON_RETURN);
    pool = new JedisPool(config, ADDR, PORT, TIMEOUT); //New connection pool, such as password and final parameter
  }

//  Should preload after the pool is defined

//  public static void preLoad() {
//    List<Jedis> minIdleJedisList = new ArrayList<Jedis>(MIN_IDLE);
//
//    for (int i = 0; i < MIN_IDLE; i++) {
//      Jedis jedis = null;
//      try {
//        jedis = pool.getResource();
//        minIdleJedisList.add(jedis);
//        jedis.ping();
//      } catch (Exception e) {
//        e.printStackTrace();
//      } finally {
//      }
//    }
//
//    for (int i = 0; i < MIN_IDLE; i++) {
//      Jedis jedis = null;
//      try {
//        jedis = minIdleJedisList.get(i);
//        jedis.close();
//      } catch (Exception e) {
//        e.printStackTrace();
//      } finally {
//
//      }
//    }
//  }

  public static Jedis getJedisResource() {
    try {
      if (pool!=null) {
        Jedis resource = pool.getResource();
        return resource;
      }
      return null;
    } catch(Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public static void returnResource (Jedis used) {
    if(pool!=null) {
      used.close();  // ReturnResource() deprecated
    }
  }
}
