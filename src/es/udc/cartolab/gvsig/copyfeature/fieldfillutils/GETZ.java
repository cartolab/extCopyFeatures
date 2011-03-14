package es.udc.cartolab.gvsig.copyfeature.fieldfillutils;

import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.fmap.core.IFeature;

public class GETZ implements IFieldFillUtils {

	public void setArguments(String args) {
	}

	public Value execute(IFeature feature) {
		return ValueFactory.createValue(feature.getGeometry().toJTSGeometry().getCoordinate().z);
	}

}
