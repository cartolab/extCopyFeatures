package es.udc.cartolab.gvsig.tools;
import javax.swing.JOptionPane;

import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;

public class CopyFeaturesExtension extends Extension{

	public void execute(String actionCommand) {

		CopyFeaturesDialog dialog = new CopyFeaturesDialog();

		/**
		 * dialog.getHondurasCopyGpsMessage() values:
		 * - null: if all was OK
		 * - not_view_to_work_on: there is no a view in the project manager
		 * - sqlite_error: error getting the connector or result set
		 * - void_view: there is view in the project manager but it's void
		 * - not_layers_in_TOC: there is no layers in TOC
		 * - not_GPS_layers_in_TOC: there is no GPS layers in TOC
		 */
		String message = dialog.getMessage();
		if (message != null) {
			JOptionPane.showMessageDialog(dialog,
					PluginServices.getText(dialog, message),
					"Error",
					JOptionPane.ERROR_MESSAGE);
		}
		else {
			PluginServices.getMDIManager().addWindow(dialog);
		}


	}

	public void initialize() {
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isVisible() {
		return true;
	}

}
