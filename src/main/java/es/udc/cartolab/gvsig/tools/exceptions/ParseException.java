package es.udc.cartolab.gvsig.tools.exceptions;

@SuppressWarnings("serial")
public class ParseException extends Exception {

	public final Object[] args;

	public ParseException(String message, Object... args) {
		super(message);
		this.args = args;
	}

	public Object[] getArgs() {
		return args;
	}
}
