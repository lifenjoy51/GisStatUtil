package gis.util;

import org.geotoolkit.geometry.DirectPosition2D;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

public class GeoConverter {
	private static int utm_zone(double _long) {
		assert _long > -180.0 && _long < 180.0;
		return (int) (Math.ceil((180 + _long) / 6.0));
	}

	public static PointF wgs2tm(double longitude, double latitude)
			throws MismatchedDimensionException, TransformException {
		DirectPosition dp = new DirectPosition2D(longitude, latitude);
		final int zone = utm_zone(longitude);
		MathTransform crs_transform;
		try {
			CoordinateReferenceSystem wgs84 = DefaultGeographicCRS.WGS84; // EPSG:4326
			CoordinateReferenceSystem utm = CRS.decode("EPSG:5181");
			crs_transform = CRS.findMathTransform(wgs84, utm);
		} catch (NoSuchAuthorityCodeException e) {
			throw new RuntimeException(e);
		} catch (FactoryException e) {
			throw new RuntimeException(e);
		}
		DirectPosition p2 = crs_transform.transform(dp, null);
		double[] c = p2.getCoordinate();
		System.out.println(String.format("%d, %f, %f", zone, c[0], c[1]));

		return new PointF(c[0], c[1]);

	}

	public static PointF tm2wgs(double x, double y)
			throws MismatchedDimensionException, TransformException {
		DirectPosition dp = new DirectPosition2D(x, y);
		MathTransform crs_transform;
		try {
			CoordinateReferenceSystem utm = CRS.decode("EPSG:5181");
			CoordinateReferenceSystem wgs84 = DefaultGeographicCRS.WGS84; // EPSG:4326
			crs_transform = CRS.findMathTransform(utm, wgs84);
		} catch (NoSuchAuthorityCodeException e) {
			throw new RuntimeException(e);
		} catch (FactoryException e) {
			throw new RuntimeException(e);
		}
		DirectPosition p2 = crs_transform.transform(dp, null);
		double[] c = p2.getCoordinate();
		System.out.println(String.format("%f, %f", c[0], c[1]));

		return new PointF(c[0], c[1]);

	}

	public static void main(String[] args) throws MismatchedDimensionException, TransformException {
		GeoConverter.wgs2tm(127, 38);
		GeoConverter.tm2wgs(500000, 200000);
	}
}