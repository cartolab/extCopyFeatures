package es.udc.cartolab.gvsig.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

public class MatchingFileParser {

	private HashMap<String, String> matchFields;

	public MatchingFileParser(String filepath) {

		File file = new File(filepath);
		if (!file.exists()){
			throw new IllegalArgumentException(filepath + PluginServices.getText(this, "file_not_exists"));
		}

		matchFields = new HashMap<String, String>();
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
			e.printStackTrace();
		}
	}


	public  HashMap<Integer, Integer> getMatchingMap(SelectableDataSource sourceRecordset, SelectableDataSource targetRecordset) throws ReadDriverException {

		// Map that match target index fields with source index fields
		HashMap<Integer, Integer> tgtSrcIdxMap = new HashMap<Integer, Integer>();

		if (matchFields == null) {
			return null;
		}

		for (String tgt_field:matchFields.keySet()) {
			tgt_field = tgt_field.toUpperCase();
			String src_field = matchFields.get(tgt_field).toUpperCase();
			int src_idx = sourceRecordset.getFieldIndexByName(src_field);
			int tgt_idx = targetRecordset.getFieldIndexByName(tgt_field);

			if (src_idx == -1){
				//TODO Translate!!
				System.out.println("ERROR -------------> El campo " + src_field + " no existe en la capa SOURCE");
				continue;
			}

			if (tgt_idx == -1){
				//TODO Translate!!
				System.out.println("ERROR -------------> El campo " + tgt_field + " no existe en la capa TARGET");
				continue;
			}
			tgtSrcIdxMap.put(tgt_idx, src_idx);

		}
		return tgtSrcIdxMap;
	}
}