package es.udc.cartolab.gvsig.copyfeature.fieldfillutils;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

public class REGEXP implements IFieldFillUtils {

    private String field;
    private Pattern pattern;

    @Override
    public void setArguments(String args) throws ParseException {
	String tokens[] = args.split(",");
	if (tokens.length != 2) {
	    throw new ParseException("Bad Syntax", 0);
	}
	field = tokens[0];
	pattern = Pattern.compile(tokens[1]);
    }

    @Override
    public Value execute(IFeature feature, SelectableDataSource sds) {
	Value value = ValueFactory.createNullValue();
	try {
	    int idx = sds.getFieldIndexByName(field);
	    Matcher m = pattern.matcher("0");
	    // feature.getAttribute(idx).toString()
	    value = ValueFactory.createValue(m.group());
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return value;
    }
}
