package io.vertigo.ai.nlu;

import static io.vertigo.ai.nlu.NluManager.DEFAULT_ENGINE_NAME;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, Vertigo.io, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.ai.AiFeatures;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.core.plugins.resource.url.URLResourceResolverPlugin;

/**
 * Test of the NLU manager.
 *
 * @author skerdudou
 */
public final class RasaNluManagerTest {

	private AutoCloseableNode node;

	@Inject
	private NluManager nluManager;

	private void defaultIntentCorpus() {

		final Map<NluIntent, List<String>> nluIntents = new HashMap<>();
		nluIntents.put(
				NluIntent.of("meteo"),
				List.of(
						"quel temps fait-il demain ?",
						"donne moi la météo",
						"C'est quoi la météo pour demain ?",
						"il fait beau demain ?",
						"va t il pleuvoir dans les prochains jours ?"));

		nluIntents.put(
				NluIntent.of("train"),
				List.of("je voudrais prendre le train",
						"réserver billet de train",
						"réserve-moi un ticket de train",
						"je veux un billet de train"));

		nluIntents.put(
				NluIntent.of("blague"),
				List.of("raconte moi une blague",
						"donne moi une blague",
						"fais moi rire",
						"t'a pas une blague pour moi ?",
						"je veux une blague",
						"je voudrais une blague"));

		nluManager.train(nluIntents, DEFAULT_ENGINE_NAME);
	}

	@Test
	public void testNlu() {
		defaultIntentCorpus();

		var result = nluManager.recognize("je veux rire", DEFAULT_ENGINE_NAME);
		Assertions.assertFalse(result.getScoredIntents().isEmpty());
		Assertions.assertEquals("blague", result.getScoredIntents().get(0).getIntent().getCode());

		result = nluManager.recognize("j'ai un train a prendre", DEFAULT_ENGINE_NAME);
		Assertions.assertFalse(result.getScoredIntents().isEmpty());
		Assertions.assertEquals("train", result.getScoredIntents().get(0).getIntent().getCode());

		result = nluManager.recognize("quel est la météo demain", DEFAULT_ENGINE_NAME);
		Assertions.assertEquals("meteo", result.getScoredIntents().get(0).getIntent().getCode());
		Assertions.assertEquals(true, result.getScoredIntents().get(0).getAccuracy() > 0.4D);
	}

	@BeforeEach
	public final void setUp() {
		node = new AutoCloseableNode(buildNodeConfig());
		DIInjector.injectMembers(this, node.getComponentSpace());
	}

	@AfterEach
	public final void tearDown() {
		if (node != null) {
			node.close();
		}
	}

	private NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.withBoot(BootConfig.builder()
						.withLocales("fr_FR")
						.addPlugin(ClassPathResourceResolverPlugin.class)
						.addPlugin(URLResourceResolverPlugin.class)
						.build())
				.addModule(new AiFeatures()
						.withNLU()
						.withRasaNLU(Param.of("rasaUrl", "http://docker-vertigo.part.klee.lan.net:5005"))
						.build())
				.build();
	}

}
