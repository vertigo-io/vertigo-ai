package io.vertigo.ai.plugins.nlu.rasa.data;

import java.util.List;

import io.vertigo.ai.plugins.nlu.rasa.data.RasaConfig.Pipeline;

public class RasaTrainingData {

	public String language;
	public List<Pipeline> pipeline;
	public List<RasaIntentNlu> nlu;

	public RasaTrainingData(
			final String language,
			final List<Pipeline> pipeline,
			final List<RasaIntentNlu> nlu) {
		this.nlu = nlu;
		this.language = language;
		this.pipeline = pipeline;
	}

	public static class RasaIntentNlu {

		public String intent;
		public List<String> examples;

		public RasaIntentNlu(final String intent, final List<String> examples) {
			this.intent = intent;
			this.examples = examples;
		}

	}
}
