package es.udc.cartolab.gvsig.tools;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import net.miginfocom.swing.MigLayout;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.BooleanValue;
import com.hardcode.gdbms.engine.values.DoubleValue;
import com.hardcode.gdbms.engine.values.IntValue;
import com.hardcode.gdbms.engine.values.StringValue;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiFrame.MDIFrame;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.CADExtension;
import com.iver.cit.gvsig.ProjectExtension;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileWriteException;
import com.iver.cit.gvsig.exceptions.validate.ValidateRowException;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.edition.DefaultRowEdited;
import com.iver.cit.gvsig.fmap.edition.EditionEvent;
import com.iver.cit.gvsig.fmap.edition.IRowEdited;
import com.iver.cit.gvsig.fmap.edition.VectorialEditableAdapter;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.gui.cad.DefaultCADTool;
import com.iver.cit.gvsig.layers.VectorialLayerEdited;
import com.iver.cit.gvsig.project.documents.ProjectDocument;
import com.iver.cit.gvsig.project.documents.table.ProjectTable;
import com.iver.cit.gvsig.project.documents.view.gui.View;

import es.udc.cartolab.gvsig.copyfeature.fieldfillutils.IFieldFillUtils;
import es.udc.cartolab.gvsig.navtable.ToggleEditing;

//TODO Implemente copy DATES
/**
 * Copy features with attributes between layers. Type conversion is made if
 * possible.
 * 
 * @author Nacho Varela
 * @author Francisco Puga <fpuga@cartolab.es> http://conocimientoabierto.es
 * 
 */
