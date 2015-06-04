package de.skawronek.compassnavigator;

import java.io.Serializable;

public final class Target implements Serializable {
	private static final long serialVersionUID = 1L;

	private String name;
	private double latitude;
	private double longitude;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Target)) {
			return false;
		} else {
			return ((Target) o).name.equals(name);
		}
	}
}
