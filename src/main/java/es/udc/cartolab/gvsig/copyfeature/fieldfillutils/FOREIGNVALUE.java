package es.udc.cartolab.gvsig.copyfeature.fieldfillutils;

import java.text.ParseException;
import java.util.List;

import org.gvsig.andami.PluginServices;
import org.gvsig.app.extension.ProjectExtension;
import org.gvsig.app.project.documents.Document;
import org.gvsig.app.project.documents.table.TableDocument;
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.dal.feature.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.icarto.gvsig.commons.gvsig2.SelectableDataSource;
import es.icarto.gvsig.commons.gvsig2.Value;

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

	private static final Logger logger = LoggerFactory.getLogger(FOREIGNVALUE.class);

	private String commonField;
	private SelectableDataSource tableSDS;
	private int foreignKeyTableFieldIndex;
	private int tableFieldIndex;

	// private String baseQuery;

	@Override
	public void setArguments(String args) throws ParseException {
		String tokens[] = args.split(",");
		if (tokens.length != 3) {
			throw new ParseException("Bad Syntax", 0);
		}

		ProjectExtension pe = (ProjectExtension) PluginServices.getExtension(ProjectExtension.class);
		List<Document> documents = pe.getProject().getDocuments();
		for (Document doc : documents) {
			if (doc.getName().compareToIgnoreCase(tokens[1]) == 0) {
				if (doc instanceof TableDocument) {
					try {
						// tableSDS = ((ProjectTable)
						// doc).getAssociatedTable().getRecordset();
						TableDocument d = (TableDocument) doc;
						tableSDS = new SelectableDataSource(d.getStore());
						foreignKeyTableFieldIndex = tableSDS.getFieldIndexByName(tokens[2]);
						tableFieldIndex = tableSDS.getFieldIndexByName(tokens[0]);
					} catch (DataException e) {
						logger.error(e.getMessage(), e);
						throw new ParseException("Document not exists", 0);
					}
					commonField = tokens[2];

					// baseQuery = "select " + tokens[0] + " from "
					// + tableSDS.getName() + " where " + commonField
					// + "='";
				}
			}
		}
	}

	@Override
	public Object execute(Feature feature) throws DataException {
		Value value = null;

		String stringSourceValue = feature.getString(commonField);

		for (long i = 0, lasti = tableSDS.getRowCount(); i < lasti; i++) {
			Value[] tableRow = tableSDS.getRow(i).getAttributes();
			// Value foreignKeyTableValue = tableSDS.getFieldValue(i,
			// foreignKeyTableFieldIndex);

			if (tableRow[foreignKeyTableFieldIndex].toString().equals(stringSourceValue)) {
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

		return value;
	}

}
