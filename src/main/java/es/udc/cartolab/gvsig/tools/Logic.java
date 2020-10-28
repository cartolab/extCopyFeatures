package es.udc.cartolab.gvsig.tools;

import static es.icarto.gvsig.commons.i18n.I18n._;

import java.util.Map;

import org.gvsig.fmap.dal.feature.EditableFeature;
import org.gvsig.fmap.dal.feature.Feature;
import org.gvsig.fmap.dal.feature.FeatureSelection;
import org.gvsig.fmap.dal.feature.FeatureSet;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.dal.feature.FeatureType;
import org.gvsig.fmap.geom.Geometry;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.tools.dispose.DisposableIterator;
import org.gvsig.tools.dispose.DisposeUtils;

import es.udc.cartolab.gvsig.copyfeature.fieldfillutils.IFieldFillUtils;
import es.udc.cartolab.gvsig.tools.exceptions.ParseException;

public class Logic {

	private final MatchingFileParser parser;

	public Logic(MatchingFileParser parser) throws ParseException {
		this.parser = parser;
	}

	public String copyData(FLyrVect sourceLayer, FLyrVect targetLayer, boolean onlySelected) throws Exception {
		FeatureStore targetStore = null;
		FeatureSet sourceSet = null;
		FeatureSelection sourceSelection = null;
		DisposableIterator sourceIt = null;

		int copyCount = 0;
		try {
			FeatureStore sourceStore = sourceLayer.getFeatureStore();
			targetStore = targetLayer.getFeatureStore();

			FeatureType sourceType = sourceStore.getDefaultFeatureType();
			FeatureType targetType = targetStore.getDefaultFeatureType();

			Map<Integer, Integer> tgtSrcIdx = parser.getMatching(sourceType, targetType);
			Map<Integer, IFieldFillUtils> calculatedFields = parser.getCalculatedFields(targetType);

			targetStore.edit();

			sourceSet = sourceStore.getFeatureSet();
			sourceIt = sourceSet.fastIterator();
			int sourceGeomIdx = sourceType.getDefaultGeometryAttributeIndex();
			int targetGeomIdx = targetType.getDefaultGeometryAttributeIndex();
			while (sourceIt.hasNext()) {
				Feature sourceFeat = (Feature) sourceIt.next();
				sourceSelection = sourceStore.getFeatureSelection();
				if (onlySelected && !sourceSelection.isSelected(sourceFeat)) {
					continue;
				}

				Geometry geom = sourceFeat.getGeometry(sourceGeomIdx);
				if (geom.getGeometryType() != targetLayer.getGeometryType()) {
					continue;
				}
				if (sourceLayer.getProjection() != sourceLayer.getMapContext().getProjection()) {
					geom.reProject(sourceLayer.getProjection().getCT(sourceLayer.getMapContext().getProjection()));
				}
				EditableFeature targetFeat = targetStore.createNewFeature();
				for (int tgtIdx : tgtSrcIdx.keySet()) {
					Object sourceValue = sourceFeat.get(tgtSrcIdx.get(tgtIdx));
					targetFeat.set(tgtIdx, sourceValue);
				}
				if (!calculatedFields.isEmpty()) {
					for (int tgtIdx : calculatedFields.keySet()) {
						IFieldFillUtils util = calculatedFields.get(tgtIdx);
						Object sourceValue = util.execute(sourceFeat);
						targetFeat.set(tgtIdx, sourceValue);
					}
				}
				targetFeat.setGeometry(targetGeomIdx, geom);
				targetStore.insert(targetFeat);
				copyCount++;
			}
			targetStore.finishEditing();
		} catch (Exception e) {
			if ((targetStore != null) && targetStore.isEditing()) {
				targetStore.cancelEditing();
			}
			throw e;
		} finally {
			DisposeUtils.dispose(sourceIt);
			DisposeUtils.dispose(sourceSelection);
			DisposeUtils.dispose(sourceSet);
		}
		return _("n_features_copied", copyCount);

	}
}
