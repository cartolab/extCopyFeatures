package es.udc.cartolab.gvsig.copyfeature.fieldfillutils;

import java.text.ParseException;
import java.util.ArrayList;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.Value;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.ProjectExtension;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.project.documents.ProjectDocument;
import com.iver.cit.gvsig.project.documents.table.ProjectTable;

/**
 * Allows get a value from a table loaded in the project that have a 1:1
 * relation with the source from features are being copied. Format:
 * [targetFieldName]:FOREIGNVALUE([tableFieldName,tableNameInTheProject,
 * foreignKeyBetweenSourceAndTable)
 *
 * The behavior of this expression can not be warranted if the relation is not
 * 1:1
 *
 * tableNameInTheProject can not contain character : or =
 *
 * Be aware that this expression is not build with performance in mind, it's
 * extremely slow
 *
 * @author Francisco Puga <fpuga@cartolab.es> http://conocimientoabierto.es
 *
 */
public class FOREIGNVALUE implements IFieldFillUtils {

    private String commonField;
    private SelectableDataSource tableSDS;
    private int foreignKeyTableFieldIndex;
    private int tableFieldIndex;

    // private String baseQuery;


    public void setArguments(String args) throws ParseException {
	String tokens[] = args.split(",");
	if (tokens.length != 3) {
	    throw new ParseException("Bad Syntax", 0);
	}

	ProjectExtension pe = (ProjectExtension) PluginServices
		.getExtension(ProjectExtension.class);
	ArrayList<ProjectDocument> documents = pe.getProject().getDocuments();
	for (ProjectDocument doc : documents) {
	    if (doc.getName().compareToIgnoreCase(tokens[1]) == 0) {
		if (doc instanceof ProjectTable) {
		    try {
			// tableSDS = ((ProjectTable)
			// doc).getAssociatedTable().getRecordset();
			tableSDS = ((ProjectTable) doc).getModelo()
				.getRecordset();
			foreignKeyTableFieldIndex = tableSDS
				.getFieldIndexByName(tokens[2]);
			tableFieldIndex = tableSDS
				.getFieldIndexByName(tokens[0]);
		    } catch (ReadDriverException e) {
			e.printStackTrace();
			throw new ParseException("Document not exists", 0);
		    }
		    commonField = tokens[2];

		    // baseQuery = "select " + tokens[0] + " from "
		    // + tableSDS.getName() + "  where " + commonField
		    // + "='";
	    }
	}
	}
    }

    public Value execute(IFeature feature, SelectableDataSource sds) {
	Value value = null;
	try {
	    int foreignKeySourceFieldIndex = sds.getFieldIndexByName(commonField);
	    String stringSourceValue = feature.getAttribute(
		    foreignKeySourceFieldIndex).toString();
	    tableSDS.start();
	    for (int i = 0; i < tableSDS.getRowCount(); i++) {
		Value[] tableRow = tableSDS.getRow(i);
		// Value foreignKeyTableValue = tableSDS.getFieldValue(i,
		// foreignKeyTableFieldIndex);

		if (tableRow[foreignKeyTableFieldIndex].toString().equals(
			stringSourceValue)) {
		    value = tableRow[tableFieldIndex];
		    break;
		}

	    }
	    // String query = baseQuery
	    // + feature.getAttribute(foreignKeySourceFieldIndex) + "';";
	    // FilteredDataSource result = (FilteredDataSource)
	    // tableSDS.getDataSourceFactory().executeSQL(query,
	    // DataSourceFactory.MANUAL_OPENING);
	    // if (result.getRowCount() == 1) {
	    // result.start();
	    // value = result.getFieldValue(0, 0);
	    // result.stop();
	    // }
	    tableSDS.stop();

	} catch (Exception e) {
	    e.printStackTrace();
	}

	return value;
    }

}
