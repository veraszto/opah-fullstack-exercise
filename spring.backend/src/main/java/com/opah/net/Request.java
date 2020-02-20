package com.opah.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class Request
{

	private HttpsURLConnection http_url_connection;
	public byte[] stream_byte_array;
	private com.opah.Asset asset = new com.opah.Asset();
	//Get the default to request from movies
	public String method = "GET";
	public String url_string;
	private boolean post_json_parse = true;

	public Request( String url_string )
	{
		this.url_string = url_string;
		asset.log(url_string, "constructor Http");
	};

	public void postJSONParse( boolean parse )
	{
		this.post_json_parse = parse;
	}

	//\makeHttpsURLConnectionReady
	private void makeHttpsURLConnectionReady( URL url ) 
		throws Exception
	{
		asset.log( String.format("(%s) Preparing connection...)", url.toString() ));
		this.http_url_connection = (HttpsURLConnection) url.openConnection();
//		http_url_connection.setRequestProperty("Accept", "*/*");
		http_url_connection.setRequestMethod( this.method );
		http_url_connection.setConnectTimeout( 10000 );
		http_url_connection.setReadTimeout( 10000 );
//		http_url_connection.setDoOutput( true );
		//This is the default anyway
		http_url_connection.setDoInput( true );
		this.http_url_connection.setAllowUserInteraction( true );
	}

	//\addToQuery
	private void addToQuery(StringBuilder s, String key, String value) throws Exception
	{
		String uppersand = "&";

		if (s.length() == 0)
		{
			uppersand = "";
		}

		s.append
		( 
			String.format
			(
				"%s%s=%s", uppersand, key, 
				java.net.URLEncoder.encode
				( 
					value, StandardCharsets.UTF_8.toString() 
				)
			)
		);
	}

	//\actionCore
	public JSONObject make( JSONObject args ) 
		throws Exception
	{
		JSONObject jo_response = new JSONObject();
		jo_response.put("success", false);
		StringBuilder post = new StringBuilder();

	

		try
		{
			Iterator<String> iterator = args.keys();
			while( iterator.hasNext() )
			{
				String key = iterator.next();
				addToQuery( post, key, args.getString(key) );
			}

		}
		catch(Exception e)
		{
			buildResponse( jo_response, "building pack to send", e );
			return jo_response;
		}

		String response = send( post );

		if ( response == null )
		{
			this.asset.log("response is null");
			return jo_response;
		}
		if ( response.equals("") )
		{
			this.asset.log("response is empty");
			return jo_response;
		}
		
	
		jo_response.put( "pure_response", response);

		if ( post_json_parse == false )
		{
			jo_response.put("success", true);
			asset.log( String.format("Imediate response, not a JSONString: %s", response ) );
			return jo_response;
		}
		else
		{
			asset.log( String.format("Imediate response: %s", response) );
		}

		try
		{
			jo_response.put
			(
				"parsed", 
				new JSONObject
				(
					response
				)
			);
			jo_response.put("success", true);
		}
		catch(Exception e)
		{
			buildResponse( jo_response, "response parse", e );
			return jo_response;
		}

		return jo_response;
	}

	//\buildResponse
	private void buildResponse( JSONObject jo_response, String comment, Exception exception )
		throws Exception
	{
		String error = String.format("Request error, %s", comment );
		jo_response.put
		(
			"error",
			error
		);

		this.asset.log( exception.toString(), error );
	}

	
	//\send
	private String send(StringBuilder post) throws Exception
	{
		ByteArrayOutputStream response = new ByteArrayOutputStream();
		InputStream in = null;
		OutputStream out = null;
		try
		{
			makeHttpsURLConnectionReady
			(
				new URL
				(
					this.url_string
				)
			);

			String string_post_from_string_builder =  post.toString();
			asset.log(String.format("ThePost(%d length): %s", post.length(), string_post_from_string_builder ));
			asset.log("Flushing data...");
			byte[] bytes = string_post_from_string_builder.getBytes();
			string_post_from_string_builder = null;
			this.http_url_connection.setFixedLengthStreamingMode(bytes.length);
			out = new BufferedOutputStream(http_url_connection.getOutputStream());
			asset.log( http_url_connection.getResponseCode(), "ResponseCode");
			/*
			out.write(bytes, 0, bytes.length);
			out.flush();
			*/
			in = new BufferedInputStream( http_url_connection.getInputStream() );
			int a_byte;

			while
			(	
				( a_byte = in.read() ) != -1
			)
			{
				response.write( a_byte );
			}
			this.stream_byte_array = response.toByteArray();
		}
		catch( Exception exception )
		{
			this.asset.log( exception.toString(), "send method from Request error ");
			exception.printStackTrace();
			return null;
		}
		finally
		{
			if (response != null)
			{
				response.close();
			}
			if (out != null)
			{
				out.close();
			}
			if (in != null)
			{
				in.close();
			}
			http_url_connection.disconnect();
			String stringed_response = response.toString();
			asset.log( String.format("(%s)", stringed_response) );
			return stringed_response;
		}
	}

}
