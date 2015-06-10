package de.skawronek.compassnavigator;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

public final class DestinationList {
	private static final String TAG = DestinationList.class.getName();

	private static final DestinationList INSTANCE = new DestinationList();

	private final List<Destination> destinations = new ArrayList<Destination>();

	public static DestinationList getInstance() {
		return INSTANCE;
	}

	private DestinationList() {
	}

	public List<Destination> getList() {
		return new ArrayList<Destination>(destinations);
	}

	public void add(final Destination dest) {
		add(destinations.size(), dest);
	}

	public void add(final int pos, final Destination dest) {
		if (dest == null) {
			throw new NullPointerException();
		}

		destinations.add(pos, dest);
		Log.v(TAG, "Add " + dest.toString());
	}

	public boolean contains(final Destination dest) {
		if (dest == null) {
			throw new NullPointerException();
		}

		return destinations.contains(dest);
	}

	public void replace(final Destination source, final Destination by) {
		if (source == null || by == null) {
			throw new NullPointerException();
		}

		final int sourcePos = destinations.indexOf(source);
		if (sourcePos == -1) {
			throw new NoSuchElementException();
		}

		destinations.add(sourcePos, by);
		destinations.remove(sourcePos);
		Log.v(TAG, "Replace " + sourcePos + " by " + by);
	}

	public void remove(final Destination tl) {
		if (tl == null) {
			throw new NullPointerException();
		}
		destinations.remove(tl);
		Log.v(TAG, "Remove " + tl.toString());
	}

	public void save(final OutputStream out) throws IOException {
		if (out == null) {
			throw new NullPointerException();
		}

		//@formatter:off
		/*
		 * [
		 *   {
		 *     "name": "Berlin, Fernsehturm",
		 *     "latitude": 52.520803,
		 *     "longitude": 13.40945,
		 *   },
		 *   {
		 *     ...
		 *   }
		 * ]
		 */
		//@formatter:on

		final JsonWriter writer = new JsonWriter(new OutputStreamWriter(out,
				"UTF-8"));
		try {
			writeList(writer, destinations);
			Log.v(TAG, "Saved " + destinations.size() + " destination(s)");
		} finally {
			writer.close();
		}
	}

	private void writeList(final JsonWriter writer, final List<Destination> list)
			throws IOException {
		writer.beginArray();
		for (final Destination tl : list) {
			writeDestination(writer, tl);
		}
		writer.endArray();
	}

	private void writeDestination(final JsonWriter writer, final Destination tl)
			throws IOException {
		writer.beginObject();
		writer.name("name").value(tl.getName());
		final double latitude = tl.getLocation().getLatitude();
		final double longitude = tl.getLocation().getLongitude();
		writer.name("latitude").value(latitude);
		writer.name("longitude").value(longitude);
		writer.endObject();
	}

	public void load(final InputStream in) throws IOException {
		if (in == null) {
			throw new NullPointerException();
		}

		final JsonReader reader = new JsonReader(new InputStreamReader(in,
				"UTF-8"));
		try {
			destinations.clear();
			readList(reader);
			Log.v(TAG, "Loaded " + destinations.size() + " destination(s)");
		} finally {
			reader.close();
		}
	}

	private void readList(final JsonReader reader) throws IOException {
		reader.beginArray();
		while (reader.hasNext()) {
			final Destination tl = readDestination(reader);
			destinations.add(tl);
		}
		reader.endArray();
	}

	private Destination readDestination(final JsonReader reader)
			throws IOException {
		reader.beginObject();
		final String name;
		if (!"name".equals(reader.nextName())) {
			throw new IOException("Expected name");
		}
		name = reader.nextString();
		final double latitude;
		if (!"latitude".equals(reader.nextName())) {
			throw new IOException("Expected latitude");
		}
		latitude = reader.nextDouble();
		final double longitude;
		if (!"longitude".equals(reader.nextName())) {
			throw new IOException("Expected longitude");
		}
		longitude = reader.nextDouble();
		reader.endObject();

		return new Destination(name, latitude, longitude);
	}
}
