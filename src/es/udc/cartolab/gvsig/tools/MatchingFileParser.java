package es.udc.cartolab.gvsig.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

import es.udc.cartolab.gvsig.copyfeature.fieldfillutils.IFieldFillUtils;

public class MatchingFileParser {

	private HashMap<String, String> matchFields;
	private HashMap<String, IFieldFillUtils> calculatedFields;

	public MatchingFileParser(String filepath) throws ParseException {

		File file = new File(filepath);
		if (!file.exists()){
			throw new IllegalArgumentException(filepath + PluginServices.getText(this, "file_not_exists"));
		}

		matchFields = new HashMap<String, String>();
		calculatedFields = new HashMap<String, IFieldFillUtils>();

		int lineNumber = 1;

		try {
			String line;
			BufferedReader fileReader = new BufferedReader(new FileReader(file));

			while ((line = fileReader.readLine())!=null) {

				if (0 == line.trim().length()) {
					continue;
				}

		if (line.startsWith("#")) {
		    continue;
		}

				String tokens[] = line.split("[:=]");

				String k = tokens[0].trim().toUpperCase();
				String v = tokens[1].trim().toUpperCase();

				if ((2 != tokens.length) || (0 == k.length()) || (0 == v.length())){
					throw new ParseException("Bad Syntax", lineNumber);
				}

				if (line.contains("=")) {
					matchFields.put(k, v);
				} else {
					try {
						String args[] = v.split("[()]");
						if (args.length > 2) {
							throw new ParseException("Bad Syntax", lineNumber);
						}

						String className = "es.udc.cartolab.gvsig.copyfeature.fieldfillutils." + args[0];
						Class c = Class.forName(className);
						IFieldFillUtils util = (IFieldFillUtils) c.newInstance();
						if (args.length == 2) {
							util.setArguments(args[1]);
						}
						calculatedFields.put(k,util);


					} catch (Exception e) {
						e.printStackTrace();
						throw new ParseException("Bad Syntax", lineNumber);
					}
				}

				lineNumber++;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public HashMap<Integer, IFieldFillUtils> getCalculatedFieldsMap(SelectableDataSource targetRecordset)
	throws ReadDriverException {
		HashMap<Integer, IFieldFillUtils> calculatedFieldsMap = new HashMap<Integer, IFieldFillUtils>();

		for (String targetField:calculatedFields.keySet()) {
			int tgtIdx = targetRecordset.getFieldIndexByName(targetField);
			if (-1 == tgtIdx) {
				//TODO Translate!!
				System.out.println("ERROR -------------> El campo " + targetField + " no existe en la capa destino");
				continue;
			}
			calculatedFieldsMap.put(tgtIdx, calculatedFields.get(targetField));
		}
		return calculatedFieldsMap;
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