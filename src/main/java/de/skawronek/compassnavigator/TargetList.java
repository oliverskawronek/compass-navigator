package de.skawronek.compassnavigator;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 final Target t1 = new Target();
 t1.setName("Leipzig, Connewitz, Kreuz");
 t1.setLatitude(51.309728d);
 t1.setLongitude(12.373341);
 Target.addTarget(t1);
 final Target t2 = new Target();
 t2.setName("Leipzig, Cospudner See");
 t2.setLatitude(51.266536d);
 t2.setLongitude(12.338553d);
 Target.addTarget(t2);
 */
public final class TargetList {
	private static TargetList instance;

	private Target selectedTarget = null;

	private final List<Target> targets = new ArrayList<Target>();

	private final List<ITargetListListener> listeners = new ArrayList<ITargetListListener>();

	private TargetList() {
		final Target t1 = new Target();
		t1.setName("Leipzig, Connewitz, Kreuz");
		t1.setLatitude(51.309728d);
		t1.setLongitude(12.373341);
		targets.add(t1);
	}

	public static synchronized TargetList getInstance() {
		if (instance == null) {
			instance = new TargetList();
		}

		return instance;
	}

	public void addListener(final ITargetListListener listener) {
		listeners.add(listener);
	}

	public void removeListner(final ITargetListListener listener) {
		listeners.remove(listener);
	}

	public Target getSelectedTarget() {
		return selectedTarget;
	}

	public void selectTarget(Target selectedTarget) {
		this.selectedTarget = selectedTarget;
		for (ITargetListListener listener : listeners) {
			listener.targetSelected(selectedTarget);
		}
	}

	public void addTarget(final Target newTarget) {
		targets.add(newTarget);
		for (ITargetListListener listener : listeners) {
			listener.targetAdded(newTarget);
		}
	}

	public void removeTarget(final Target target) {
		if (target == selectedTarget) {
			selectedTarget = null;
			for (ITargetListListener listener : listeners) {
				listener.targetSelected(null);
			}
		}
		targets.remove(target);
		for (ITargetListListener listener : listeners) {
			listener.targetRemoved(target);
		}
	}

	public List<Target> getTargets() {
		return Collections.unmodifiableList(targets);
	}

	public synchronized void save(final FileOutputStream stream)
			throws IOException {
		if (stream == null) {
			throw new NullPointerException("stream is null");
		}
		final ObjectOutputStream oos = new ObjectOutputStream(stream);
		oos.writeInt(targets.size());
		for (final Target target : targets) {
			oos.writeObject(target);
		}
		if (selectedTarget != null) {
			assert targets.contains(selectedTarget);
			oos.writeInt(targets.indexOf(selectedTarget));
		} else {
			oos.writeInt(-1);
		}
		oos.close();
	}

	public synchronized void load(final FileInputStream stream)
			throws IOException {
		if (stream == null) {
			throw new NullPointerException("stream is null");
		}
		final ObjectInputStream ois = new ObjectInputStream(stream);
		final int numTargets = ois.readInt();
		targets.clear();
		for (int i = 0; i < numTargets; i++) {
			try {
				targets.add((Target) ois.readObject());
			} catch (ClassNotFoundException e) {
				throw new IOException("unable to read the Target object");
			}
		}
		final int index = ois.readInt();
		if (index != -1) {
			assert index >= 0 && index < numTargets;
			selectedTarget = targets.get(index);
		} else {
			selectedTarget = null;
		}
		ois.close();
	}
}
