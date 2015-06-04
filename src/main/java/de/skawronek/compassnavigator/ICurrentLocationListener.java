package de.skawronek.compassnavigator;

import android.location.Location;

public interface ICurrentLocationListener {
	public void locationChanged(Location newLocation);
}
