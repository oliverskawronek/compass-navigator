package vertex.compassnavigator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.location.Location;

public final class Destination implements Serializable {
	private static final long serialVersionUID = 1L;

	// TODO Serialiazation Proxy to make the fields final
	private String name;
	private Location location;

	public Destination(final String name, final Location location) {
		if (name == null || location == null) {
			throw new NullPointerException();
		}
		this.name = name;
		this.location = location;
	}

	public Destination(final String name, final double latitude,
			final double longitude) {
		if (name == null) {
			throw new NullPointerException();
		}

		final Location location = new Location("default");
		location.setLatitude(latitude);
		location.setLongitude(longitude);
		this.name = name;
		this.location = location;
	}

	public String getName() {
		return name;
	}

	public Location getLocation() {
		return new Location(location);
	}

	// TODO seems to be buggy
	@Override
	public boolean equals(final Object o) {
		if (o == null) {
			return false;
		} else if (o == this) {
			return true;
		} else if (o instanceof Destination) {
			final Destination other = (Destination) o;
			//@formatter:off
			return this.name.equals(other.name)
					&& Double.compare(this.location.getLatitude(), other.location.getLatitude()) == 0
					&& Double.compare(this.location.getLongitude(), other.location.getLongitude()) == 0;
			//@formatter:on
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		//@formatter:off
		return name.hashCode()
				^ Double.valueOf(location.getLatitude()).hashCode()
				^ Double.valueOf(location.getLongitude()).hashCode();
		//@formatter:on
	}

	@Override
	public String toString() {
		return String.format("%s, %s", name, Util.formatLocation(location));
	}

	// For Serializeable
	private void readObject(final ObjectInputStream in)
			throws ClassNotFoundException, IOException {
		this.name = (String) in.readObject();
		final double lat = in.readDouble();
		final double lon = in.readDouble();
		location = new Location("default");
		location.setLatitude(lat);
		location.setLongitude(lon);
	}

	// For Serializeable
	private void writeObject(final ObjectOutputStream out) throws IOException {
		out.writeObject(name);
		final double lat = location.getLatitude();
		final double lon = location.getLongitude();
		out.writeDouble(lat);
		out.writeDouble(lon);
	}
}
