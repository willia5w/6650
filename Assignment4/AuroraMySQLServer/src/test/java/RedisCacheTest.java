import static org.junit.Assert.assertFalse;

import dal.LiftRidesDao;
import io.swagger.client.model.LiftRide;
import java.sql.SQLException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


public class RedisCacheTest {

	public static void main(String[] args) throws ClassNotFoundException, SQLException {

		LiftRidesDao liftRidesDao = new LiftRidesDao();

		final String resortName = "Mission Ridge";
		final String skierId = "7889";
		final String dayId = "23";
		final String liftId = "21";
		final String startTime = "217";

		LiftRide liftRide = new LiftRide();
		liftRide.setResortID(resortName);
		liftRide.setDayID(dayId);
		liftRide.setSkierID(skierId);
		liftRide.setLiftID(liftId);
		liftRide.setTime(startTime);

//		liftRidesDao.insertLiftRide(liftRide);
		int resVert;
		JedisPool jedisPool;
		Jedis jedis;


		final JedisPoolConfig poolConfig = buildPool();
//		jedisPool = new JedisPool(poolConfig, "ikkyonecache.prdtv6.ng.0001.usw2.cache.amazonaws.com",
//				6739);
		jedisPool = new JedisPool(poolConfig, "ikkyonecache.prdtv6.ng.0001.usw2.cache.amazonaws.com", 6379, 2000, "passwordpassword", false);
		jedis = jedisPool.getResource();
//		jedis = new Jedis("ikkyonecache.prdtv6.ng.0001.usw2.cache.amazonaws.com", 6379);

//			resVert = liftRidesDao.getTotalVerticalForResort(Integer.parseInt(skierId), resortName);
			String key1 = String.valueOf(skierId) + resortName + String.valueOf(dayId);
			assertFalse(jedis.exists(key1));

//			resVert = liftRidesDao.getTotalVerticalForResort(Integer.parseInt(skierId), resortName);
//			String key2 = String.valueOf(skierId) + resortName + String.valueOf(dayId);
//			assertTrue(jedis.exists(key2));

	}

	private static JedisPoolConfig buildPool() {
		final JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(60); // MAX Connections
		poolConfig.setMaxIdle(60);
		poolConfig.setMinIdle(10);
		return poolConfig;
	}
}
