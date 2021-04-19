package io.vertigo.ai.bt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.ai.AiFeatures;
import io.vertigo.ai.bt.parser.BtCommandManager;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.NodeConfig;

public class BTParserTest {

	@Inject
	private BtCommandManager btCommandManager;

	private AutoCloseableNode node;

	@BeforeEach
	public final void setUp() throws Exception {
		node = new AutoCloseableNode(buildNodeConfig());
		DIInjector.injectMembers(this, node.getComponentSpace());
	}

	private NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.addModule(
						new AiFeatures()
								.withParser()
								.build())
				.build();
	}

	@AfterEach
	public final void tearDown() throws Exception {
		if (node != null) {
			node.close();
		}
	}

	@Test
	public void testSimpleSequence() {
		final String bt = "begin sequence\n" +
				"end sequence";

		final Function<List<Object>, BTNode> nodeProducer = btCommandManager.parse(bt);
		final BTNode rootNode = nodeProducer.apply(Collections.emptyList());
		final BTStatus status = rootNode.eval();
		//---
		Assertions.assertEquals(BTStatus.Succeeded, status);
	}

	@Test
	public void testSimpleSelector() {
		final String bt = "begin selector\n" +
				"end selector";

		final Function<List<Object>, BTNode> nodeProducer = btCommandManager.parse(bt);
		final BTNode rootNode = nodeProducer.apply(Collections.emptyList());
		final BTStatus status = rootNode.eval();
		//---
		Assertions.assertEquals(BTStatus.Failed, status);
	}

	@Test
	public void testCloseOther() {
		final String bt = "begin sequence\n" +
				"end selector";

		final Exception exception = Assertions.assertThrows(IllegalStateException.class, () -> {
			final Function<List<Object>, BTNode> nodeProducer = btCommandManager.parse(bt);
			nodeProducer.apply(Collections.emptyList());
		});

		assertEquals("Cannot close 'selector', currently on 'sequence'", exception.getMessage());
	}

	@Test
	public void testMultipleRoot() {
		final String bt = "begin sequence\n" +
				"end sequence\n" +
				"begin selector\n" +
				"end selector";

		final Exception exception = Assertions.assertThrows(IllegalStateException.class, () -> {
			final Function<List<Object>, BTNode> nodeProducer = btCommandManager.parse(bt);
			nodeProducer.apply(Collections.emptyList());
		});

		assertEquals("Commands after root node is not supported.", exception.getMessage());
	}

}
