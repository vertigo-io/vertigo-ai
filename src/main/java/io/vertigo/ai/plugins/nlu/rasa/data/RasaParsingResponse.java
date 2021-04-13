package io.vertigo.ai.plugins.nlu.rasa.data;

import java.util.List;

public final class RasaParsingResponse {

	public String text;
	public List<?> entities;
	public RasaIntentWithConfidence intent;
	public List<RasaIntentWithConfidence> intent_ranking;

	public final class RasaIntentWithConfidence {
		public String id;
		public String name;
		public Double confidence;

	}

}
