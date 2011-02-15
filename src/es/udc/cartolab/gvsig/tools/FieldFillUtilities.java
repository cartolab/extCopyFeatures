package es.udc.cartolab.gvsig.tools;

import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.fmap.core.IGeometry;

public class FieldFillUtilities {

	public static Value GETX(IGeometry gvGeom) {
		return ValueFactory.createValue(gvGeom.toJTSGeometry().getCoordinate().x);
	}

	public static Value GETY(IGeometry gvGeom) {
		return ValueFactory.createValue(gvGeom.toJTSGeometry().getCoordinate().y);
	}

	public static Value GETZ(IGeometry gvGeom) {
		return ValueFactory.createValue(gvGeom.toJTSGeometry().getCoordinate().z);
	}

}
