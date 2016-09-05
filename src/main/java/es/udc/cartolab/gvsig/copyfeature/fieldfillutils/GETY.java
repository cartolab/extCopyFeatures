package es.udc.cartolab.gvsig.copyfeature.fieldfillutils;

import java.text.ParseException;

import org.gvsig.fmap.dal.feature.Feature;
import org.gvsig.fmap.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GETY implements IFieldFillUtils {

	private static final Logger logger = LoggerFactory.getLogger(GETY.class);

	@Override
	public void setArguments(String args) throws ParseException {
	}

	@Override
	public Object execute(Feature feature) {
		int idx = feature.getType().getDefaultGeometryAttributeIndex();
		Geometry geom = feature.getGeometry(idx);
		try {
			return geom.centroid().getY();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return 0;
	}
}
