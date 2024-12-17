package io.vertigo.ai.plugins.nlu.rasa.data;

import java.util.ArrayList;
import java.util.List;

public final class RasaConfig {

	public String language;
	public List<Pipeline> pipeline = new ArrayList<>();

	public static final class Pipeline {

		public String name;
		public String analyzer;
		public int min_ngram;
		public int max_ngram;
		public int epochs;

	}

}
