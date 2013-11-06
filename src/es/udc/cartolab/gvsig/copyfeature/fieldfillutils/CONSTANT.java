package es.udc.cartolab.gvsig.copyfeature.fieldfillutils;

import java.text.ParseException;

import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

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
    public Value execute(IFeature feature, SelectableDataSource sds) {
	return constantValue;
    }

}
