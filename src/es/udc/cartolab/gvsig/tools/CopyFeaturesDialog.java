package es.udc.cartolab.gvsig.tools;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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

import com.hardcode.driverManager.DriverLoadException;
import com.hardcode.gdbms.engine.values.BooleanValue;
import com.hardcode.gdbms.engine.values.DoubleValue;
import com.hardcode.gdbms.engine.values.IntValue;
import com.hardcode.gdbms.engine.values.StringValue;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.CADExtension;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.drivers.DriverIOException;
import com.iver.cit.gvsig.fmap.edition.DefaultRowEdited;
import com.iver.cit.gvsig.fmap.edition.EditionEvent;
import com.iver.cit.gvsig.fmap.edition.IRowEdited;
import com.iver.cit.gvsig.fmap.edition.VectorialEditableAdapter;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.gui.cad.DefaultCADTool;
import com.iver.cit.gvsig.layers.VectorialLayerEdited;
import com.iver.cit.gvsig.project.documents.view.gui.View;

import es.udc.cartolab.gvsig.navtable.ToggleEditing;

public class CopyFeaturesDialog extends JPanel implements IWindow, ActionListener{

	private View view = null;
	FLayers layers = null;

	private String message = null;

	private WindowInfo viewInfo = null;
	private JButton cancelButton = null;
	private JButton okButton = null;
	private JButton matchFileButton  = null;
	private JPanel panelButtons = null;
	private JComboBox sourceLayerCB = null;
	private JComboBox targetLayerCB = null;
	private JTextField matchFileTF = null;

	private JCheckBox onlySelectedChB = null;

	public WindowInfo getWindowInfo() {
		if (viewInfo == null) {
			viewInfo = new WindowInfo(WindowInfo.MODALDIALOG | WindowInfo.RESIZABLE | WindowInfo.PALETTE);
			viewInfo.setTitle(PluginServices.getText(this, "copyfeatures"));
			viewInfo.setWidth(300);
			viewInfo.setHeight(200);
		}
		return viewInfo;
	}

