

package spring.backend;

import com.opah.net.Request;

import java.math.BigDecimal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import org.springframework.web.context.request.async.DeferredResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.regex.*;
import java.util.Calendar;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;


@CrossOrigin
@RestController
public class BrokerController
{

	com.opah.Asset asset = new com.opah.Asset();
	private final BigDecimal commission = new BigDecimal("0.7");
	private final String url = "https://cvcbackendhotel.herokuapp.com:443/hotels/avail/%s";
//	private final String url = "https://ifconfig.me/ip";

	public BrokerController()
	{
	}

	@PostMapping("/ask")
	public DeferredResult<String> requestBroker
	(
		@RequestParam( value = "citycode", defaultValue = "9626" ) String citycode,
		@RequestParam( value = "checkin", defaultValue = "20/02/2020" ) String checkin,
		@RequestParam( value = "checkout", defaultValue = "21/02/2020" ) String checkout,
		@RequestParam( value = "adults_count", defaultValue = "2" ) String adults_count,
		@RequestParam( value = "children_count", defaultValue = "5" ) String children_count
	) 
	{
		this.asset.log
		( 
			String.format
			(
				"Has just received /ask request, %s, %s, %s, %s, %s", 
				citycode, checkin, checkout, adults_count, children_count
			) 
		);

		final DeferredResult<String> result = new DeferredResult<>();
		long days_interval = calculateDaysInterval( checkin, checkout );

		if 
		(
			citycode.equals("none")
		)
		{
			result.setResult( result( false, "Not enough input to proccess the request", "[]") );
			return result;
		}
		if ( days_interval <= 0 )
		{
			result.setResult( result( false, "Dates are inverted", "[]") );
			return result;
		}
		new Thread
		(
			() -> 
			{
				try
				{
					this.asset.log("Initializing request...");

					Request request = new Request ( String.format( url, citycode ) );
					request.postJSONParse( false );
					JSONObject jo = request.make( new JSONObject() );
					asset.log("Ask finished");

					if ( ! jo.has("pure_response") )
					{
						result.setResult( result( false, "uncompleted action", "[]") );
						return;
					}

					JSONArray ja = new JSONArray( jo.getString( "pure_response" ) );

					String json_result = 
							BrokerController.this.initiateCalculation
							( 
								ja, checkin, checkout, 
								Integer.valueOf( adults_count ), Integer.valueOf( children_count ),
								days_interval
							);
					

					result.setResult
					( 
						result
						(
							true,
							"ok",
							json_result						
						)
					);
				}
				catch( Exception exception )
				{
					BrokerController.this.asset.log( exception.toString(), "error new Thread http request" );
				}
			}

		).start();

		return result;
	}

	private String initiateCalculation
			( 
				JSONArray ja, String checkin, String checkout,  
				int adults_count, int children_count, long days_interval
			)
			throws Exception
	{
		int length = ja.length();
		asset.log( String.format("Received %d hotels", length) );
		for ( int i = 0 ; i < ja.length() ; i++ )
		{
			JSONObject jo = ja.getJSONObject( i );
			JSONArray ja_rooms = jo.getJSONArray("rooms");
			
			for ( int j = 0 ; j < ja_rooms.length() ; j++ )
			{
				JSONObject jo_room = ja_rooms.getJSONObject( j );
				jo_room.put
				(
					"totalPrice", 
					calculateValueSumWithDaysInterval
					( 
						days_interval, adults_count, children_count,
						Float.valueOf( jo_room.getJSONObject("price").getFloat("child") ),
						Float.valueOf( jo_room.getJSONObject("price").getFloat("adult") )
					) 
				);
			}
		}

		return ja.toString();
	}

	private String calculateValueSumWithDaysInterval
			( 
				long days_interval, int adults_count, int children_count, 
				float value_child, float value_adult
			)
	{
		BigDecimal days_interval_bd = new BigDecimal( days_interval );
		BigDecimal adults_count_bd = new BigDecimal( adults_count );
		BigDecimal children_count_bd = new BigDecimal( children_count );
		BigDecimal value_child_bd = new BigDecimal( value_child );
		BigDecimal value_adult_bd = new BigDecimal( value_adult );

		BigDecimal children_cost = 
			days_interval_bd
			.multiply( children_count_bd )
			.multiply( value_child_bd )
			.divide( commission, 2, RoundingMode.HALF_UP  );

		BigDecimal adults_cost = 
			days_interval_bd
			.multiply( adults_count_bd )
			.multiply( value_adult_bd )
			.divide( commission, 2, RoundingMode.HALF_UP  );

		BigDecimal total_cost = adults_cost.add( children_cost );
		return total_cost.toString();
	}

	private String result( boolean success, String description, String result )
	{
		return String.format
				(
					"{\"success\":%b, \"description\":\"%s\", \"result\":%s}", 
					success, description, result 
				);
	}

	protected long calculateDaysInterval( String init, String end )
	{
		long diff = 
			ChronoUnit.DAYS.between
			( 
				makeCalendar( init ).toInstant(), makeCalendar( end ).toInstant()
			);
		return diff;
	}

	protected Calendar makeCalendar( String date )
	{
		String match_year = "^....";
		String match_month = "....-(..)";
		//Could also be this
		//String match_month = "(?<=....-)..(?=-..)";
		String match_day = "..$";


		Calendar calendar = Calendar.getInstance();
		int year = extractFromDate( match_year, date, 0, 0);
		int month = extractFromDate( match_month, date, 1, -1 );
		int day = extractFromDate( match_day, date, 0, 0 );
		calendar.set( year, month, day );
		return calendar;
	}

	protected int extractFromDate( String regex, String date, int group_index, int modifier )
	{
		Matcher match = asset.match( regex, date );

		if ( match.find() )
		{
			return Integer.valueOf( match.group( group_index ) ) + modifier; 
		}

		return -1;
	}

}
