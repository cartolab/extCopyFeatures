package es.udc.cartolab.gvsig.copyfeature.fieldfillutils;

import java.text.ParseException;

import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

public class SUBSTRING implements IFieldFillUtils {

    private String field;
    private int begin;
    private int end;

    @Override
    public void setArguments(String args) throws ParseException {
	String tokens[] = args.split(",");
	if (tokens.length != 3) {
	    throw new ParseException("Bad Syntax", 0);
	}
	field = tokens[0];
	begin = Integer.valueOf(tokens[1]);
	end = Integer.valueOf(tokens[2]);
    }

    @Override
    public Value execute(IFeature feature, SelectableDataSource sds) {
	Value value = ValueFactory.createNullValue();
	try {
	    int idx = sds.getFieldIndexByName(field);
	    String v = feature.getAttribute(idx).toString()
		    .substring(begin, end);
	    value = ValueFactory.createValue(v);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return value;
    }
}
