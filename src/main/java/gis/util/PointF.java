package gis.util;

public final class PointF {
	public double x;
	public double y;

	public PointF(double x, double y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return "PointF [x=" + x + ", y=" + y + "]";
	}

}
