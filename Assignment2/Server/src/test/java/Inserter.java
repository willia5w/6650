import dal.LiftRidesDao;
import io.swagger.client.model.LiftRide;
import java.sql.SQLException;


/**
 * main() runner, used to test the Ikkyone .
 *
 */
public class Inserter {

	public static void main(String[] args) throws ClassNotFoundException, SQLException {

		// DAO instances.
		LiftRidesDao liftRidesDao = new LiftRidesDao();
		//		{
		//			  "resortID": "Mission Ridge",
		//				"dayID": 23,
		//				"skierID": 7889,
		//				"time": 217,
		//				"liftID": 21
		//		}
		// INSERT objects from our model.
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

		liftRidesDao.insertLiftRide(liftRide);
		
		// READ.
		int resVert = liftRidesDao.getTotalVerticalForResort(skierId, resortName);
		System.out.format("Resort Vert: " + Integer.toString(resVert) + "\n");

		int dayVert = liftRidesDao.getSkierVerticalForSkiDay(resortName, Integer.parseInt(dayId), Integer.parseInt(skierId));
		System.out.format("Day Vert: " + Integer.toString(dayVert) + "\n");
	}
}
