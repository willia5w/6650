package dal;

import connection.RedisCacheSetup;
import io.swagger.client.model.LiftRide;
import redis.clients.jedis.Jedis;

public class LiftRidesDao {
  Jedis jedis;

  public LiftRidesDao() {
//    RedisCacheSetup.preLoad();
  }

  public void insertLiftRide (LiftRide newLiftRide) {

    String resort = newLiftRide.getResortID();
    String day = newLiftRide.getDayID();
    String skierId = newLiftRide.getSkierID();
    int newVert = 10 * Integer.parseInt(newLiftRide.getLiftID());
    int oldResortVert = getTotalVerticalForResort(skierId, resort);
    int oldDayVert = getSkierVerticalForSkiDay(resort, day, skierId);

    try {
      jedis = RedisCacheSetup.getJedisResource();
      jedis.hset(skierId, resort+day, String.valueOf(newVert + oldResortVert));
      jedis.hset(skierId, resort, String.valueOf(newVert + oldDayVert));
    } finally {
      RedisCacheSetup.returnResource(jedis);
    }
  }


  public int getTotalVerticalForResort (String skierId, String resortName) {
    int resortVert = 0;
    try {
      jedis = RedisCacheSetup.getJedisResource();
      if (jedis.hexists(skierId, resortName)) {
        resortVert = Integer.valueOf(jedis.hget(skierId, resortName));
      }
    } finally {
      RedisCacheSetup.returnResource(jedis);
    }
    return resortVert;
  }

  public int getSkierVerticalForSkiDay (String resortName, String dayId, String skierId) {
    int dayVert = 0;
    try {
      jedis = RedisCacheSetup.getJedisResource();
      if (jedis.hexists(skierId, resortName+dayId)) {
        dayVert = Integer.valueOf(jedis.hget(skierId, resortName+dayId));
      }
    } finally {
      RedisCacheSetup.returnResource(jedis);
    }
    return dayVert;
  }
}