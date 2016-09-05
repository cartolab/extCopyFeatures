package es.udc.cartolab.gvsig.copyfeature.fieldfillutils;

import java.text.ParseException;

import org.gvsig.fmap.dal.feature.Feature;
import org.gvsig.fmap.geom.Geometry;
import org.gvsig.fmap.geom.primitive.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GETZ implements IFieldFillUtils {

	private static final Logger logger = LoggerFactory.getLogger(GETZ.class);

	@Override
	public void setArguments(String args) throws ParseException {
	}

	@Override
	public Object execute(Feature feature) {
		int idx = feature.getType().getDefaultGeometryAttributeIndex();
		Geometry geom = feature.getGeometry(idx);
		try {
			Point centroid = geom.centroid();
			if (centroid.getDimension() > 2) {
				return centroid.getCoordinateAt(3);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return 0;
	}

}
