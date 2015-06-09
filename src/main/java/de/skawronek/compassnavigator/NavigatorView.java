package de.skawronek.compassnavigator;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class NavigatorView extends View {
	public static final int UNKNOWN_DISTANCE = -1;
	public static final float UNKNOWN_BEARING = Float.NaN;

	private final Path arrowPath = new Path();
	private final Path betweenPath = new Path();
	private final Paint activeSolidPaint = new Paint();
	private final Paint activeOutlinePaint = new Paint();
	private final Paint inactiveSolidPaint = new Paint();
	private final Paint inactiveOutlinePaint = new Paint();
	private final Paint textPaint = new Paint();
	private final float defaultTextSize;
	private final float defaultMaxTextWidth;
	private final Rect rect = new Rect();

	private int distance = -1; // in Meter [m]
	private float bearing = -1; // in Degree [°]

	public NavigatorView(final Context context) {
		super(context);
		defaultTextSize = textPaint.getTextSize();
		defaultMaxTextWidth = textPaint.measureText("mmmmmmmm");
		init();
	}

	public NavigatorView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		defaultTextSize = textPaint.getTextSize();
		defaultMaxTextWidth = textPaint.measureText("mmmmmmmm");
		init();
	}

	public NavigatorView(final Context context, final AttributeSet attrs,
			final int defStyle) {
		super(context, attrs, defStyle);
		defaultTextSize = textPaint.getTextSize();
		defaultMaxTextWidth = textPaint.measureText("mmmmmmmm");
		init();
	}

	private void init() {
		arrowPath.moveTo(0, -80);
		arrowPath.lineTo(30, -45);
		arrowPath.lineTo(11, -50);
		arrowPath.lineTo(15, 0);
		arrowPath.lineTo(-15, 0);
		arrowPath.lineTo(-11, -50);
		arrowPath.lineTo(-30, -45);
		arrowPath.close();

		betweenPath.moveTo(-10, 0);
		betweenPath.lineTo(10, 0);
		betweenPath.lineTo(10, -56);
		betweenPath.lineTo(-10, -56);
		betweenPath.close();

		activeSolidPaint.setColor(Color.WHITE);
		activeSolidPaint.setStyle(Paint.Style.FILL);
		activeSolidPaint.setAntiAlias(false);

		inactiveSolidPaint.setColor(Color.LTGRAY);
		inactiveSolidPaint.setStyle(Paint.Style.FILL);
		inactiveSolidPaint.setAntiAlias(false);

		activeOutlinePaint.setColor(Color.BLACK);
		activeOutlinePaint.setStyle(Paint.Style.STROKE);
		activeOutlinePaint.setStrokeWidth(4.0f);
		activeOutlinePaint.setStrokeCap(Cap.ROUND);
		activeOutlinePaint.setStrokeJoin(Join.ROUND);
		activeOutlinePaint.setAntiAlias(true);

		inactiveOutlinePaint.setColor(Color.GRAY);
		inactiveOutlinePaint.setStyle(Paint.Style.STROKE);
		inactiveOutlinePaint.setStrokeWidth(4.0f);
		inactiveOutlinePaint.setStrokeCap(Cap.ROUND);
		inactiveOutlinePaint.setStrokeJoin(Join.ROUND);
		inactiveOutlinePaint.setAntiAlias(true);

		textPaint.setTypeface(Typeface.DEFAULT_BOLD);
		textPaint.setColor(Color.BLACK);
		textPaint.setAntiAlias(true);
	}

	public int getDistance() {
		return distance;
	}

	/**
	 * @param distance
	 *            in meter
	 */
	public void setDistance(final int distance) {
		if (distance != UNKNOWN_DISTANCE && distance < 0)
			throw new IllegalArgumentException(
					"Distance must be positive or UNKNOWN_DISTANCE");

		this.distance = distance;
		invalidate();
	}

	public float getBearing() {
		return bearing;
	}

	/**
	 * @param bearing
	 *            0..360 clockwise or UNKNOWN_BEARING
	 */
	public void setBearing(final float bearing) {
		if (Float.compare(UNKNOWN_BEARING, bearing) != 0
				&& (bearing < 0.0f || bearing > 360.0f))
			throw new IllegalArgumentException(
					"Bearing must be in 0..360 or UNKNOWN_BEARING");

		this.bearing = bearing;
		invalidate();
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		getDrawingRect(rect);
		final float outerRadius = Math.min(rect.width(), rect.height()) / 2.0f;
		final float scale = (0.55f / 140.0f) * outerRadius;
		final float innerRadius = outerRadius - 80.0f * scale;

		final int activeArrow;
		if (Float.compare(UNKNOWN_BEARING, bearing) == 0) {
			activeArrow = -1;
		} else {
			activeArrow = getActiveArrow();
		}

		for (int i = 0; i < 16; i++) {
			final float angle = i * 22.5f;
			canvas.save();
			// Center origin
			canvas.translate(outerRadius, outerRadius);

			// Origin = Inner circle
			final double translateX = innerRadius
					* Math.cos((float) Math.toRadians(angle));
			final double translateY = innerRadius
					* Math.sin((float) Math.toRadians(angle));
			canvas.translate((float) translateX, (float) translateY);

			// Turn arrow 90° cw
			canvas.rotate(angle + 90);
			canvas.scale(scale, scale);

			final boolean isActive = (i == activeArrow);

			canvas.drawPath(i % 2 == 0 ? arrowPath : betweenPath,
					isActive ? activeSolidPaint : inactiveSolidPaint);
			canvas.drawPath(i % 2 == 0 ? arrowPath : betweenPath,
					isActive ? activeOutlinePaint : inactiveOutlinePaint);
			canvas.restore();
		}

		final float maxTextWidth = (innerRadius * 2.0f) * 0.9f;
		final float textScale = (maxTextWidth / defaultMaxTextWidth);
		textPaint.setTextSize(defaultTextSize * textScale);

		canvas.save();
		final String textDistance = formatDistance(distance);
		textPaint.setTextAlign(Paint.Align.CENTER);
		canvas.translate(outerRadius, outerRadius * 1.06f);
		canvas.drawText(textDistance, 0, 0, textPaint);
		canvas.restore();
	}

	private int getActiveArrow() {
		final int numArrows = 16; // Divide 360° in 16 segments
		final int rounded = Math.round((bearing - 90) / (360f / numArrows));
		// Avoid nagetive numbers. So to return positive numbers for (a % b)
		// we calculate (a % b + b) % b.
		final int activeArrow = (rounded % numArrows + numArrows) % numArrows;
		assert 0 <= activeArrow && activeArrow <= numArrows;
		return activeArrow;
	}

	// For 1km <= distance < 100km
	private static final NumberFormat DISTANCE_FORMAT = new DecimalFormat(
			"0.#km");

	/**
	 * @param distance
	 *            in meter
	 * @return Formatted distance
	 */
	private String formatDistance(final int distance) {
		if (distance == UNKNOWN_DISTANCE) {
			String string = getResources().getString(R.string.unknown_distance);
			return string;
		} else if (distance < 1000) {
			// Less than 1 km: Show only meters, e.g. "650m"
			return distance + "m";
		} else if (distance < 100000) {
			// Less than 100 km: Show kilometers and hundred meters, e.g.
			// "25.6km"
			final double kilometer = distance / 1000d;
			return DISTANCE_FORMAT.format(kilometer);
		} else {
			// Greater or equal than 100km: Show only kilometers, e.g. "320km"
			final int kilometer = (int) Math.round(distance / 1000.0d);
			return kilometer + "km";
		}
	}
}
