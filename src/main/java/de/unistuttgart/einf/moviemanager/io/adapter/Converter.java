package de.unistuttgart.einf.moviemanager.io.adapter;

import com.google.gson.JsonObject;

import java.util.UUID;

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

	protected static UUID getAsUUIDOrNull(JsonObject obj, String key) {
		if (!obj.has(key) || obj.get(key).isJsonNull())
			return null;

		try {
			return UUID.fromString(obj.get(key).getAsString());

		} catch (IllegalArgumentException e) {
			return null;
		}
	}

}
