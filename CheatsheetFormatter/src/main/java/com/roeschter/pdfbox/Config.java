package com.roeschter.pdfbox;

import java.math.BigDecimal;

import org.json.JSONArray;
import org.json.JSONObject;

public class Config {

	private JSONObject json;
	private Config parent;

	public Config( JSONObject _json, Config _parent ) {
		parent = _parent;
		json = _json;
	}

	public Config( JSONObject _json ) {
		json = _json;
	}


	/*
	public String get( JSONObject json, String key, String _default) {
		String ret = null;

		if ( !json.isNull(key) )
			ret = json.getString(key);
		else
			ret = _default;

		return ret;
	}
	*/

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

	/*
	public float get( JSONObject json, String key, float _default) {
		float ret = 0;

		if ( !json.isNull(key) )
			ret = (float)json.getDouble(key);
		else
			ret = _default;

		return ret;
	}
	*/


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


	/*
	public int getInt( JSONObject json, String key, int _default) {
		int ret = 0;

		if ( !json.isNull(key) )
			ret = (int)json.getLong(key);
		else
			ret = _default;

		return ret;
	}
	*/



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

	public float[] getFloatArray( String key) {
		JSONArray array = getJSONArray(key);
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

    public int[] getIntArray( String key) {
		JSONArray array = getJSONArray(key);
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


	public JSONArray getJSONArray( String key) {
		JSONArray ret = null;

		if ( parent!=null)
			ret = parent.getJSONArray( key );

		if ( !json.isNull(key) )
			ret = json.getJSONArray(key);

		return ret;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
