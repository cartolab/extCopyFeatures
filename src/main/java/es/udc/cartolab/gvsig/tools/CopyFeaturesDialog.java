package es.udc.cartolab.gvsig.tools;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import net.miginfocom.swing.MigLayout;

import org.gvsig.andami.PluginServices;
import org.gvsig.andami.ui.mdiManager.IWindow;
import org.gvsig.app.project.documents.view.gui.IView;
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.mapcontext.layers.FLayers;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.fmap.mapcontrol.MapControl;
import org.gvsig.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.icarto.gvsig.commons.gui.AbstractIWindow;
import es.icarto.gvsig.commons.gui.ChooseLayerPanel;
import es.icarto.gvsig.commons.gui.ChooseLayerPanel.Orientation;
import es.icarto.gvsig.commons.gui.OkCancelPanel;
import es.icarto.gvsig.commons.gui.WidgetFactory;
import es.udc.cartolab.gvsig.tools.exceptions.ParseException;

@SuppressWarnings("serial")
public class CopyFeaturesDialog extends AbstractIWindow implements IWindow,
		ActionListener {

	private static final Logger logger = LoggerFactory
			.getLogger(CopyFeaturesDialog.class);
	FLayers layers = null;
	private JButton matchFileButton = null;

	ChooseLayerPanel<FLyrVect> sourceChooser;
	ChooseLayerPanel<FLyrVect> targetChooser;
	private JTextField matchFileTF = null;

	private JCheckBox onlySelectedChB = null;

	private final OkCancelPanel okPanel;
	private final String defaultPath;

	public CopyFeaturesDialog(IView view, String defaultPath) {
		super();
		this.defaultPath = defaultPath;
		setWindowTitle(PluginServices.getText(this, "_copy_features"));
		okPanel = WidgetFactory.okCancelPanel(this, this, this);

		try {
			initialize(view);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void initialize(IView view) {
		MapControl map = view.getMapControl();
		layers = map.getMapContext().getLayers();

		initSourceCombo(view);
		initTargetCombo(view);
		initSelectMatchingFile();
		initOptionPanel();
	}

	private void initSourceCombo(IView view) {
		sourceChooser = new ChooseLayerPanel<FLyrVect>(this, "SourceLayer",
				Orientation.HORIZONTAL, FLyrVect.class);
		sourceChooser.populateFrom(view, null);
		sourceChooser.preselectFirstActive();
		super.add(sourceChooser, "span 2, growx, wrap");
	}

	private void initTargetCombo(IView view) {
		targetChooser = new ChooseLayerPanel<FLyrVect>(this, "TargerLayer",
				Orientation.HORIZONTAL, FLyrVect.class);
		targetChooser.populateFrom(view, null);
		targetChooser.addEmptyFirst(true);
		super.add(targetChooser, "span 2, growx, wrap");
	}

	private void initSelectMatchingFile() {
		JPanel matchPanel = new JPanel(new MigLayout("center", "[][70%][]", ""));
		Border border = BorderFactory.createTitledBorder(PluginServices
				.getText(this, "Matching"));
		matchPanel.setBorder(border);

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
	}

	private void initOptionPanel() {
		JPanel featPanel = new JPanel(new MigLayout("left"));
		Border border = BorderFactory.createTitledBorder(PluginServices
				.getText(this, "Features"));
		featPanel.setBorder(border);

		onlySelectedChB = new JCheckBox(PluginServices.getText(this,
				"OnlySelected"));
		featPanel.add(onlySelectedChB);

		super.add(featPanel, "span 3, grow, wrap");
	}

	private void showErrorMsg(String msg) {
		Object translateMsg = Messages.getText(msg);
		JOptionPane.showMessageDialog(this, translateMsg, "",
				JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(OkCancelPanel.OK_ACTION_COMMAND)) {
			if (sameLayer()) {
				showErrorMsg("sameLayerError");
				return;
			}
			MatchingFileParser parser = validMatchingFile();
			if (parser == null) {
				return;
			}

			long featuresToCopy = getNumberOfFeaturesToCopy();
			if (featuresToCopy < 1) {
				showErrorMsg("No hay entidades para copiar en la capa origen");
				return;
			}
			if (!userConfirmation(sourceChooser.getSelected().getName(),
					targetChooser.getSelected().getName(), featuresToCopy)) {
				return;
			}
			copyFeatures(parser);
		}

		if (e.getActionCommand().equals(OkCancelPanel.CANCEL_ACTION_COMMAND)) {
			PluginServices.getMDIManager().closeWindow(this);
		}

		if (e.getSource() == matchFileButton) {
			JFileChooser chooser = new JFileChooser(defaultPath);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			int returnVal = chooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File selectedFile = chooser.getSelectedFile();
				matchFileTF.setText(selectedFile.getAbsolutePath());
				CopyFeaturesExtension.setDefaultPath(selectedFile.getParent());

			}

		}
	}

	private long getNumberOfFeaturesToCopy() {
		FLyrVect sourceLayer = sourceChooser.getSelected();

		long numberOfEntitiesToBeCopied = 0;

		try {
			if (onlySelectedChB.isSelected()) {
				numberOfEntitiesToBeCopied = sourceLayer.getFeatureStore()
						.getFeatureSelection().getSize();
			} else {
				numberOfEntitiesToBeCopied = sourceLayer.getFeatureStore()
						.getFeatureSet().getSize();
			}
		} catch (DataException e) {
			logger.error(e.getMessage(), e);
		}
		return numberOfEntitiesToBeCopied;
	}

	private boolean sameLayer() {
		FLyrVect source = sourceChooser.getSelected();
		FLyrVect target = targetChooser.getSelected();
		return (source == null) || (target == null) || (source == target);
	}

	private MatchingFileParser validMatchingFile() {
		String filePath = matchFileTF.getText();
		File file = new File(filePath);
		if (!file.exists()) {
			showErrorMsg("matching_file_not_exists");
			return null;
		}
		MatchingFileParser parser = null;
		try {
			parser = new MatchingFileParser(file);
		} catch (ParseException e) {
			showErrorMsg(e.getMessage());
			logger.error(e.getMessage(), e);
		}

		return parser;
	}

	private boolean userConfirmation(String source, String target,
			long numberOfEntitiesToBeCopied) {

		int val = JOptionPane.showOptionDialog(this, "Se van a copiar "
				+ numberOfEntitiesToBeCopied + " entidades de la capa \""
				+ source + "\"\n a la capa \"" + target
				+ "\".\n�Desea continuar?", "", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, new String[] { "Ok",
						"Cancelar" }, "Ok");

		return val == 0;
	}

	private void copyFeatures(MatchingFileParser parser) {
		FLyrVect sourceLayer = sourceChooser.getSelected();
		FLyrVect targetLayer = targetChooser.getSelected();

		String msg = "";
		int messageType = JOptionPane.INFORMATION_MESSAGE;
		try {

			PluginServices.getMDIManager().setWaitCursor();
			Logic logic = new Logic(parser);
			msg = logic.copyData(sourceLayer, targetLayer,
					onlySelectedChB.isSelected());

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			msg = ex.getMessage();
			messageType = JOptionPane.ERROR_MESSAGE;
		} finally {
			PluginServices.getMDIManager().restoreCursor();
			JOptionPane.showMessageDialog(this, msg, "", messageType);
			PluginServices.getMDIManager().closeWindow(this);
		}
	}

	@Override
	protected JButton getDefaultButton() {
		return okPanel.getOkButton();
	}

	@Override
	protected Component getDefaultFocusComponent() {
		return sourceChooser.getDefaultFocusComponent();
	}

}