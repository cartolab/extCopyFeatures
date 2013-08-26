package es.udc.cartolab.gvsig.tools;

import javax.swing.JOptionPane;

import com.iver.andami.Launcher;
import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;
import com.iver.cit.gvsig.project.documents.view.gui.View;

public class CopyFeaturesExtension extends Extension {

    public static final String COPY_FEATURES_ICON = "copy-features-icon";
    public static final String COPY_FEATURES_MENU = "_copy_features_menu";
    private static String defaultPath = null;

    public static void setDefaultPath(String defPath) {
	defaultPath = defPath;
    }

    public static String getDefaultPath() {
	return defaultPath;
    }

    @Override
    public void execute(String actionCommand) {

	CopyFeaturesDialog dialog = new CopyFeaturesDialog();

	/**
	 * dialog.getHondurasCopyGpsMessage() values: - null: if all was OK -
	 * not_view_to_work_on: there is no a view in the project manager -
	 * sqlite_error: error getting the connector or result set - void_view:
	 * there is view in the project manager but it's void -
	 * not_layers_in_TOC: there is no layers in TOC - not_GPS_layers_in_TOC:
	 * there is no GPS layers in TOC
	 */
	String message = dialog.getMessage();
	if (message != null) {
	    JOptionPane.showMessageDialog(dialog,
		    PluginServices.getText(dialog, message), "Error",
		    JOptionPane.ERROR_MESSAGE);
	} else {
	    PluginServices.getMDIManager().addWindow(dialog);
	}

    }

    @Override
    public void initialize() {
	PluginServices.getIconTheme()
		.registerDefault(
			COPY_FEATURES_ICON,
			this.getClass().getClassLoader()
				.getResource("images/copy.png"));
	defaultPath = Launcher.getAppHomeDir();

    }

    @Override
    public boolean isEnabled() {
	if (PluginServices.getMDIManager().getActiveWindow() instanceof View) {
	    return true;
	} else {
	    return false;
	}
    }

    @Override
    public boolean isVisible() {
	return true;
    }

}
