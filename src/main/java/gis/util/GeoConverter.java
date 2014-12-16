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

	/**
	 * 
	 * @param latitude
	 * @param longitude
	 * @return
	 * @throws MismatchedDimensionException
	 * @throws TransformException
	 */
	public static PointF wgs2tm(double latitude, double longitude)
			throws MismatchedDimensionException, TransformException {
		DirectPosition dp = new DirectPosition2D(longitude, latitude);
		final int zone = utm_zone(longitude);
		MathTransform crs_transform;
		try {
			CoordinateReferenceSystem wgs84 = DefaultGeographicCRS.WGS84; // EPSG:4326
			CoordinateReferenceSystem utm = CRS.decode("EPSG:2097");
			crs_transform = CRS.findMathTransform(wgs84, utm);
		} catch (NoSuchAuthorityCodeException e) {
			throw new RuntimeException(e);
		} catch (FactoryException e) {
			throw new RuntimeException(e);
		}
		DirectPosition p2 = crs_transform.transform(dp, null);
		double[] c = p2.getCoordinate();
		//System.out.println(String.format("%d, %f, %f", zone, c[1], c[0]));

		return new PointF(c[1], c[0]);

	}

	/**
	 * 
	 * @param latitude
	 * @param longitude
	 * @return
	 * @throws MismatchedDimensionException
	 * @throws TransformException
	 */
	public static PointF tm2wgs(double latitude, double longitude)
			throws MismatchedDimensionException, TransformException {
		DirectPosition dp = new DirectPosition2D(longitude, latitude);
		MathTransform crs_transform;
		try {
			CoordinateReferenceSystem utm = CRS.decode("EPSG:2097");
			CoordinateReferenceSystem wgs84 = CRS.decode("EPSG:4326"); //DefaultGeographicCRS.WGS84; // EPSG:4326
			crs_transform = CRS.findMathTransform(utm, wgs84, true);
		} catch (NoSuchAuthorityCodeException e) {
			throw new RuntimeException(e);
		} catch (FactoryException e) {
			throw new RuntimeException(e);
		}
		DirectPosition p2 = crs_transform.transform(dp, null);
		double[] c = p2.getCoordinate();
		//System.out.println(String.format("%f, %f", c[1], c[0]));

		return new PointF(c[1], c[0]);

	}

	public static void main(String[] args) throws MismatchedDimensionException,
			TransformException {
		//PointF p1 = GeoConverter.wgs2tm(37.608298202892925, 126.95091548864121);
		//System.out.println(p1);
		PointF p2 = GeoConverter.tm2wgs(195598, 456221);
		System.out.println(p2);
		//PointF [x=126.9501417466901, y=	37.606094787622794]
		//			126.9509154886412		37.608298202892925
		//			  0.0007737419511		 0.002203415270131
		
		PointF p3 = GeoConverter.tm2wgs(155598, 356221);
		System.out.println(p3);
		//PointF [x=126.50303274010484, y=	36.70390799169085]
		//			126.50387183776607		36.70621304872709
		//			  0.00083909766123		 0.00230505703624
		
		PointF p4 = GeoConverter.tm2wgs(214005, 256243);
		System.out.println(p4);
		//PointF [x=127.15496295769653, y=	35.80374669616551]
		//			127.15576732000366		35.80617170307602
		//			  0.00080436230713		 0.00242500691051
		
		//EPSG:9999	PointF [x=126.950 91548864121, y=37.608 298202892925]
		//EPSG:2097	PointF [x=126.950 1417466901,  y=37.606 094787622794]
		
		//EPSG:9999	PointF [x=126.950 91548864121, y=37.608 298202892925]
		//EPSG:5181	PointF [x=126.950 14768805734, y=37.605 55829916078]
		
		//EPSG:5186	PointF [x=126.95073579695696, y=36.70449993724312]
		//EPSG:5174	PointF [x=126.95303202446784, y=37.606094787622794]
		
	}
}
