package es.udc.cartolab.gvsig.copyfeature.fieldfillutils;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gvsig.fmap.dal.feature.Feature;

public class REGEXP implements IFieldFillUtils {

	private Pattern pattern;

	@Override
	public void setArguments(String args) throws ParseException {
		String tokens[] = args.split(",");
		if (tokens.length != 2) {
			throw new ParseException("Bad Syntax", 0);
		}
		pattern = Pattern.compile(tokens[1]);
	}

	@Override
	public Object execute(Feature feature) {
		Matcher m = pattern.matcher("0");
		return m.group();
	}
}
