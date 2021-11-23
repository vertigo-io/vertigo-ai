package io.vertigo.ai.plugins.nlu.rasa.data;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.ScalarStyle;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import io.vertigo.ai.plugins.nlu.rasa.data.RasaTrainingData.RasaIntentNlu;

public class RasaNluTrainDataRepresenter extends Representer {

	public RasaNluTrainDataRepresenter() {
		super();
		//---
		addClassTag(RasaTrainingData.class, Tag.MAP);
		//---
		// keep natural order of properties
		final PropertyUtils propUtil = new PropertyUtils() {
			@Override
			protected Set<Property> createPropertySet(final Class<? extends Object> type, final BeanAccess bAccess) {
				return getPropertiesMap(type, bAccess).values().stream()
						.filter(prop -> prop.isReadable() && (isAllowReadOnlyProperties() || prop.isWritable()))
						.collect(Collectors.toCollection(LinkedHashSet::new));
			}
		};
		setPropertyUtils(propUtil);
	}

	@Override
	protected NodeTuple representJavaBeanProperty(final Object javaBean, final Property property, final Object propertyValue, final Tag customTag) {

		if (javaBean instanceof RasaIntentNlu) {
			if (property.getName().equals("examples")) {
				final RasaIntentNlu rasaIntentNlu = (RasaIntentNlu) javaBean;
				final NodeTuple standard = super.representJavaBeanProperty(javaBean, property,
						propertyValue, customTag);
				final Node n = standard.getValueNode();

				return new NodeTuple(standard.getKeyNode(), new ScalarNode(Tag.STR,
						createTrainingSetence(rasaIntentNlu.examples), n.getStartMark(), n.getEndMark(), ScalarStyle.LITERAL));
			}
		}
		if (javaBean instanceof RasaConfig.Pipeline) {
			if (propertyValue == null || (propertyValue instanceof Integer && (Integer) propertyValue == 0)) {
				return null;
			}
		}
		return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
	}

	private static String createTrainingSetence(final List<String> value) {
		if (value.isEmpty()) {
			return "";
		}
		// output yaml format for Rasa
		return "- " + String.join("\n- ", value) + "\n";
	}

	@Override
	public FlowStyle getDefaultFlowStyle() {
		return FlowStyle.BLOCK;
	}

}
