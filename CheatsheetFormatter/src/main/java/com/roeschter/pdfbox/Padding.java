package com.roeschter.pdfbox;

import org.json.JSONObject;

public class Padding {

	public Padding() {}

	public Padding( Config config) {
		top = config.getInt("pad.top", 0 );
		bottom = config.getInt("pad.bottom", 0 );
		left = config.getInt("pad.left", 0 );
		right = config.getInt("pad.right", 0 );
	}

	public Padding( Config config, String key) {
		JSONObject json = config.getJSONObject(key);
		if ( json != null) {
			config = new Config(json);
			top = config.getInt("pad.top", 0 );
			bottom = config.getInt("pad.bottom", 0 );
			left = config.getInt("pad.left", 0 );
			right = config.getInt("pad.right", 0 );
		}
	}


	public Padding( float _top, float _bottom, float _left, float _right) {
		top = _top;
		bottom = _bottom;
		left = _left;
		right = _right;
	}

	public float top;
	public float bottom;
	public float left;
	public float right;
}
