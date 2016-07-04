package es.udc.cartolab.gvsig.copyfeature.fieldfillutils;

import java.text.ParseException;

import org.gvsig.fmap.dal.feature.Feature;
import org.gvsig.fmap.geom.Geometry;
import org.gvsig.fmap.geom.generalpath.util.Converter;

public class GETY implements IFieldFillUtils {

	@Override
	public void setArguments(String args) throws ParseException {
	}

	@Override
	public Object execute(Feature feature) {
		int idx = feature.getType().getDefaultGeometryAttributeIndex();
		Geometry geom = feature.getGeometry(idx);
		com.vividsolutions.jts.geom.Geometry point = Converter
				.geometryToJts(geom);
		return point.getCoordinate().y;
	}

}