@SuppressWarnings("serial")
public class CopyFeaturesDialog extends JPanel implements IWindow,
	ActionListener {

    private View view = null;
    FLayers layers = null;

    private String message = null;

    private WindowInfo viewInfo = null;
    private JButton cancelButton = null;
    private JButton okButton = null;
    private JButton matchFileButton = null;
    private JPanel panelButtons = null;
    private JComboBox sourceLayerCB = null;
    private JComboBox targetLayerCB = null;
    private JTextField matchFileTF = null;

    private JCheckBox onlySelectedChB = null;

    @Override
    public WindowInfo getWindowInfo() {
	if (viewInfo == null) {
	    viewInfo = new WindowInfo(WindowInfo.MODALDIALOG
		    | WindowInfo.RESIZABLE);
	    viewInfo.setTitle(PluginServices.getText(this, "_copy_features"));
	    Dimension dim = getPreferredSize();
	    MDIFrame a = (MDIFrame) PluginServices.getMainFrame();
	    int maxHeight = a.getHeight() - 175;
	    int maxWidth = a.getWidth() - 15;

	    int width, heigth = 0;
	    if (dim.getHeight() > maxHeight) {
		heigth = maxHeight;
	    } else {
		heigth = new Double(dim.getHeight()).intValue();
	    }
	    if (dim.getWidth() > maxWidth) {
		width = maxWidth;
	    } else {
		width = new Double(dim.getWidth()).intValue();
	    }
	    viewInfo.setWidth(width + 20);
	    viewInfo.setHeight(heigth + 15);
	}
	return viewInfo;
    }

    public CopyFeaturesDialog() {
	super();
	try {
	    initialize();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private JButton getOkButton() {
	if (okButton == null) {
	    okButton = new JButton();
	    okButton.setText(PluginServices.getText(this, "OK"));
	    okButton.addActionListener(this);
	}
	return okButton;
    }

    private JButton getCancelButton() {
	if (cancelButton == null) {
	    cancelButton = new JButton();
	    cancelButton.setText(PluginServices.getText(this, "Cancel"));
	    cancelButton.addActionListener(this);
	}
	return cancelButton;
    }

    private JPanel getJPanelButtons() {
	if (panelButtons == null) {
	    panelButtons = new JPanel();
	    panelButtons.add(getOkButton());
	    panelButtons.add(getCancelButton());
	}
	return panelButtons;
    }

    protected void setMessage(String string) {
	message = string;
    }

    protected String getMessage() {
	return message;
    }

    private void fillVectorWithTables(Vector<String> layerNames) {
	// fpuga. Instead of the names we should have a custom model for the
	// combobox to get directly the SelectableDataSource

	// or at least use getDocumentsOfType and avoid the loop
	ProjectExtension pe = (ProjectExtension) PluginServices
		.getExtension(ProjectExtension.class);
	for (ProjectDocument doc : pe.getProject().getDocuments()) {
	    if (doc instanceof ProjectTable) {
		layerNames.add(doc.getName());
	    }
	}

    }

    private void fillVectorWithVectLayersOfToc(Vector<String> layerNames,
	    FLayers layers) {
	for (int i = layers.getLayersCount() - 1; i >= 0; i--) {
	    FLayer l = layers.getLayer(i);
	    if (l instanceof FLyrVect) {
		layerNames.add(l.getName());
	    } else if (l instanceof FLayers) {
		fillVectorWithVectLayersOfToc(layerNames, (FLayers) l);
	    }
	}
    }

    private void initialize() throws Exception {

	MigLayout layout = new MigLayout("center", "[][70%][]",
		"[][]10[]10[]15%[bottom]");
	super.setLayout(layout);

	try {
	    view = (View) PluginServices.getMDIManager().getActiveWindow();

	    if (view != null) {

		MapControl map = view.getMapControl();
		layers = map.getMapContext().getLayers();

		setMessage(null);
		if (layers.getLayersCount() == 0) {
		    setMessage("not_layers_in_TOC");
		    return;
		}

		Vector<String> layerNames = new Vector<String>();
		fillVectorWithVectLayersOfToc(layerNames, layers);
		// fillVectorWithTables(layerNames);

		JLabel sourceLayerLabel = new JLabel(PluginServices.getText(
			this, "SourceLayer"));
		super.add(sourceLayerLabel);
		sourceLayerCB = new JComboBox(layerNames);
		super.add(sourceLayerCB, "span 2, growx, wrap");

		JLabel targetLayerLabel = new JLabel(PluginServices.getText(
			this, "TargetLayer"));
		super.add(targetLayerLabel);

		targetLayerCB = new JComboBox(layerNames);
		super.add(targetLayerCB, "span 2, growx, wrap");

		JPanel matchPanel = new JPanel(new MigLayout("center",
			"[][70%][]", ""));
		Border border = BorderFactory.createTitledBorder(PluginServices
			.getText(this, "Matching"));
		matchPanel.setBorder(border);

		// TODO More options
		// TODO Only selected / all feaures
		// JRadioButton radioAllAttrib = new
		// JRadioButton(PluginServices.getText(this, "All attributes"));
		// JRadioButton radioSameAttrib = new
		// JRadioButton(PluginServices.getText(this,
		// "Add same attributes"));
		// ButtonGroup group = new ButtonGroup();

		JLabel matchFileLabel = new JLabel(PluginServices.getText(this,
			"MatchFile"));
		matchPanel.add(matchFileLabel);

		matchFileTF = new JTextField();
		matchFileTF.setText("");

		matchPanel.add(matchFileTF, "growx");

		matchFileButton = new JButton("...");
		matchFileButton.addActionListener(this);
		matchPanel.add(matchFileButton, "wrap");

		super.add(matchPanel, "span 3, grow, wrap");

		JPanel featPanel = new JPanel(new MigLayout("left"));
		border = BorderFactory.createTitledBorder(PluginServices
			.getText(this, "Features"));
		featPanel.setBorder(border);

		onlySelectedChB = new JCheckBox(PluginServices.getText(this,
			"OnlySelected"));
		featPanel.add(onlySelectedChB);

		super.add(featPanel, "span 3, grow, wrap");

		JPanel buttonPanel = getJPanelButtons();
		super.add(buttonPanel, "span 3, grow");
	    }

	    else {
		setMessage("not_view_to_work_on");
	    }

	} catch (ClassCastException e) {
	    setMessage("void_view");
	}
    }

    public static void createFeature(ToggleEditing te, FLyrVect vectLayer,
	    IGeometry feature, Value[] values) throws IOException {

	VectorialLayerEdited vle = (VectorialLayerEdited) CADExtension
		.getEditionManager().getActiveLayerEdited();
	VectorialEditableAdapter vea = vle.getVEA();

	try {
	    String newFID;
	    newFID = vea.getNewFID();

	    DefaultFeature df = new DefaultFeature(feature, values, newFID);
	    int index = vea.addRow(df, "_newET", EditionEvent.GRAPHIC);
	    // clearSelection();
	    ArrayList<DefaultRowEdited> selectedRow = vle.getSelectedRow();
	    ViewPort vp = vle.getLayer().getMapContext().getViewPort();
	    BufferedImage selectionImage = new BufferedImage(
		    vp.getImageWidth(), vp.getImageHeight(),
		    BufferedImage.TYPE_INT_ARGB);
	    Graphics2D gs = selectionImage.createGraphics();
	    int inversedIndex = vea.getInversedIndex(index);
	    selectedRow.add(new DefaultRowEdited(df, IRowEdited.STATUS_ADDED,
		    inversedIndex));
	    vea.getSelection().set(inversedIndex);
	    IGeometry geom = df.getGeometry();
	    geom.cloneGeometry().draw(gs, vp, DefaultCADTool.selectionSymbol);
	    vle.drawHandlers(geom.cloneGeometry(), gs, vp);
	    vea.setSelectionImage(selectionImage);

	    SelectableDataSource recordset = vectLayer.getRecordset();
	    recordset.clearSelection();

	} catch (ExpansionFileWriteException e) {
	    e.printStackTrace();
	} catch (ValidateRowException e) {
	    e.printStackTrace();
	} catch (ReadDriverException e) {
	    e.printStackTrace();
	}

    }

    public void copyData(String targetLayerName, String sourceLayerName,
	    boolean onlySelected, String match_filepath) {

	ReadableVectorial sourceFeats = null;
	boolean error = false;
	String errorMessage = "";
	boolean isEdited = false;

	int copyCount = 0;

	es.udc.cartolab.gvsig.navtable.ToggleEditing te = new es.udc.cartolab.gvsig.navtable.ToggleEditing();

	FLyrVect sourceLayer = (FLyrVect) layers.getLayer(sourceLayerName);
	FLyrVect targetLayer = (FLyrVect) layers.getLayer(targetLayerName);

	try {
	    sourceFeats = sourceLayer.getSource();
	    sourceFeats.start();

	    SelectableDataSource targetRecordset = targetLayer.getRecordset();
	    SelectableDataSource sourceRecordset = sourceFeats.getRecordset();

	    MatchingFileParser parser = new MatchingFileParser(match_filepath);
	    HashMap<Integer, Integer> tgtSrcIdxMap = parser.getMatchingMap(
		    sourceRecordset, targetRecordset);
	    HashMap<Integer, IFieldFillUtils> calculatedFieldsMap = parser
		    .getCalculatedFieldsMap(targetRecordset);

	    int fieldsNumber = targetRecordset.getFieldCount();

	    te.startEditing(targetLayer);
	    isEdited = true;

	    FBitSet bitset = sourceRecordset.getSelection();

	    int number2copy = 0;
	    if (onlySelected) {
		number2copy = bitset.cardinality();
	    } else {
		number2copy = (int) sourceRecordset.getRowCount();
	    }

	    int val = JOptionPane.showOptionDialog(
		    this,
		    PluginServices.getText(this, "Se van a copiar "
			    + number2copy + " entidades de la capa \""
			    + sourceLayerName + "\"\n a la capa \""
			    + targetLayerName + "\".\n¿Desea continuar?"),
		    "Copy Features Confirmation", JOptionPane.OK_CANCEL_OPTION,
		    JOptionPane.QUESTION_MESSAGE, null, new String[] { "Ok",
			    "Cancelar" }, "Ok");

	    if (val != 0) {
		return;
	    }

	    for (int i = 0; i < sourceRecordset.getRowCount(); i++) {

		if (onlySelected && !bitset.get(i)) {
		    continue;
		}
		IGeometry gvGeom = sourceFeats.getShape(i);

		// TODO: pull up the getProjection and comparison to improve
		// performance. A test must be done first
		if (sourceLayer.getProjection() != sourceLayer.getMapContext()
			.getProjection()) {
		    gvGeom.reProject(sourceLayer.getProjection().getCT(
			    sourceLayer.getMapContext().getProjection()));
		}

		// TODO: maybe is faster create a nullValues object out of the
		// loop and clone it here
		Value[] values = new Value[fieldsNumber];
		for (int j = 0; j < values.length; j++) {
		    values[j] = ValueFactory.createNullValue();
		}

		for (int tgtIdx : tgtSrcIdxMap.keySet()) {

		    int srcIdx = tgtSrcIdxMap.get(tgtIdx);
		    Value srcValue = sourceRecordset.getFieldValue(i, srcIdx);
		    int tgtType = targetRecordset.getFieldType(tgtIdx);
		    values[tgtIdx] = getValue(srcValue, tgtType);
		}

		// getFeature is a slow process, better get it just once, and
		// not do it inside the loop
		if (!calculatedFieldsMap.isEmpty()) {
		    IFeature feature = sourceFeats.getFeature(i);
		    for (int tgtIdx : calculatedFieldsMap.keySet()) {
			IFieldFillUtils util = calculatedFieldsMap.get(Integer
				.valueOf(tgtIdx));
			values[tgtIdx] = util.execute(feature, sourceRecordset);
		    }
		}

		createFeature(te, targetLayer, gvGeom, values);
		copyCount++;

	    }
	    sourceFeats.stop();
	} catch (ParseException e) {
	    error = true;
	    errorMessage = String.format(
		    PluginServices.getText(this, "bad_syntax"),
		    e.getErrorOffset());
	} catch (Exception e) {
	    error = true;
	    errorMessage = PluginServices.getText(this,
		    "ERROR: Se han copiado " + copyCount + " entidades.");
	    e.printStackTrace();
	} finally {

	    if (isEdited) {
		te.stopEditing(targetLayer, false);
	    }

	    if (error) {
		JOptionPane.showMessageDialog(this, errorMessage, "Error",
			JOptionPane.ERROR_MESSAGE);
	    } else {
		JOptionPane.showMessageDialog(
			this,
			PluginServices.getText(this, "Se han copiado "
				+ copyCount + " entidades."), "Information",
			JOptionPane.INFORMATION_MESSAGE);
	    }
	    PluginServices.getMDIManager().closeWindow(this);
	}

    }

    private Value getValue(Value srcValue, int tgtType) {
	Value value = null;
	switch (tgtType) {
	case java.sql.Types.CHAR:
	case java.sql.Types.LONGVARCHAR:
	case java.sql.Types.VARCHAR:
	    value = ValueFactory.createValue(srcValue.toString());
	    break;
	case java.sql.Types.TINYINT:
	case java.sql.Types.SMALLINT:
	case java.sql.Types.BIGINT:
	case java.sql.Types.INTEGER:
	    if (srcValue instanceof StringValue) {
		String aux = srcValue.toString().replace("\"", "");
		try {
		    // fpuga: Maybe is better don't convert from real numbers,
		    // but others time this is a
		    // useful feature
		    int auxInt = (int) Math.round(Double.parseDouble(aux));
		    value = ValueFactory.createValue(auxInt);
		} catch (NumberFormatException e) {
		    // TODO
		}
	    }
	    if (srcValue instanceof IntValue) {
		IntValue v = (IntValue) srcValue;
		value = ValueFactory.createValue(v.intValue());
	    }
	    if (srcValue instanceof DoubleValue) {
		DoubleValue v = (DoubleValue) srcValue;
		value = ValueFactory.createValue(v.intValue());
	    }
	    if (srcValue instanceof BooleanValue) {
		BooleanValue v = (BooleanValue) srcValue;
		if (v.getValue() == false) {
		    value = ValueFactory.createValue(0);
		} else {
		    value = ValueFactory.createValue(1);
		}
	    }
	    break;
	case java.sql.Types.DECIMAL:
	case java.sql.Types.FLOAT:
	case java.sql.Types.NUMERIC:
	case java.sql.Types.DOUBLE:
	    if (srcValue instanceof StringValue) {
		String aux = srcValue.toString().replace("\"", "");
		try {
		    double auxDouble = Double.parseDouble(aux);
		    value = ValueFactory.createValue(auxDouble);
		} catch (NumberFormatException e) {
		    // TODO
		}
	    }
	    if (srcValue instanceof IntValue) {
		IntValue v = (IntValue) srcValue;
		value = ValueFactory.createValue(v.doubleValue());
	    }
	    if (srcValue instanceof DoubleValue) {
		DoubleValue v = (DoubleValue) srcValue;
		value = ValueFactory.createValue(v.doubleValue());
	    }
	    if (srcValue instanceof BooleanValue) {
		BooleanValue v = (BooleanValue) srcValue;
		if (v.getValue() == false) {
		    value = ValueFactory.createValue(0.0);
		} else {
		    value = ValueFactory.createValue(1.0);
		}
	    }
	    break;
	case java.sql.Types.BIT:
	case java.sql.Types.BOOLEAN:
	    String aux = srcValue.toString();
	    if ((aux.toUpperCase() == "FALSE") || (aux == "0")
		    || (aux == "0.0") || (aux.toUpperCase() == "NO")) {
		value = ValueFactory.createValue(false);
	    } else {
		value = ValueFactory.createValue(true);
	    }
	    break;
	default:
	    break;
	}
	return value;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

	if (e.getSource() == okButton) {

	    String sourceLayerName = sourceLayerCB.getSelectedItem().toString();
	    String targetLayerName = targetLayerCB.getSelectedItem().toString();

	    if (sourceLayerName.equalsIgnoreCase(targetLayerName)) {
		JOptionPane.showMessageDialog(this,
			PluginServices.getText(this, "sameLayerError"),
			"Error", JOptionPane.ERROR_MESSAGE);
	    } else {
		PluginServices.getMDIManager().setWaitCursor();
		copyData(targetLayerName, sourceLayerName,
			onlySelectedChB.isSelected(), matchFileTF.getText());
		PluginServices.getMDIManager().restoreCursor();
	    }
	}

	if (e.getSource() == cancelButton) {
	    PluginServices.getMDIManager().closeWindow(this);
	}

	if (e.getSource() == matchFileButton) {

	    CopyFeaturesExtension cfe = ((CopyFeaturesExtension) PluginServices
		    .getExtension(CopyFeaturesExtension.class));
	    String defaultPath = cfe.getDefaultPath();

	    JFileChooser chooser = new JFileChooser(defaultPath);
	    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

	    int returnVal = chooser.showOpenDialog(this);
	    if (returnVal == JFileChooser.APPROVE_OPTION) {
		File selectedFile = chooser.getSelectedFile();
		matchFileTF.setText(selectedFile.getAbsolutePath());
		cfe.setDefaultPath(selectedFile.getParent());

	    }

	}
    }

    @Override
    public Object getWindowProfile() {
	return null;
    }

}
