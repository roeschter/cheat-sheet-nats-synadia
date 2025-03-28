package com.roeschter.pdfbox;

import java.awt.Color;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import org.json.JSONArray;
import org.json.JSONObject;

public class Config {

	private JSONObject json;
	private Config parent;

	public Config( String file, Config _parent ) {
		parent = _parent;
		json = loadJSON(file);;
	}

	public Config( String file ) {
		this( file, null );
	}

	public Config( JSONObject _json ) {
		json = _json;
	}

	public Config( JSONObject _json, Config _parent ) {
		parent = _parent;
		json = _json;
	}



	public JSONObject loadJSON(String file) {
		JSONObject json = null;
		String input = "";
		try {
			input = Files.readString(Path.of(file));
		} catch ( Exception e ) {
			e.printStackTrace();
		}

		try {
			json = new JSONObject ( removeHashComments( input) );
		} catch (Exception e) {
			System.out.println( "Error Parsing: " + file );
			e.printStackTrace();
			System.exit(1);
		}
		return json;
	}

	public static String removeHashComments(String input) {
	    if (input == null || input.isEmpty()) {
	        return input;
	    }

	    StringBuilder result = new StringBuilder();
	    String[] lines = input.split("\n");

	    for (String line : lines) {
	        if (line.trim().startsWith("#")) {
	        	result.append("\n");
	        } else {
	        	result.append(line).append("\n");
	        }
	    }

	    return result.toString();
	}


	public String get( String key, String _default) {
		String ret = null;

		if ( parent!=null)
			ret = parent.get( key, _default );
		else
			ret = _default;

		if ( !json.isNull(key) )
			ret = json.getString(key);

		return ret;
	}




	public float getFloat(  String key, double _default) {
		float ret = 0;

		if ( parent!=null)
			ret = parent.getFloat( key, _default );
		else
			ret = (float) _default;

		if ( !json.isNull(key) )
			ret = (float) json.getDouble(key);

		return ret;
	}

	public boolean getBool( String key, boolean _default) {
		boolean ret = false;

		if ( parent!=null)
			ret = parent.getBool( key, _default );
		else
			ret = _default;

		if ( !json.isNull(key) )
			ret = json.getBoolean(key);

		return ret;
	}


	public Color getColor( String key, Color _default)  {
		Color ret;

		if ( parent!=null)
			ret = parent.getColor( key, _default );
		else
			ret = _default;

		if ( !json.isNull(key) ) {
			int[] c = getIntArray(key);
			ret = new Color(c[0],c[1],c[2]);
		}

		return ret;
	}

    public int[] getIntArray( String key) {
		JSONArray array = getJSONArray(key, null);
		int[] ret = null;
		if ( array != null) {
			ret = new int[array.length()];
			int n=0;
			for ( Object obj: array)
			{
				Integer dec = (Integer)obj;
				ret[n++] = dec.intValue();
			}
		}
		return ret;
	}

    public float[] getFloatArray( String key) {
		JSONArray array = getJSONArray(key, null);
		float[] ret = null;
		if ( array != null) {
			ret = new float[array.length()];
			int n=0;
			for ( Object obj: array)
			{
				BigDecimal dec = (BigDecimal)obj;
				ret[n++] = dec.floatValue();
			}
		}
		return ret;
    }

    public int getInt( String key, int _default) {
		int ret = 0;

		if ( parent!=null)
			ret = parent.getInt( key, _default );
		else
			ret = _default;

		if ( !json.isNull(key) )
			ret = (int) json.getLong(key);

		return ret;
	}



	public boolean isNull( String key ) {
		return json.isNull(key);
	}

	public JSONObject getJSONObject( String key) {
		JSONObject ret = null;

		if ( parent!=null)
			ret = parent.getJSONObject( key );

		if ( !json.isNull(key) )
			ret = json.getJSONObject(key);

		return ret;
	}


	public JSONArray getJSONArray( String key, JSONArray _default) {
		JSONArray ret = _default;

		if ( parent!=null)
			ret = parent.getJSONArray( key, ret );

		if ( !json.isNull(key) )
			ret = json.getJSONArray(key);

		return ret;
	}


}
