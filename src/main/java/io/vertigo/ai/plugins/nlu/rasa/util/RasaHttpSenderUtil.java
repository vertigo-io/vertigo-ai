package io.vertigo.ai.plugins.nlu.rasa.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.vertigo.ai.plugins.nlu.rasa.mda.MessageToRecognize;
import io.vertigo.ai.plugins.nlu.rasa.mda.RasaParsingResponse;
import io.vertigo.core.lang.VSystemException;

public final class RasaHttpSenderUtil {
	private static final String RASA_MODEL = "/model";
	private static final String RASA_TRAIN = "/train";
	private static final String RASA_PARSE = "/parse";

	private RasaHttpSenderUtil() {
		// util
	}

	public static String launchTraining(final String rasaUrl, final Map<String, Object> map) {
		final byte[] output = FileIOUtil.getYamlByteArrayFromMap(map);

		final HttpRequest request = HttpRequest.newBuilder(URI.create(rasaUrl + RASA_MODEL + RASA_TRAIN))
				.header("Content-Type", "application/x-yaml")
				.POST(BodyPublishers.ofByteArray(output))
				.build();

		final HttpResponse<InputStream> response = sendRequest(request, BodyHandlers.ofInputStream(), 200);
		return response.headers().map().get("filename").get(0);
	}

	public static void putModel(final String rasaUrl, final String filename) {
		//Create object to send
		final ObjectMapper mapper = FileIOUtil.createCustomObjectMapper();
		final ObjectNode node = FileIOUtil.createNode(mapper, "model_file", "models/" + filename);
		final String json = FileIOUtil.getJsonStringFromObject(mapper, node);

		//send request
		final HttpRequest request = HttpRequest.newBuilder(URI.create(rasaUrl + RASA_MODEL))
				.header("Content-Type", "application/json")
				.PUT(BodyPublishers.ofString(json))
				.build();

		sendRequest(request, BodyHandlers.ofString(), HttpServletResponse.SC_NO_CONTENT);
	}

	public static RasaParsingResponse getIntentFromRasa(final String rasaUrl, final MessageToRecognize message) {
		final ObjectMapper mapper = FileIOUtil.createCustomObjectMapper();
		final String json = FileIOUtil.getJsonStringFromObject(mapper, message);

		final HttpRequest request = HttpRequest.newBuilder(URI.create(rasaUrl + RASA_MODEL + RASA_PARSE))
				.header("Content-Type", "application/json")
				.POST(BodyPublishers.ofString(json))
				.build();

		final HttpResponse<String> response = sendRequest(request, BodyHandlers.ofString(), 200);
		return FileIOUtil.getObjectFromJson(mapper, response.body(), RasaParsingResponse.class);
	}

	/************** Request Part *********/

	private static <T extends Object> HttpResponse<T> sendRequest(final HttpRequest request, final BodyHandler<T> handler, final int successStatutCode) {
		final HttpClient client = HttpClient.newBuilder().version(Version.HTTP_1_1).build();
		try {
			final HttpResponse<T> response = client.send(request, handler);
			if (response.statusCode() != successStatutCode) {
				throw new VSystemException("Error while sending request to '{0}'. Expected HTTP code '{1}' but was '{2}'.", request.uri().toString(), successStatutCode, response.statusCode());
			}
			return response;
		} catch (IOException | InterruptedException e) {
			throw new VSystemException(e, "Error while sending request to '{0}'", request.uri().toString());
		}
	}
}
