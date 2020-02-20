

package spring.backend;
import com.opah.network.Request;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.context.request.async.DeferredResult;


@RestController
public class BrokerController
{

	Asset asset = new com.opah.Asset();
	private final BigDecimal comission = new BigDecimal("0.7");
	private final String url = "https://cvcbackendhotel.herokuapp.com/hotels/avail/%d"

	public BrokerController()
	{
	}

	@GetMapping("/ask")
	public DeferredResult<String> requestBroker
	(
		@RequestParam( value = "citycode", defaultValue = null ) int citycode,
		@RequestParam( value = "checkin", defaultValue = null) String checkin,
		@RequestParam( value = "checkout", defaultValue = null) String checkout,
		@RequestParam( value = "adults_count", defaultValue = null) String adults_count,
		@RequestParam( value = "children_count", defaultValue = null) String children_count
	) 
	{

		final DeferredResult<String> result = new DeferredResult<>();

		if 
		(
			citycode == null				
		)
		{
			result.setResult( "[]" );
			return result;
		}

		new Thread
		(
			() -> 
			{
				Request request = 
					new Request
					(
						String.format( url, citycode ),
						new Request.Callback()
						{
							public void callback( JSONObject jo )
								throws Exception
							{
								JSONArray ja = new JSONArray( jo.getString( "pure_response" ) );
								result.setResult
								( 
									BrokerController.this.initiateCalculation
									( 
										ja, checkin, checkout, 
										adults_count, children_count
									) ;
								);
							}
						}	
					);

				request.postJSONParse( false );
				request.executeOnExecutor( AsyncTask.THREAD_POOL_EXECUTOR,  new JSONObject() );
			}
		);

		return result;
	}

	private String initiateCalculation
			( 
				JSONArray ja, String checkin, 
				String checkout,  int adults_count, int children_count 
			)
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
						days_interval, 
						new BigDecimal( jo_room.getJSONObject("price").getFloat("child") ),
						new BigDecimal( jo_room.getJSONObject("price").getFloat("adult") )
					) 
				);
			}
		}

		return ja.toString();
	}

	private int calculateValueSumWithDaysInterval( int days_interval, BigDecimal value_child, BigDecimal value_adult )
	{
	}

	private int calculateDaysInterval( String init, String end )
	{
		return 5;
	}

}
