package de.skawronek.compassnavigator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.util.AttributeSet;
import android.widget.ImageView;

public class NavigatorView extends ImageView {
	public static final int INVALID_DISTANCE = -1;
	public static final float INVALID_BEARING = -1.0f;

	private static final Path arrowPath = new Path();
	private static final Path betweenPath = new Path();
	private static final Paint activeSolidPaint = new Paint();
	private static final Paint activeOutlinePaint = new Paint();
	private static final Paint inactiveSolidPaint = new Paint();
	private static final Paint inactiveOutlinePaint = new Paint();
	private static final Paint textPaint = new Paint();
	private static final float defaultTextSize;
	private static final float defaultMaxTextWidth;
	private static final Rect rect = new Rect();

	private int distance = -1; // in Meter
	private float bearing = -1; // in Grad °

	static {
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

		inactiveSolidPaint.setColor(Color.DKGRAY);
		inactiveSolidPaint.setStyle(Paint.Style.FILL);
		inactiveSolidPaint.setAntiAlias(false);

		activeOutlinePaint.setColor(Color.GRAY);
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
		textPaint.setColor(Color.WHITE);
		textPaint.setAntiAlias(true);
		defaultTextSize = textPaint.getTextSize();
		defaultMaxTextWidth = textPaint.measureText("mmmmmmm");
	}

	public NavigatorView(Context context) {
		super(context);
	}

	public NavigatorView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NavigatorView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		if (distance != INVALID_DISTANCE && distance < 0)
			throw new IllegalArgumentException(
					"distance must be positive or INVALID_DISTANCE");

		this.distance = distance;
		invalidate();
	}

	public float getBearing() {
		return bearing;
	}

	public void setBearing(float bearing) {
		if (bearing != INVALID_BEARING && (bearing < 0.0f || bearing > 360.0f))
			throw new IllegalArgumentException(
					"bearing must be in [0,360] or INVALID_BEARING");

		this.bearing = bearing;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		float quantizedBearing = ((int) (((bearing + 270.0f) % 360.0f) / 22.5f)) * 22.5f;

		getDrawingRect(rect);
		final float outerRadius = Math.min(rect.width(), rect.height()) / 2.0f;
		final float scale = (0.55f / 140.0f) * outerRadius;
		final float innerRadius = outerRadius - 80.0f * scale;

		// clear canvas
		canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

		for (int i = 0; i < 16; i++) {
			float angle = i * 22.5f;
			canvas.save();
			// center origin
			canvas.translate(outerRadius, outerRadius);
			
			//origin = inner circle
			canvas.translate(
					innerRadius
							* android.util.FloatMath.cos((float) Math
									.toRadians(angle)),
					innerRadius
							* android.util.FloatMath.sin((float) Math
									.toRadians(angle)));
			
			// Pfeil 90° im Uhrzeigersinn drehen
			canvas.rotate(angle + 90);
			canvas.scale(scale, scale);

			boolean isActive = bearing != INVALID_BEARING
					&& Math.abs(angle - quantizedBearing) < 11.25f;

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
		canvas.translate(outerRadius, outerRadius);
		canvas.drawText(textDistance, 0, 0, textPaint);
		canvas.restore();
	}

	private String formatDistance(final int distance) {
		if (distance == INVALID_DISTANCE) {
			return getContext().getString(R.string.unknown_distance);
		} else if (distance < 1000) {
			return distance + "m";
		} else if (distance < 10000000) {
			final int kilometer = distance / 1000;
			final int meter = (int) Math
					.round((distance - kilometer * 1000) / 100.0d);
			return kilometer + (meter > 0 ? "," + meter : "") + "km";
		} else {
			final int kilometer = (int) Math.round(distance / 1000.0d);
			return kilometer + "km";
		}
	}
}
