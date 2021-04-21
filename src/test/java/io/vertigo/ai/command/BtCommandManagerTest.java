package io.vertigo.ai.command;

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
import io.vertigo.ai.bt.BTNode;
import io.vertigo.ai.bt.BTStatus;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.NodeConfig;

public class BtCommandManagerTest {

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

	private BTStatus eval(final String commands) {
		final Function<List<Object>, BTNode> nodeProducer = btCommandManager.parse(commands);
		final BTNode rootNode = nodeProducer.apply(Collections.emptyList());
		return rootNode.eval();
	}

	@Test
	public void testSucceed() {
		final String bt = "succeed";
		final BTStatus status = eval(bt);
		//---
		Assertions.assertEquals(BTStatus.Succeeded, status);
	}

	@Test
	public void testFail() {
		final String bt = "fail";
		final BTStatus status = eval(bt);
		//---
		Assertions.assertEquals(BTStatus.Failed, status);
	}

	@Test
	public void testRunning() {
		final String bt = "running";
		final BTStatus status = eval(bt);
		//---
		Assertions.assertEquals(BTStatus.Running, status);
	}

	@Test
	public void testSimpleSequence() {
		// An empty sequence always succeeds
		final String bt = "begin sequence\n" +
				"end sequence";
		final BTStatus status = eval(bt);
		//---
		Assertions.assertEquals(BTStatus.Succeeded, status);
	}

	@Test
	public void testSimpleSelector() {
		// An empty selector always fails
		final String bt = "begin selector\n" +
				"end selector";
		final BTStatus status = eval(bt);
		//---
		Assertions.assertEquals(BTStatus.Failed, status);
	}

	@Test
	public void testCloseOther() {
		final String bt = "begin sequence\n" +
				"end selector";

		final Exception exception = Assertions.assertThrows(IllegalStateException.class, () -> eval(bt));
		assertEquals("Cannot close 'selector', currently on 'sequence'", exception.getMessage());
	}

	@Test
	public void testMultipleRoot() {
		final String bt = "begin sequence\n" +
				"end sequence\n" +
				"begin selector\n" +
				"end selector";

		final Exception exception = Assertions.assertThrows(IllegalStateException.class, () -> eval(bt));

		assertEquals("Commands after root node is not supported.", exception.getMessage());
	}

	@Test
	public void testSubNodes() {
		final String bt = "begin sequence\n" +
				"	begin sequence\n" +
				"	end sequence\n" +
				"	begin sequence\n" +
				"	end sequence\n" +
				"end sequence";

		final BTStatus status = eval(bt);
		//---
		Assertions.assertEquals(BTStatus.Succeeded, status);
	}

	@Test
	public void testUnbalanced() {
		final String bt = "begin sequence\n" +
				"	begin sequence\n" +
				"end sequence\n";

		final Exception exception = Assertions.assertThrows(IllegalStateException.class, () -> eval(bt));

		assertEquals("Node 'sequence' not ended.", exception.getMessage());
	}

	@Test
	public void testParams() {
		final String bt = "begin sequence param1 param2\n" +
				"end sequence";

		final BTStatus status = eval(bt);
		//---
		Assertions.assertEquals(BTStatus.Succeeded, status);
	}

	@Test
	public void testQuotedParams() {
		final String bt = "begin sequence \"param 1\" \"param 2\"\n" +
				"end sequence";

		final BTStatus status = eval(bt);
		//---
		Assertions.assertEquals(BTStatus.Succeeded, status);
	}

	@Test
	public void testQuotedParamsErr1() {
		final String bt = "begin sequence \"param 1\n" +
				"end sequence";

		final Exception exception = Assertions.assertThrows(IllegalStateException.class, () -> eval(bt));

		assertEquals("End quote not found. '\"param 1'", exception.getMessage());
	}

	@Test
	public void testQuotedParamsErr2() {
		final String bt = "begin sequence \"param 1\"param2\n" +
				"end sequence";

		final Exception exception = Assertions.assertThrows(IllegalStateException.class, () -> eval(bt));

		assertEquals("Text is not allowed just after quotes, please add a space. '\"param 1\"param2'", exception.getMessage());
	}

	@Test
	public void testQuotedParamsErr3() {
		final String bt = "begin sequence param\"1\n" +
				"end sequence";

		final Exception exception = Assertions.assertThrows(IllegalStateException.class, () -> eval(bt));

		assertEquals("Quotes are only allowed around text or escaped inside quotes. 'param\"1'", exception.getMessage());
	}

	@Test
	public void testComments() {
		final String bt = " -- end sequence \"param 1\" \"param 2\"\n" +
				"begin sequence -- a\"a\n" +
				"end sequence";

		final BTStatus status = eval(bt);
		//---
		Assertions.assertEquals(BTStatus.Succeeded, status);
	}

	@Test
	public void testUseCompositeWithoutBegin() {
		final String bt = "begin sequence\n" +
				"	sequence\n" +
				"end sequence";

		final Exception exception = Assertions.assertThrows(IllegalStateException.class, () -> eval(bt));

		assertEquals("The command parser is not for the correct type", exception.getMessage());
	}

}
