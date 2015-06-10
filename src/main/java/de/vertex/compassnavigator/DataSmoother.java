package vertex.compassnavigator;

import java.util.NoSuchElementException;

public final class DataSmoother {
	public static enum Smoothing {
		AVERAGE, MEDIAN
	}

	private final int numSamples;
	private final int dimension;
	private int size = 0;
	private final float[][] buffer;

	public DataSmoother(final int numSamples, final int dimension) {
		if (numSamples < 1) {
			throw new IllegalArgumentException("numSamples " + numSamples
					+ " < 1");
		} else if (dimension < 1) {
			throw new IllegalArgumentException("dimension " + dimension
					+ " < 1");
		}

		this.numSamples = numSamples;
		this.dimension = dimension;
		buffer = new float[numSamples][dimension];
	}

	public void clear() {
		size = 0;
	}

	public int getSize() {
		return size;
	}

	public boolean isEmpty() {
		return getSize() == 0;
	}

	public void put(final float[] data) {
		if (data == null) {
			throw new NullPointerException();
		} else if (data.length != dimension) {
			throw new IllegalArgumentException("data length " + data.length
					+ " != " + dimension);
		}

		// Shift old data left
		for (int sample = 1; sample < numSamples; sample++) {
			for (int i = 0; i < dimension; i++) {
				buffer[sample - 1][i] = buffer[sample][i];
			}
		}
		// Put new data at the end
		for (int i = 0; i < dimension; i++) {
			buffer[numSamples - 1][i] = data[i];
		}

		if (size < numSamples) {
			size++;
		}
	}

	public void getSmoothed(final float[] dest, final Smoothing smoothing) {
		if (dest == null || smoothing == null) {
			throw new NullPointerException();
		} else if (dest.length != dimension) {
			throw new IllegalArgumentException("dest length " + dest.length
					+ " != " + dimension);
		} else if (isEmpty()) {
			throw new NoSuchElementException();
		}

		final float[] temp = new float[size];

		for (int i = 0; i < dimension; i++) {
			for (int sample = numSamples - size; sample < numSamples; sample++) {
				temp[sample - (numSamples - size)] = buffer[sample][i];
			}

			switch (smoothing) {
			case AVERAGE:
				dest[i] = Util.average(temp);
				break;
			case MEDIAN:
				dest[i] = Util.median(temp);
				break;
			default:
				throw new AssertionError();
			}
		}
	}
}
