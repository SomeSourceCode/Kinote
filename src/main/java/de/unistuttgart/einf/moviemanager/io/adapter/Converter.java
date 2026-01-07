package de.unistuttgart.einf.moviemanager.io.adapter;

import com.google.gson.JsonObject;

public class Converter {

	  protected static String getAsStringOrNull(JsonObject obj, String key) {
		if (!obj.has(key) || obj.get(key).isJsonNull())
			return null;

		return obj.get(key).getAsString();
	}

	protected static Boolean getAsBooleanOrFalse(JsonObject obj, String key) {
		if (!obj.has(key) || obj.get(key).isJsonNull())
			return false;

		return obj.get(key).getAsBoolean();
	}
}
