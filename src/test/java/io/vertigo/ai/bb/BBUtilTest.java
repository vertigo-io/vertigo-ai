package io.vertigo.ai.bb;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.ai.impl.bb.BlackBoardManagerImpl;
import io.vertigo.ai.plugins.bb.memory.MemoryBlackBoardStorePlugin;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;

public class BBUtilTest {

	@Inject
	private BlackBoardManager blackBoardManager;
	@Inject
	private VTransactionManager transactionManager;

	private AutoCloseableNode node;

	@BeforeEach
	public final void setUp() throws Exception {
		node = new AutoCloseableNode(buildNodeConfig());
		DIInjector.injectMembers(this, node.getComponentSpace());
	}

	private NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.addModule(new CommonsFeatures().build())// for transactions
				.addModule(
						ModuleConfig.builder("myModule")
								.addComponent(BlackBoardManager.class, BlackBoardManagerImpl.class)
								.addPlugin(MemoryBlackBoardStorePlugin.class)
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
	public void testFormatter0() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final BlackBoard blackBoard = blackBoardManager.connect();
			//---
			Assertions.assertEquals("hello world", blackBoard.format("hello world"));
		}
	}

	@Test
	public void testFormatter1() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final BlackBoard blackBoard = blackBoardManager.connect();
			//---
			Assertions.assertEquals("hello world", blackBoard.format("hello world"));
			blackBoard.putString("name", "joe");
			blackBoard.putString("lastname", "diMagio");
			//---
			Assertions.assertEquals("joe", blackBoard.format("{{name}}"));
			Assertions.assertEquals("hello joe", blackBoard.format("hello {{name}}"));
			Assertions.assertEquals("hello joe...", blackBoard.format("hello {{name}}..."));
			Assertions.assertEquals("hello joe diMagio", blackBoard.format("hello {{name}} {{lastname}}"));
			Assertions.assertThrows(IllegalStateException.class,
					() -> blackBoard.format("hello {{name}"));
			Assertions.assertThrows(IllegalStateException.class,
					() -> blackBoard.format("hello {{name"));
			Assertions.assertThrows(IllegalStateException.class,
					() -> blackBoard.format("hello name}}"));
		}
	}

	@Test
	public void testFormatter2() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final BlackBoard blackBoard = blackBoardManager.connect();
			//---
			blackBoard.putString("u/1/name", "alan");
			blackBoard.putString("u/2/name", "ada");
			blackBoard.putString("u/idx", "2");
			Assertions.assertEquals("hello ada", blackBoard.format("hello {{u/{{u/idx}}/name}}"));
		}
	}
}
