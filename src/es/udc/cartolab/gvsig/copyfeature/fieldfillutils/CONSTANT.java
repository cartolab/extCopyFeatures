package es.udc.cartolab.gvsig.copyfeature.fieldfillutils;

import java.text.ParseException;

import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

public class CONSTANT implements IFieldFillUtils {

    private Value constantValue;

    public void setArguments(String args) throws ParseException {
	constantValue = ValueFactory.createValue(args);
    }

    public Value execute(IFeature feature, SelectableDataSource sds) {
	return constantValue;
    }

}
