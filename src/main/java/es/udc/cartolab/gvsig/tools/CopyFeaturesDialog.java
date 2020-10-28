package es.udc.cartolab.gvsig.tools;

import static es.icarto.gvsig.commons.i18n.I18n._;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.gvsig.andami.ui.mdiManager.IWindow;
import org.gvsig.andami.ui.mdiManager.MDIManagerFactory;
import org.gvsig.app.project.documents.view.gui.IView;
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.mapcontext.layers.FLayers;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.fmap.mapcontrol.MapControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.icarto.gvsig.commons.gui.AbstractIWindow;
import es.icarto.gvsig.commons.gui.ChooseLayerPanel;
import es.icarto.gvsig.commons.gui.ChooseLayerPanel.Orientation;
import es.icarto.gvsig.commons.gui.FileChooser;
import es.icarto.gvsig.commons.gui.OkCancelPanel;
import es.icarto.gvsig.commons.gui.WidgetFactory;
import es.udc.cartolab.gvsig.tools.exceptions.ParseException;
import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class CopyFeaturesDialog extends AbstractIWindow implements IWindow, ActionListener {

	private static final Logger logger = LoggerFactory.getLogger(CopyFeaturesDialog.class);
	FLayers layers = null;

	ChooseLayerPanel<FLyrVect> sourceChooser;
	ChooseLayerPanel<FLyrVect> targetChooser;

	private JCheckBox onlySelectedChB = null;

	private final OkCancelPanel okPanel;
	private final String defaultPath;
	private FileChooser fileChooser;

	public CopyFeaturesDialog(IView view, String defaultPath) {
		super();
		this.defaultPath = defaultPath;
		setWindowTitle("copy_features");
		this.okPanel = WidgetFactory.okCancelPanel(this, this, this);

		try {
			initialize(view);
		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void initialize(IView view) {
		final MapControl map = view.getMapControl();
		this.layers = map.getMapContext().getLayers();

		initSourceCombo(view);
		initTargetCombo(view);
		initSelectMatchingFile();
		initOptionPanel();
	}

	private void initSourceCombo(IView view) {
		this.sourceChooser = new ChooseLayerPanel<>(this, "SourceLayer", Orientation.HORIZONTAL, FLyrVect.class);
		this.sourceChooser.populateFrom(view, null);
		this.sourceChooser.preselectFirstActive();
	}

	private void initTargetCombo(IView view) {
		this.targetChooser = new ChooseLayerPanel<>(this, "TargetLayer", Orientation.HORIZONTAL, FLyrVect.class);
		this.targetChooser.populateFrom(view, null);
		this.targetChooser.addEmptyFirst(true);
	}

	private void initSelectMatchingFile() {
		final JPanel wrapPanel = new JPanel();
		this.fileChooser = new FileChooser(wrapPanel, "MatchFile", this.defaultPath);
		final Border border = WidgetFactory.borderTitled("Matching");
		wrapPanel.setBorder(border);
		this.add(wrapPanel, "span 3, grow, wrap");
	}

	private void initOptionPanel() {
		final JPanel featPanel = new JPanel(new MigLayout("left"));
		final Border border = WidgetFactory.borderTitled("Features");
		featPanel.setBorder(border);

		this.onlySelectedChB = new JCheckBox(_("OnlySelected"));
		featPanel.add(this.onlySelectedChB);

		super.add(featPanel, "span 3, grow, wrap");
	}

	private void showErrorMsg(String msg) {
		showErrorMsg(msg, (Object[]) null);
	}

	private void showErrorMsg(String msg, Object... args) {
		final Object translateMsg = _(msg, args);
		JOptionPane.showMessageDialog(this, translateMsg, "", JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(OkCancelPanel.OK_ACTION_COMMAND)) {
			if (sameLayer()) {
				showErrorMsg("same_layer_error");
				return;
			}

			final MatchingFileParser parser = validMatchingFile();
			if (parser == null) {
				return;
			}

			final long featuresToCopy = getNumberOfFeaturesToCopy();
			if (featuresToCopy < 1) {
				showErrorMsg("no_entities_to_copy");
				return;
			}
			if (!userConfirmation(this.sourceChooser.getSelected().getName(),
					this.targetChooser.getSelected().getName(), featuresToCopy)) {
				return;
			}
			copyFeatures(parser);
		}

		if (e.getActionCommand().equals(OkCancelPanel.CANCEL_ACTION_COMMAND)) {
			MDIManagerFactory.getManager().closeWindow(this);
		}
	}

	private long getNumberOfFeaturesToCopy() {
		final FLyrVect sourceLayer = this.sourceChooser.getSelected();

		long numberOfEntitiesToBeCopied = 0;

		try {
			if (this.onlySelectedChB.isSelected()) {
				numberOfEntitiesToBeCopied = sourceLayer.getFeatureStore().getFeatureSelection().getSize();
			} else {
				numberOfEntitiesToBeCopied = sourceLayer.getFeatureStore().getFeatureSet().getSize();
			}
		} catch (final DataException e) {
			logger.error(e.getMessage(), e);
		}
		return numberOfEntitiesToBeCopied;
	}

	private boolean sameLayer() {
		final FLyrVect source = this.sourceChooser.getSelected();
		final FLyrVect target = this.targetChooser.getSelected();
		return source == null || target == null || source == target;
	}

	private MatchingFileParser validMatchingFile() {
		if (!this.fileChooser.isValidAndExist()) {
			showErrorMsg("matching_file_not_exists");
			return null;
		}
		final File file = this.fileChooser.getFile();
		CopyFeaturesExtension.setDefaultPath(file.getParent());
		MatchingFileParser parser = null;
		try {
			parser = new MatchingFileParser(file);
		} catch (final ParseException e) {
			showErrorMsg(e.getMessage(), e.getArgs());
			logger.error(e.getMessage(), e);
		}

		return parser;
	}

	private boolean userConfirmation(String source, String target, long numberOfEntitiesToBeCopied) {

		final String msg = _("confirmation_message", numberOfEntitiesToBeCopied, source, target);
		final int val = JOptionPane.showOptionDialog(this, msg, "", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, new String[] { "Ok", "Cancelar" }, "Ok");

		return val == 0;
	}

	private void copyFeatures(MatchingFileParser parser) {
		final FLyrVect sourceLayer = this.sourceChooser.getSelected();
		final FLyrVect targetLayer = this.targetChooser.getSelected();

		String msg = "";
		int messageType = JOptionPane.INFORMATION_MESSAGE;
		try {

			MDIManagerFactory.getManager().setWaitCursor();
			final Logic logic = new Logic(parser);
			msg = logic.copyData(sourceLayer, targetLayer, this.onlySelectedChB.isSelected());
		} catch (final ParseException e) {
			msg = _(e.getMessage(), e.getArgs());
			messageType = JOptionPane.ERROR_MESSAGE;
		} catch (final Exception ex) {
			logger.error(ex.getMessage(), ex);
			msg = ex.getMessage();
			messageType = JOptionPane.ERROR_MESSAGE;
		} finally {
			MDIManagerFactory.getManager().restoreCursor();
			JOptionPane.showMessageDialog(this, msg, "", messageType);
			MDIManagerFactory.getManager().closeWindow(this);
		}
	}

	@Override
	protected JButton getDefaultButton() {
		return this.okPanel.getOkButton();
	}

	@Override
	protected Component getDefaultFocusComponent() {
		return this.sourceChooser.getDefaultFocusComponent();
	}

}
