package es.udc.cartolab.gvsig.tools.exceptions;

import org.gvsig.tools.exception.BaseException;

@SuppressWarnings("serial")
public class ParseException extends BaseException {

	public ParseException(String message, String key, long code) {
		super(message, key, code);
	}

}
