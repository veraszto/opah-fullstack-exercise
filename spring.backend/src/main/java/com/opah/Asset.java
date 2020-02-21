
package com.opah;

import java.lang.Exception;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.*;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


public class Asset
{

	public Asset( )
	{
	}

	public String join(String[] pieces, String delimiter)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0 ; i < pieces.length ; i++)
		{
			sb.append( pieces[i] + (delimiter) );
		}

		return sb.toString().replaceAll(".$", "");
	}

	public String join(String[] pieces)
	{
		return join(pieces, " ");
	}


	public boolean booleanCoercion( JSONObject jo, String key, boolean case_no_coercion )
	{
		if ( jo.has( key ) == false )
		{
			return case_no_coercion;
		}

		try
		{
			return jo.getBoolean( key );

		} catch( Exception e ) { /**/ }

		try
		{
			int can_int = jo.getInt( key );
			if ( can_int == 0 )
			{
				return false;
			}
			return true;
		} catch( Exception e ) { /**/ }

		try
		{
			String can_string = jo.getString( key );
			if ( can_string.equals("0") )
			{
				return false;
			}
			return true;
		} catch( Exception e ) { /**/ }

		this.log( "Could not coerce!" );
		return case_no_coercion;
	}

	
	public String matched( String regex, String against_to )
	{
		Pattern pattern = Pattern.compile( regex );
		Matcher match = pattern.matcher( against_to );

		if ( match.find() )
		{
			return match.group();
		}

		return null;
	}

	public Matcher match( String regex, String against_to )
	{
		return Pattern.compile( regex ).matcher( against_to );
	}

	
	public void log(String matter)
	{
		System.out.println( matter );
	}

	public void log( float matter )
	{
		this.log( String.valueOf( matter ) );
	}

	public void log( float matter, String label)
	{
		this.log( String.format("%s: %f", label, matter) );
	}

	public void log( double matter, String label )
	{
		this.log( String.format("%s: %f", label, matter) );
	}

	public void log(boolean matter, String label)
	{
		this.log( String.format("%s: %b", label, matter) );
	}

	public void log( String matter, String label )
	{
		this.log ( String.format("%s: %s", label, matter) );
	}

	public <T> void log( T[] collection )
	{
		log("Collection <T> like");
		for ( T item : collection )
		{
			log( item.toString() );
		}
	}

	public <T> void log(Map<String, T> collection, String label)
	{
		log( String.format ("%s>>>>>>>>>>>>>>>>>", label) );
		Set<Map.Entry<String, T>> set = collection.entrySet();
		Iterator<Map.Entry<String, T>> iterator = set.iterator();
		while( iterator.hasNext() )
		{
			Map.Entry<String, T> entry = iterator.next();
			if ( entry.getValue() instanceof java.util.List )
			{
				List list = ( List ) entry.getValue();
				Iterator layered_iterator = list.iterator();
				while( layered_iterator.hasNext() == true )
				{
					this.log
					( 
						String.format("%s: %s", entry.getKey(), layered_iterator.next().toString() ) 
					);
				}
			}
			else
			{
				this.log
				( 
					String.format("%s: %s", entry.getKey(), entry.getValue().toString()) 
				);
			}
		}
	}

	public <T> void log(T[] collection, String label)
	{
		log(label);
		log(collection);
	}

	public void log(int[] collection)
	{
		for (int a : collection)
		{
			log(String.valueOf(a));
		}
	}

	public void log(List<?> list)
	{
		Iterator iter = list.iterator();
		while(iter.hasNext())
		{
			log( iter.next().toString() );
		}
	}

	public void log(List<?> list, String label)
	{
		log( label );
		Iterator iter = list.iterator();
		while(iter.hasNext())
		{
			log( iter.next().toString() );
		}
	}
}