	public CopyFeaturesDialog() {
		super();
		try {
			initialize();
		} catch (Exception e) {
			// TODO Auto-generated catch block
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

	private void initialize() throws Exception {

		MigLayout layout = new MigLayout("center", "[][70%][]", "[][]10[]10[]20[bottom]");
		super.setLayout(layout);

		try {
			view = (View) PluginServices.getMDIManager().getActiveWindow();

			if (view != null){

				MapControl map = view.getMapControl();
				layers = map.getMapContext().getLayers();

				setMessage(null);
				if (layers.getLayersCount() == 0) {
					setMessage("not_layers_in_TOC");
					return;
				}

				JLabel sourceLayerLabel = new JLabel(PluginServices.getText(this, "SourceLayer"));
				super.add(sourceLayerLabel);

				sourceLayerCB = new JComboBox();
				for (int i=layers.getLayersCount()-1; i >= 0; i--) {
					sourceLayerCB.addItem(layers.getLayer(i).getName());
				}
				super.add(sourceLayerCB, "span 2, growx, wrap");

				JLabel targetLayerLabel = new JLabel(PluginServices.getText(this, "TargetLayer"));
				super.add(targetLayerLabel);

				targetLayerCB = new JComboBox();
				for (int i=layers.getLayersCount()-1; i >= 0; i--) {
					targetLayerCB.addItem(layers.getLayer(i).getName());
				}

				if (targetLayerCB.getItemCount() > 1){
					targetLayerCB.setSelectedIndex(1);
				}

				super.add(targetLayerCB, "span 2, growx, wrap");

				JPanel matchPanel = new JPanel(new MigLayout("center", "[][70%][]",""));
				Border border = BorderFactory.createTitledBorder(PluginServices.getText(this, "Matching"));
				matchPanel.setBorder(border);

				// TODO More options
				// TODO Only selected / all feaures
				//				JRadioButton radioAllAttrib = new JRadioButton(PluginServices.getText(this, "All attributes"));
				//				JRadioButton radioSameAttrib = new JRadioButton(PluginServices.getText(this, "Add same attributes"));
				//				ButtonGroup group = new ButtonGroup();

				JLabel matchFileLabel = new JLabel(PluginServices.getText(this, "MatchFile"));
				matchPanel.add(matchFileLabel);

				matchFileTF = new JTextField();
				matchFileTF.setText("");

				matchPanel.add(matchFileTF, "growx");

				matchFileButton = new JButton("...");
				matchFileButton.addActionListener(this);
				matchPanel.add(matchFileButton, "wrap");

				super.add(matchPanel, "span 3, grow, wrap");

				JPanel featPanel = new JPanel(new MigLayout("left"));
				border = BorderFactory.createTitledBorder(PluginServices.getText(this, "Features"));
				featPanel.setBorder(border);

				onlySelectedChB = new JCheckBox(PluginServices.getText(this, "OnlySelected"));
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

	public static void createFeature(ToggleEditing te, FLyrVect vectLayer, IGeometry feature, Value[] values) {

		VectorialLayerEdited vle = (VectorialLayerEdited) CADExtension.getEditionManager().getActiveLayerEdited();
		VectorialEditableAdapter vea = vle.getVEA();

		try  {
			String newFID;
			newFID = vea.getNewFID();

			DefaultFeature df = new DefaultFeature(feature, values, newFID);
			int index = vea.addRow(df, "_newET", EditionEvent.GRAPHIC);
			//clearSelection();
			ArrayList selectedRow = vle.getSelectedRow();
			ViewPort vp = vle.getLayer().getMapContext().getViewPort();
			BufferedImage selectionImage = new BufferedImage(vp
					.getImageWidth(), vp.getImageHeight(),
					BufferedImage.TYPE_INT_ARGB);
			Graphics2D gs = selectionImage.createGraphics();
			int inversedIndex=vea.getInversedIndex(index);
			selectedRow.add(new DefaultRowEdited(df,
					IRowEdited.STATUS_ADDED, inversedIndex ));
			vea.getSelection().set(inversedIndex);
			IGeometry geom = df.getGeometry();
			geom.cloneGeometry().draw(gs, vp, DefaultCADTool.selectionSymbol);
			vle.drawHandlers(geom.cloneGeometry(), gs, vp);
			vea.setSelectionImage(selectionImage);


			SelectableDataSource recordset = vectLayer.getRecordset();
			recordset.clearSelection();

		} catch (IOException e) {
			//logger.debug(e);
			NotificationManager.addError(e);
		}catch (DriverIOException e) {
			NotificationManager.addError(e.getMessage(), e);
		} catch (DriverLoadException e) {
			NotificationManager.addError(e.getMessage(), e);
		} catch (com.iver.cit.gvsig.fmap.DriverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//		te.stopEditing(vectLayer, false);

	}


	/**
	 *  File must have lines with:
	 *  			TARGET_FIELDNAME = SOURCE_FIELDNAME
	 * 
	 *  It returns a Map<target, source> fields
	 * 
	 * @param filepath
	 * @return
	 */
	private HashMap getMatchFieldMap(String filepath){

		File file = new File(filepath);
		if (!file.exists()){
			return null;
		}

		HashMap<String, String> matchFields = new HashMap<String, String>();

		try {
			String line;
			BufferedReader fileReader = new BufferedReader(new FileReader(file));
			while ((line = fileReader.readLine())!=null) {
				String tokens[] = line.split("=");
				if (tokens.length == 2) {
					String k = tokens[0].trim().toUpperCase();
					String v = tokens[1].trim().toUpperCase();
					if ((k.length() > 0) && (v.length() > 0)) {
						matchFields.put(k, v);
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return matchFields;
	}

	public void copyData(String targetLayerName, String sourceLayerName, String match_filepath, boolean onlySelected) {

		ReadableVectorial sourceFeats = null;
		boolean error = false;
		boolean isEdited = false;

		int copyCount = 0;

		es.udc.cartolab.gvsig.navtable.ToggleEditing te = new es.udc.cartolab.gvsig.navtable.ToggleEditing();
		FLyrVect sourceLayer = (FLyrVect) layers.getLayer(sourceLayerName);
		FLyrVect targetLayer = (FLyrVect) layers.getLayer(targetLayerName);

		try {

			// Map that match target index fields with source index fields
			HashMap<Integer, Integer> tgtSrcIdxMap = new HashMap<Integer, Integer>();
			HashMap<String, String> matchMap = getMatchFieldMap(match_filepath);

			if (matchMap == null) {
				error = true;
				return;
			}

			SelectableDataSource targetRecordset = targetLayer.getRecordset();
			SelectableDataSource sourceRecordset = sourceLayer.getRecordset();

			for (String tgt_field:matchMap.keySet()) {
				tgt_field = tgt_field.toUpperCase();
				String src_field = matchMap.get(tgt_field).toUpperCase();
				int src_idx = sourceRecordset.getFieldIndexByName(src_field);
				int tgt_idx = targetRecordset.getFieldIndexByName(tgt_field);

				if (src_idx == -1){
					//TODO Translate!!
					System.out.println("El campo " + src_field + " no existe en la capa SOURCE ["+ "]");
					continue;
				}

				if (tgt_idx == -1){
					//TODO Translate!!
					System.out.println("El campo " + tgt_field + " no existe en la capa TARGET ["+ "]");
					continue;
				}
				tgtSrcIdxMap.put(tgt_idx, src_idx);

			}

			String[] attrNames = sourceRecordset.getFieldNames();
			int fieldsNumber = sourceRecordset.getFieldCount();

			te.startEditing(targetLayer);
			isEdited = true;

			sourceFeats = sourceLayer.getSource();
			sourceFeats.start();

			FBitSet bitset= sourceRecordset.getSelection();

			int number2copy = 0;
			if (onlySelected){
				number2copy =bitset.cardinality();
			} else {
				number2copy = (int) sourceRecordset.getRowCount();
			}

			int val = JOptionPane.showOptionDialog(this,
					PluginServices.getText(this, "Se van a copiar " +number2copy+ " entidades de la capa \"" + sourceLayerName
							+ "\"\n a la capa \"" + targetLayerName +"\".\n¿Desea continuar?"),
							"Copy Features Confirmation",
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE,
							null,
							new String[] {"Ok", "Cancelar"},
			"Ok");

			if (val != 0){
				return;
			}

			for (int i = 0; i < sourceRecordset.getRowCount(); i++){

				if (onlySelected && !bitset.get(i)){
					continue;
				}
				IGeometry gvGeom = sourceFeats.getShape(i);
				gvGeom.reProject(sourceLayer.getProjection().getCT(sourceLayer.getMapContext().getProjection()));

				Value[] values = new Value[fieldsNumber];
				for (int j = 0;  j < values.length; j++) {
					values[j] = ValueFactory.createNullValue();
				}

				for (int tgtIdx:tgtSrcIdxMap.keySet()){

					int srcIdx = tgtSrcIdxMap.get(tgtIdx);
					Value srcValue = sourceRecordset.getFieldValue(i, srcIdx);
					int tgtType = targetRecordset.getFieldType(tgtIdx);

					switch (tgtType) {
					case java.sql.Types.VARCHAR:
						values[tgtIdx] = ValueFactory.createValue((String) srcValue.toString());
						break;
					case java.sql.Types.INTEGER:
						if (srcValue instanceof StringValue){
							String aux = srcValue.toString().replace("\"", "");
							try {
								int auxInt = Integer.parseInt(aux);
								values[tgtIdx] = ValueFactory.createValue(auxInt);
							} catch (NumberFormatException e){
								//TODO
							}
						}
						if (srcValue instanceof IntValue){
							IntValue v = (IntValue) srcValue;
							values[tgtIdx] = ValueFactory.createValue((Integer) v.intValue());
						}
						if (srcValue instanceof DoubleValue){
							DoubleValue v = (DoubleValue) srcValue;
							values[tgtIdx] = ValueFactory.createValue((Integer) v.intValue());
						}
						if (srcValue instanceof BooleanValue){
							BooleanValue v = (BooleanValue) srcValue;
							if (v.getValue() == false) {
								values[tgtIdx] = ValueFactory.createValue(0);
							} else {
								values[tgtIdx] = ValueFactory.createValue(1);
							}
						}
					case java.sql.Types.DOUBLE:
						if (srcValue instanceof StringValue){
							String aux = srcValue.toString().replace("\"", "");
							try {
								double auxDouble = Double.parseDouble(aux);
								values[tgtIdx] = ValueFactory.createValue(auxDouble);
							} catch (NumberFormatException e){
								//TODO
							}
						}
						if (srcValue instanceof IntValue){
							IntValue v = (IntValue) srcValue;
							values[tgtIdx] = ValueFactory.createValue((Double) v.doubleValue());
						}
						if (srcValue instanceof DoubleValue){
							DoubleValue v = (DoubleValue) srcValue;
							values[tgtIdx] = ValueFactory.createValue((Double) v.doubleValue());
						}
						if (srcValue instanceof BooleanValue){
							BooleanValue v = (BooleanValue) srcValue;
							if (v.getValue() == false) {
								values[tgtIdx] = ValueFactory.createValue(0.0);
							} else {
								values[tgtIdx] = ValueFactory.createValue(1.0);
							}
						}
					case java.sql.Types.BOOLEAN:
						String aux = srcValue.toString();
						if ((aux.toUpperCase() == "FALSE") || (aux == "0") || (aux == "0.0") || (aux.toUpperCase() == "NO")){
							values[tgtIdx] = ValueFactory.createValue(false);
						} else {
							values[tgtIdx] = ValueFactory.createValue(true);
						}
					default:
						break;
					}

					if (srcValue instanceof StringValue){
						switch (tgtType) {
						case java.sql.Types.VARCHAR:
							values[tgtIdx] = ValueFactory.createValue((String) srcValue.toString());
							break;
						case java.sql.Types.INTEGER:
							String aux = srcValue.toString().replace("\"", "");
							try {
								int auxInt = Integer.parseInt(aux);
								values[tgtIdx] = ValueFactory.createValue(auxInt);
							} catch (NumberFormatException e){
								//TODO
							}
							break;
						default:
							values[tgtIdx] = ValueFactory.createValue((String) srcValue.toString());
							break;
						}
					}
					if (srcValue instanceof IntValue){
						IntValue v = (IntValue) srcValue;
						values[tgtIdx] = ValueFactory.createValue((Integer) v.intValue());
					}
					if (srcValue instanceof DoubleValue){
						DoubleValue v = (DoubleValue) srcValue;
						values[tgtIdx] = ValueFactory.createValue((Double) v.doubleValue());
					}
					if (srcValue instanceof BooleanValue){
						BooleanValue v = (BooleanValue) srcValue;
						values[tgtIdx] = ValueFactory.createValue((Boolean) v.getValue());
					}
				}
				createFeature(te, targetLayer, gvGeom, values);
				copyCount++;
			}
			sourceFeats.stop();

		} catch (com.hardcode.gdbms.engine.data.driver.DriverException e) {
			error = true;
			e.printStackTrace();
		} catch (Exception e) {
			error = true;
			e.printStackTrace();
		} finally {

			if (isEdited) {
				te.stopEditing(targetLayer, false);
				PluginServices.getMDIManager().closeWindow(this);
			}

			if (error){
				JOptionPane.showMessageDialog(this,
						PluginServices.getText(this, "ERROR: Se han copiado " +copyCount+ " entidades."),
						"Error",
						JOptionPane.ERROR_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(this,
						PluginServices.getText(this, "Se han copiado "+copyCount+ " entidades."),
						"Information",
						JOptionPane.INFORMATION_MESSAGE);
			}

		}

	}

	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == okButton) {

			String sourceLayerName =  sourceLayerCB.getSelectedItem().toString();
			String targetLayerName =  targetLayerCB.getSelectedItem().toString();

			if (sourceLayerName.equalsIgnoreCase(targetLayerName)){
				JOptionPane.showMessageDialog(this,
						PluginServices.getText(this, "sameLayerError"),
						"Error",
						JOptionPane.ERROR_MESSAGE);
			} else {
				copyData(targetLayerName, sourceLayerName, matchFileTF.getText(), onlySelectedChB.isSelected());
			}
		}

		if (e.getSource() == cancelButton) {
			PluginServices.getMDIManager().closeWindow(this);
		}

		if (e.getSource() == matchFileButton){

			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int returnVal = chooser.showOpenDialog(this);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				matchFileTF.setText(chooser.getSelectedFile().getAbsolutePath());
			}
		}
	}

}
