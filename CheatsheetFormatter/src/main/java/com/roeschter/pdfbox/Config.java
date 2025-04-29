package com.roeschter.pdfbox;

import java.awt.Color;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

public class Config {

	private JSONObject json;
	private Config parent;

	private String rootDir;
	private HashMap<JSONObject,JSONObject> parents = new HashMap<JSONObject,JSONObject>();


	public Config( String rootDir, String file, Config _parent ) {
		parent = _parent;
		this.rootDir = rootDir;
		json = loadJSON(file);
		buildMaps(json);
	}

	public Config(  String rootDir, String file ) {
		this( rootDir, file, null );
	}

	public Config( JSONObject _json ) {
		json = _json;
	}

	public Config( JSONObject _json, Config _parent ) {
		parent = _parent;
		json = _json;
	}

	public JSONObject loadJSON(String file) {
		String input = loadFile( file);
		String finalFile = file.replace(".json", "_final.json");
		try {

			Files.writeString(Path.of( finalFile), input);
			json = new JSONObject ( input );
		} catch (Exception e) {
			System.out.println( "Error Parsing: " + finalFile );
			e.printStackTrace();
			System.exit(1);
		}
		return json;
	}

	public String loadFile(String file) {
		if (rootDir != null)
		{
			file = rootDir + File.separatorChar + file;
		}
		String input = null;
		try {
			input = Files.readString(Path.of(file));
		} catch ( Exception e ) {
			e.printStackTrace();
			System.exit(0);
		}

		input = preProcess( input);
		return input;
	}

	/*
	 *Build  maps to help finding things
	 */
	public void buildMaps( JSONObject json ) {
		 Iterator<String> keys = json.keys();

	        // Iterate through all keys
	        while(keys.hasNext()) {
	        	String key = keys.next();
	            Object value = json.get(key);

	            // If value is a JSONObject, recursively traverse it
	            if (value instanceof JSONObject) {
	            	//Parent mapping
	            	parents.put((JSONObject) value, json);

	            	//Recursivly traverse
	                buildMaps((JSONObject) value);
	            } else {
	            	//Nothing to do yet
	            }
	        }
	}

	public JSONObject getParent(JSONObject json) {
		return parents.get(json);
	}

	public String preProcess(String input) {
	    if (input == null || input.isEmpty()) {
	        return input;
	    }

	    StringBuilder result = new StringBuilder();
	    String[] lines = input.split("\n");

	    //Check for comments and instructions
	    //We only accept full lines starting with
	    for (String line : lines) {
	    	String l = line.trim();

	    	//Check for includes
	        if ( l.startsWith("#INCLUDE")) {
	        	String file = l.substring(9);
	        	CheatsheetFormatter.info(l);
	        	String include = loadFile(file);
	        	String[] _lines = include.split("\n");
	        	for (String _line : _lines) {
	        		result.append(_line).append("\n");
	        	}
	        } else if ( l.startsWith("#")) {
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
