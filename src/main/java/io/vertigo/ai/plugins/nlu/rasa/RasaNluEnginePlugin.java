package io.vertigo.ai.plugins.nlu.rasa;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.inspector.TagInspector;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import io.vertigo.ai.impl.nlu.NluEnginePlugin;
import io.vertigo.ai.impl.nlu.NluManagerImpl;
import io.vertigo.ai.nlu.NluIntent;
import io.vertigo.ai.nlu.NluResult;
import io.vertigo.ai.nlu.ScoredIntent;
import io.vertigo.ai.plugins.nlu.rasa.data.RasaConfig;
import io.vertigo.ai.plugins.nlu.rasa.data.RasaNluTrainDataRepresenter;
import io.vertigo.ai.plugins.nlu.rasa.data.RasaParsingResponse;
import io.vertigo.ai.plugins.nlu.rasa.data.RasaTrainingData;
import io.vertigo.ai.plugins.nlu.rasa.data.RasaTrainingData.RasaIntentNlu;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.VSystemException;
import io.vertigo.core.param.ParamValue;
import io.vertigo.core.resource.ResourceManager;
import io.vertigo.core.util.FileUtil;

public class RasaNluEnginePlugin implements NluEnginePlugin {

	private static final String RASA_MODEL = "/model";
	private static final String RASA_TRAIN = "/train";
	private static final String RASA_PARSE = "/parse";
	private static final Gson GSON = new Gson();

	private final String name;
	private final String rasaUrl;

	//time in seconds
	private final int rasaRequestTimeout;

	private final RasaConfig rasaConfig;

	private boolean ready;

	@Inject
	public RasaNluEnginePlugin(
			@ParamValue("rasaUrl") final String rasaUrl,
			@ParamValue("rasaRequestTimeout") final Optional<Integer> rasaRequestTimeout,
			@ParamValue("configFile") final Optional<String> configFileOpt,
			@ParamValue("pluginName") final Optional<String> pluginNameOpt,
			final ResourceManager resourceManager) {

		Assertion.check().isNotBlank(rasaUrl);

		this.rasaUrl = rasaUrl;
		this.rasaRequestTimeout = rasaRequestTimeout.orElse(30);
		name = pluginNameOpt.orElse(NluManagerImpl.DEFAULT_ENGINE_NAME);

		final var configFileName = configFileOpt.orElse("rasa-config.yaml"); // in classpath by default
		Assertion.check().isNotBlank(configFileName);
		LoaderOptions loaderoptions = new LoaderOptions();
		TagInspector taginspector =
				tag -> tag.getClassName().equals(RasaConfig.class.getName());
		loaderoptions.setTagInspector(taginspector);
		rasaConfig = new Yaml(new Constructor(RasaConfig.class, loaderoptions)).load(FileUtil.read(resourceManager.resolve(configFileName)));

		ready = false;
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void train(final Map<NluIntent, List<String>> trainingData) {
		//train a model
		final String filename = trainModel(trainingData);
		ready = false;

		//put model
		putModel(filename);

		ready = true;
	}

	private String trainModel(final Map<NluIntent, List<String>> trainingData) {
		final RasaTrainingData rasaTrainingData = new RasaTrainingData(
				rasaConfig.language,
				rasaConfig.pipeline,
				trainingData.entrySet().stream()
						.map(entry -> new RasaIntentNlu(entry.getKey().getCode(), entry.getValue()))
						.collect(Collectors.toList()));

		LoaderOptions loaderoptions = new LoaderOptions();
		TagInspector taginspector =
				tag -> tag.getClassName().equals(RasaTrainingData.class.getName());
		loaderoptions.setTagInspector(taginspector);
		//train
		final String trainingDataAsYaml = new Yaml(new Constructor(RasaTrainingData.class, loaderoptions), new RasaNluTrainDataRepresenter())
				.dump(rasaTrainingData);

		final HttpRequest request = HttpRequest.newBuilder(URI.create(rasaUrl + RASA_MODEL + RASA_TRAIN))
				.timeout(Duration.ofSeconds(rasaRequestTimeout))
				.header("Content-Type", "application/x-yaml")
				.POST(BodyPublishers.ofString(trainingDataAsYaml))
				.build();

		final HttpResponse<String> response = sendRequest(request, BodyHandlers.ofString(), 200);
		return response.headers().map().get("filename").get(0);
	}

	private void putModel(final String filename) {
		//Create object to send
		final JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("model_file", "models/" + filename);
		final String json = GSON.toJson(jsonObject);

		//send request
		final HttpRequest request = HttpRequest.newBuilder(URI.create(rasaUrl + RASA_MODEL))
				.timeout(Duration.ofSeconds(rasaRequestTimeout))
				.header("Content-Type", "application/json")
				.PUT(BodyPublishers.ofString(json))
				.build();

		sendRequest(request, BodyHandlers.ofString(), HttpServletResponse.SC_NO_CONTENT);
	}

	/** {@inheritDoc} */
	@Override
	public NluResult recognize(final String sentence) {
		// wait node is ready for recognition
		waitUntilReady();
		//---
		//prepare Json request
		final JsonObject messageToRecognize = new JsonObject();
		messageToRecognize.addProperty("text", sentence);

		// send request
		final HttpRequest request = HttpRequest.newBuilder(URI.create(rasaUrl + RASA_MODEL + RASA_PARSE))
				.header("Content-Type", "application/json")
				.POST(BodyPublishers.ofString(GSON.toJson(messageToRecognize)))
				.build();
		final HttpResponse<String> response = sendRequest(request, BodyHandlers.ofString(), 200);
		final RasaParsingResponse rasaParsingResponse = GSON.fromJson(response.body(), RasaParsingResponse.class);

		// put response in the standard format
		final List<ScoredIntent> intentClassificationList = rasaParsingResponse.intent_ranking.stream()
				.map(rasaIntent -> new ScoredIntent(NluIntent.of(rasaIntent.name), rasaIntent.confidence))
				.collect(Collectors.toList());
		return new NluResult(rasaParsingResponse.intent.name, intentClassificationList);
	}

	private void waitUntilReady() {
		int retry = 0;
		while (!ready && retry < 5) {
			try {
				Thread.sleep(500);
			} catch (final InterruptedException e) {
				//si interrupt on relance
				Thread.currentThread().interrupt();
			}
			retry++;
		}

		if (!ready) {
			throw new IllegalStateException("NLU engine '" + getName() + "' is not ready to recognize sentences.");
		}
	}

	private static <T extends Object> HttpResponse<T> sendRequest(final HttpRequest request, final BodyHandler<T> handler, final int successStatutCode) {
		final HttpClient client = HttpClient.newBuilder().version(Version.HTTP_1_1).build();
		try {
			final HttpResponse<T> response = client.send(request, handler);
			if (response.statusCode() != successStatutCode) {
				throw new VSystemException("Error while sending request to '{0}'. Expected HTTP code '{1}' but was '{2}'.", request.uri().toString(), successStatutCode, response.statusCode());
			}
			return response;
		} catch (final IOException | InterruptedException e) {
			throw new VSystemException(e, "Error while sending request to '{0}'", request.uri().toString());
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isReady() {
		return ready;
	}

	@Override
	public boolean isAlive() {
		final HttpRequest request = HttpRequest.newBuilder(URI.create(rasaUrl))
				.header("Content-Type", "application/json")
				.GET()
				.build();
		try {
			final HttpResponse<String> response = sendRequest(request, BodyHandlers.ofString(), 200);
			return response.statusCode() == 200;
		} catch (final VSystemException vSystemException) {
			return false;
		}
	}
}
