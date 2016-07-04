package es.udc.cartolab.gvsig.copyfeature.fieldfillutils;

import java.text.ParseException;

import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.dal.feature.Feature;

public interface IFieldFillUtils {

	public void setArguments(String args) throws ParseException;

	public Object execute(Feature feature) throws DataException;

}
