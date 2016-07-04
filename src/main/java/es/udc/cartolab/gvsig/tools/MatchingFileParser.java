package es.udc.cartolab.gvsig.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.gvsig.fmap.dal.feature.FeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.udc.cartolab.gvsig.copyfeature.fieldfillutils.IFieldFillUtils;
import es.udc.cartolab.gvsig.tools.exceptions.ParseException;

public class MatchingFileParser {
	private static final Logger logger = LoggerFactory
			.getLogger(MatchingFileParser.class);

	private Map<String, String> matchFields;
	private Map<String, IFieldFillUtils> calculatedFields;

	public MatchingFileParser(File file) throws ParseException {
		matchFields = new HashMap<String, String>();
		calculatedFields = new HashMap<String, IFieldFillUtils>();
		parseFile(file);
	}

	private void parseFile(File file) throws ParseException {
		int lineNumber = 1;
		BufferedReader fileReader = null;
		try {
			String line;
			fileReader = new BufferedReader(new FileReader(file));

			while ((line = fileReader.readLine()) != null) {

				if (0 == line.trim().length()) {
					continue;
				}

				if (line.startsWith("#")) {
					continue;
				}

				String tokens[] = line.split("[:=]");

				String k = tokens[0].trim().toUpperCase();
				String v = tokens[1].trim().toUpperCase();

				if ((2 != tokens.length) || (0 == k.length())
						|| (0 == v.length())) {
					throw new ParseException(String.format(
							"Error en la línea %d del fichero de entrada",
							lineNumber), "", -1);
				}

				if (line.contains("=")) {
					matchFields.put(k, v);
				} else {
					try {
						String args[] = v.split("[()]");
						if (args.length > 2) {
							throw new ParseException(
									String.format(
											"Error en la línea %d del fichero de entrada",
											lineNumber), "", -1);
						}

						String className = "es.udc.cartolab.gvsig.copyfeature.fieldfillutils."
								+ args[0];
						Class c = Class.forName(className);
						IFieldFillUtils util = (IFieldFillUtils) c
								.newInstance();
						if (args.length == 2) {
							util.setArguments(args[1]);
						}
						calculatedFields.put(k, util);

					} catch (Exception e) {
						throw new ParseException(String.format(
								"Error en la línea %d del fichero de entrada",
								lineNumber), "", -1);
					}
				}

				lineNumber++;
			}

		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (fileReader != null) {
				try {
					fileReader.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	public Map<Integer, IFieldFillUtils> getCalculatedFields(FeatureType target)
			throws ParseException {
		Map<Integer, IFieldFillUtils> calculatedFieldsMap = new HashMap<Integer, IFieldFillUtils>();

		for (String targetField : calculatedFields.keySet()) {
			int tgtIdx = target.getIndex(targetField);
			if (-1 == tgtIdx) {
				throw new ParseException("El campo " + targetField
						+ " no existe en la capa destino", "", -1);
			}
			calculatedFieldsMap.put(tgtIdx, calculatedFields.get(targetField));
		}
		return calculatedFieldsMap;
	}

	public Map<Integer, Integer> getMatching(FeatureType source,
			FeatureType target) throws ParseException {

		Map<Integer, Integer> tgtSrcIdxMap = new HashMap<Integer, Integer>();

		if (matchFields == null) {
			return null;
		}

		for (String tgt_field : matchFields.keySet()) {
			String src_field = matchFields.get(tgt_field);
			int src_idx = source.getIndex(src_field);
			int tgt_idx = target.getIndex(tgt_field);

			if (src_idx == -1) {
				throw new ParseException("El campo " + src_field
						+ " no existe en la capa SOURCE", "", -1);
			}

			if (tgt_idx == -1) {
				throw new ParseException("El campo " + tgt_field
						+ " no existe en la capa TARGET", "", -1);
			}
			tgtSrcIdxMap.put(tgt_idx, src_idx);

		}
		return tgtSrcIdxMap;
	}
}