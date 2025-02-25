package com.roeschter.pdfbox;

import org.json.JSONObject;

public class Config {

	private JSONObject json;
	private Config parent;

	public Config( JSONObject _json, Config _parent ) {
		parent = _parent;
		json = json;
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

	public float get(  String key, double _default) {
		float ret = 0;

		if ( parent!=null)
			ret = parent.get( key, _default );
		else
			ret = (float) _default;

		if ( !json.isNull(key) )
			ret = (float) json.getDouble(key);

		return ret;
	}

	public int get( String key, int _default) {
		int ret = 0;

		if ( parent!=null)
			ret = parent.get( key, _default );
		else
			ret = _default;

		if ( !json.isNull(key) )
			ret = (int) json.getLong(key);

		return ret;
	}


	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
