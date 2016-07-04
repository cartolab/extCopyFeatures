package es.udc.cartolab.gvsig.tools;

import org.gvsig.andami.IconThemeHelper;
import org.gvsig.andami.Launcher;
import org.gvsig.andami.PluginServices;
import org.gvsig.andami.plugins.Extension;
import org.gvsig.andami.ui.mdiManager.IWindow;
import org.gvsig.app.project.documents.view.gui.IView;

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

	private IView view;

	@Override
	public void execute(String actionCommand) {
		CopyFeaturesDialog dialog = new CopyFeaturesDialog(view, defaultPath);
		PluginServices.getMDIManager().addWindow(dialog);
	}

	@Override
	public void initialize() {
		String id = this.getClass().getName();
		IconThemeHelper.registerIcon("action", id, this);
		defaultPath = Launcher.getAppHomeDir();

	}

	@Override
	public boolean isEnabled() {
		IWindow iWindow = PluginServices.getMDIManager().getActiveWindow();
		if (iWindow instanceof IView) {
			view = (IView) iWindow;
			return true;
		} else {
			view = null;
			return false;
		}
	}

	@Override
	public boolean isVisible() {
		return true;
	}

}