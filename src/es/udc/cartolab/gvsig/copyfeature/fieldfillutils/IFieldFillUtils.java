package es.udc.cartolab.gvsig.copyfeature.fieldfillutils;

import com.hardcode.gdbms.engine.values.Value;
import com.iver.cit.gvsig.fmap.core.IFeature;

public interface IFieldFillUtils {
	
	public void setArguments(String args);
	
	public Value execute(IFeature feature);

}
