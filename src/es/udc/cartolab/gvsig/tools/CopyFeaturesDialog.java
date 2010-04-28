package es.udc.cartolab.gvsig.tools;

import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.hardcode.driverManager.DriverLoadException;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.CADExtension;
import com.iver.cit.gvsig.fmap.DriverException;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.drivers.DriverIOException;
import com.iver.cit.gvsig.fmap.edition.DefaultRowEdited;
import com.iver.cit.gvsig.fmap.edition.EditionEvent;
import com.iver.cit.gvsig.fmap.edition.IRowEdited;
import com.iver.cit.gvsig.fmap.edition.VectorialEditableAdapter;
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

	String [][] codeResultSet;

	String [] fields = new String[3];
	String [] pkvalues = new String[1];

	private String message = null;

	private WindowInfo viewInfo = null;
	private JButton cancelButton = null;
	private JButton okButton = null;
	private JPanel panelButtons = null;
	private JComboBox inputLayerCB = null;
	private JComboBox targetLayerCB = null;

	public WindowInfo getWindowInfo() {
		if (viewInfo == null) {
			viewInfo = new WindowInfo(WindowInfo.MODALDIALOG | WindowInfo.RESIZABLE | WindowInfo.PALETTE);
			viewInfo.setTitle(PluginServices.getText(this, "copyinputdata"));
			viewInfo.setWidth(500);
			viewInfo.setHeight(100);
		}
		return viewInfo;
	}

	public CopyFeaturesDialog() {
		super();
		try {
			fields[0] = "capa";
			fields[1] = "codigo";
			fields[2] = "altura";

			pkvalues[0] = "capa";
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

	private void initialize() throws Exception {

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

				GridBagLayout layout = new GridBagLayout();
				GridBagConstraints c = new GridBagConstraints();

				//ROW 1: input layers combobox
				c.weightx = 1.0;
				c.insets = new Insets(0,12,0,12);
				c.ipady = 5;
				//c.fill = GridBagConstraints.BOTH;
				c.anchor = GridBagConstraints.WEST;

				super.setLayout(layout);

				JLabel inputLayerLabel = new JLabel(PluginServices.getText(this, "inputLayer"));
				layout.setConstraints(inputLayerLabel, c);
				super.add(inputLayerLabel, c);

				c.fill = GridBagConstraints.BOTH;
				c.anchor = GridBagConstraints.WEST;

				inputLayerCB = new JComboBox();

				for (int i=layers.getLayersCount()-1; i >= 0; i--) {
					inputLayerCB.addItem(layers.getLayer(i).getName());
				}
				layout.setConstraints(inputLayerCB, c);
				super.add(inputLayerCB);

				//ROW 2: target layers combobox
				c.gridy = 3;
				super.setLayout(layout);

				JLabel targetLayerLabel = new JLabel(PluginServices.getText(this, "TargetLayer"));
				layout.setConstraints(targetLayerLabel, c);
				super.add(targetLayerLabel, c);

				c.fill = GridBagConstraints.BOTH;
				c.anchor = GridBagConstraints.WEST;

				targetLayerCB = new JComboBox();
				boolean notGpsLayerInTOC = true;
				for (int i=codeResultSet.length-1; i >= 0; i--) {
					targetLayerCB.addItem(layers.getLayer(i).getName());
				}

				if (targetLayerCB.getItemCount() > 0){
					targetLayerCB.setSelectedIndex(1);
				}

				layout.setConstraints(targetLayerCB, c);
				super.add(targetLayerCB);

				// TODO GET CSV list of the identifiers and put it on a CBox

				//TODO Only selected / all feaures


				//ROW 3: ok & cancel buttons
				c.gridy=5;
				c.gridwidth = 2;
				c.ipady = 8;
				c.insets = new Insets(12,0,0,0);
				c.fill = GridBagConstraints.CENTER;
				c.anchor = GridBagConstraints.CENTER;
				JPanel buttonPanel = getJPanelButtons();
				layout.setConstraints(buttonPanel, c);

				super.add(buttonPanel);
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
			try {
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

			} catch (IOException e) {
				//logger.debug(e);
				NotificationManager.addError(e);
				return;
			}

			SelectableDataSource recordset = vectLayer.getRecordset();
			recordset.clearSelection();

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

	private String my_format(double d){

		NumberFormat nf = NumberFormat.getInstance(Locale.GERMAN);
		nf.setMaximumFractionDigits(1);
		nf.setMinimumFractionDigits(1);

		String number = nf.format(d);
		number = number.replaceAll("\\.", "");
		number = number.replaceAll(",", ".");

		return number;

	}

	public void copyData(String targetLayerName, String inputLayerName) {

		es.udc.cartolab.gvsig.navtable.ToggleEditing te = new es.udc.cartolab.gvsig.navtable.ToggleEditing();
		ReadableVectorial feats = null;
		FLyrVect targetLayer = null;
		boolean error = false;

		try {

			FLyrVect inputLayer = (FLyrVect) layers.getLayer(inputLayerName);

			// TODO GET IDENTIFIERS OF THE CSVs
			SelectableDataSource inputLayerRecordset = inputLayer.getRecordset();
			int codigoIdx = inputLayerRecordset.getFieldIndexByName("Identifica");
			int longitudIdx = inputLayerRecordset.getFieldIndexByName("Longitud");
			int latitudIdx = inputLayerRecordset.getFieldIndexByName("Latitud");
			int altitudIdx = inputLayerRecordset.getFieldIndexByName("Altitud");
			String codeField = null;
			String zField = null;

			if ((codigoIdx == -1) || (longitudIdx == -1) ||
					(latitudIdx == -1) || (altitudIdx == -1)) {

				JOptionPane.showMessageDialog(this,
						PluginServices.getText(this, "noGPSError"),
						"Error",
						JOptionPane.ERROR_MESSAGE);

				return;
			}

			for(int i=0; i<codeResultSet.length; i++) {
				if (targetLayerName.compareTo(codeResultSet[i][0]) == 0) {
					codeField = codeResultSet[i][1];
					zField = codeResultSet[i][2];
					break;
				}
			}

			targetLayer = (FLyrVect) layers.getLayer(targetLayerName);
			SelectableDataSource targetRecordset = targetLayer.getRecordset();
			int fieldsNumber = targetRecordset.getFieldCount();

			te.startEditing(targetLayer);

			feats = inputLayer.getSource();
			feats.start();


			//TODO PUT ON TARGET ATTRIB
			for (int i=0; i<inputLayerRecordset.getRowCount(); i++){

				IGeometry gvGeom = feats.getShape(i);
				gvGeom.reProject(inputLayer.getProjection().getCT(inputLayer.getMapContext().getProjection()));

				Value[] values = new Value[fieldsNumber];

				int codeIdx = targetRecordset.getFieldIndexByName(codeField);
				int lonIdx = targetRecordset.getFieldIndexByName("x");
				int latIdx = targetRecordset.getFieldIndexByName("y");
				int zIdx = targetRecordset.getFieldIndexByName(zField);

				for (int j=0; j<values.length;j++) {
					values[j] = ValueFactory.createNullValue();
				}

				values[codeIdx] = ValueFactory.createValue(inputLayerRecordset.getFieldValue(i, codigoIdx).toString());
				values[lonIdx] = ValueFactory.createValue(my_format(gvGeom.toJTSGeometry().getCoordinate().x));
				values[latIdx] = ValueFactory.createValue(my_format(gvGeom.toJTSGeometry().getCoordinate().y));
				Double z = new Double(inputLayerRecordset.getFieldValue(i, altitudIdx).toString());
				values[zIdx] = ValueFactory.createValue(new Double(my_format(z)));
				String[] attrNames = targetRecordset.getFieldNames();

				createFeature(te, targetLayer, gvGeom, values);

				//				sqlconnector.saveSQLValues(targetLayerCB.getSelectedItem().toString(), attrNames, values);
			}

			feats.stop();

		} catch (DriverException e) {
			// TODO Auto-generated catch block
			error = true;
			e.printStackTrace();
		} catch (com.hardcode.gdbms.engine.data.driver.DriverException e) {
			// TODO Auto-generated catch block
			error = true;
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			error = true;
			e.printStackTrace();
		} finally {

			if (error){
				JOptionPane.showMessageDialog(this,
						PluginServices.getText(this, "ERROR: Algunos puntos del GPS puede que no se hayan copiado."),
						"Error",
						JOptionPane.ERROR_MESSAGE);
			}
			te.stopEditing(targetLayer, false);
			PluginServices.getMDIManager().closeWindow(this);

		}

	}

	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == okButton) {

			String inputLayerName =  inputLayerCB.getSelectedItem().toString();
			String targetLayerName =  targetLayerCB.getSelectedItem().toString();

			if (inputLayerName.equalsIgnoreCase(targetLayerName)){
				JOptionPane.showMessageDialog(this,
						PluginServices.getText(this, "sameLayerError"),
						"Error",
						JOptionPane.ERROR_MESSAGE);
			} else {
				copyData(targetLayerName, inputLayerName);
			}
		}

		if (e.getSource() == cancelButton) {
			PluginServices.getMDIManager().closeWindow(this);
		}
	}

}
