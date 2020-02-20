

package spring.backend;

import com.opah.net.Request;

import java.math.BigDecimal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.context.request.async.DeferredResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


@RestController
public class BrokerController
{

	com.opah.Asset asset = new com.opah.Asset();
	private final BigDecimal commission = new BigDecimal("0.7");
	private final String url = "https://cvcbackendhotel.herokuapp.com/hotels/avail/%s";

	public BrokerController()
	{
	}

	@GetMapping("/ask")
	public DeferredResult<String> requestBroker
	(
		@RequestParam( value = "citycode", defaultValue = "9626" ) String citycode,
		@RequestParam( value = "checkin", defaultValue = "20/02/2020" ) String checkin,
		@RequestParam( value = "checkout", defaultValue = "21/02/2020" ) String checkout,
		@RequestParam( value = "adults_count", defaultValue = "2" ) String adults_count,
		@RequestParam( value = "children_count", defaultValue = "5" ) String children_count
	) 
	{
		this.asset.log("Has just received /ask request");

		final DeferredResult<String> result = new DeferredResult<>();

		if 
		(
			citycode.equals("none")
		)
		{
			result.setResult( "{success:false, description:\"not enough input to proccess\"}" );
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

					if ( ! jo.has("pure_response") )
					{
						result.setResult( result( false, "uncompleted action", "[]") );
						return;
					}

					JSONArray ja = new JSONArray( jo.getString( "pure_response" ) );
					result.setResult
					( 
						BrokerController.this.initiateCalculation
						( 
							ja, checkin, checkout, 
							Integer.valueOf( adults_count ), Integer.valueOf( children_count )
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
				int adults_count, int children_count 
			)
			throws Exception
	{
		int days_interval = calculateDaysInterval( checkin, checkout );

		for ( int i = 0 ; i < ja.length() ; i++ )
		{
			JSONObject jo = ja.getJSONObject( i );
			JSONArray ja_rooms = jo.getJSONArray("rooms");
			
			for ( int j = 0 ; j < ja_rooms.length() ; j++ )
			{
				JSONObject jo_room = ja.getJSONObject( j );
				jo_room.put
				(
					"totalPrice", 
					calculateValueSumWithDaysInterval
					( 
						days_interval, adults_count, children_count,
						jo_room.getJSONObject("price").getFloat("child"),
						jo_room.getJSONObject("price").getFloat("adult") 
					) 
				);
			}
		}

		return ja.toString();
	}

	private String calculateValueSumWithDaysInterval
			( 
				int days_interval, int adults_count, int children_count, 
				float value_child, float value_adult
			)
	{
		BigDecimal days_interval_bd = new BigDecimal( days_interval );
		BigDecimal adults_count_bd = new BigDecimal( adults_count );
		BigDecimal children_count_bd = new BigDecimal( children_count );
		BigDecimal value_child_bd = new BigDecimal( value_child );
		BigDecimal value_adult_bd = new BigDecimal( value_adult );

		BigDecimal children_cost = days_interval_bd.multiply( children_count_bd ).divide( commission );
		BigDecimal adults_cost = days_interval_bd.multiply( adults_count_bd ).divide( commission );

		BigDecimal total_cost = adults_cost.add( children_cost );
		return total_cost.toString();
	}

	private String result( boolean success, String description, String result )
	{
		return String.format
				(
					"{success:%b, description:\"%s\", result:%s}", 
					success, description, result 
				);
	}

	private int calculateDaysInterval( String init, String end )
	{
		return 5;
	}

}
