package es.udc.cartolab.gvsig.copyfeature.fieldfillutils;

import java.text.ParseException;

import org.gvsig.fmap.dal.feature.Feature;

import es.icarto.gvsig.commons.gvsig2.Value;
import es.icarto.gvsig.commons.gvsig2.ValueFactory;

/**
 * Example: type:constant(mytype) will fill the field type with the string
 * mytype. type:cosntant( ) will fill the field with an space character.
 *
 */
public class CONSTANT implements IFieldFillUtils {

	private Value constantValue;

	@Override
	public void setArguments(String args) throws ParseException {
		constantValue = ValueFactory.createValue(args);
	}

	@Override
	public Value execute(Feature feature) {
		return constantValue;
	}

}
