package es.udc.cartolab.gvsig.copyfeature.fieldfillutils;

import java.text.ParseException;

import org.gvsig.fmap.dal.feature.Feature;

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
	public Object execute(Feature feature) {
		String v = feature.getString(field).substring(begin, end);
		return v;
	}
}
