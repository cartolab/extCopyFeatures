package es.udc.cartolab.gvsig.copyfeature.fieldfillutils;

import java.text.ParseException;

import com.hardcode.gdbms.engine.values.Value;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

public interface IFieldFillUtils {
	
	public void setArguments(String args) throws ParseException;
	
	public Value execute(IFeature feature, SelectableDataSource sds);

}
