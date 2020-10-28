package es.udc.cartolab.gvsig.tools;

import org.gvsig.andami.IconThemeHelper;
import org.gvsig.andami.Launcher;
import org.gvsig.andami.PluginServices;
import org.gvsig.andami.plugins.Extension;
import org.gvsig.andami.ui.mdiManager.IWindow;
import org.gvsig.andami.ui.mdiManager.MDIManagerFactory;
import org.gvsig.app.project.documents.view.gui.IView;

public class CopyFeaturesExtension extends Extension {

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
		MDIManagerFactory.getManager().addWindow(dialog);
	}

	@Override
	public void initialize() {
		String id = this.getClass().getName();
		IconThemeHelper.registerIcon("action", id, this);
		defaultPath = Launcher.getAppHomeDir();

	}

	@Override
	public boolean isEnabled() {
		IWindow iWindow = MDIManagerFactory.getManager().getActiveWindow();
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
