package io.vertigo.ai.bb;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.ai.impl.bb.BlackBoardManagerImpl;
import io.vertigo.ai.plugins.bb.memory.MemoryBlackBoardStorePlugin;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;

public class BBUtilTest {

	@Inject
	private BlackBoardManager bb;
	private AutoCloseableNode node;

	@BeforeEach
	public final void setUp() throws Exception {
		node = new AutoCloseableNode(buildNodeConfig());
		DIInjector.injectMembers(this, node.getComponentSpace());
	}

	private NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
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
		Assertions.assertEquals("hello world", bb.format("hello world"));
	}

	@Test
	public void testFormatter1() {
		Assertions.assertEquals("hello world", bb.format("hello world"));
		bb.put("name", "joe");
		bb.put("lastname", "diMagio");
		//---
		Assertions.assertEquals("joe", bb.format("{{name}}"));
		Assertions.assertEquals("hello joe", bb.format("hello {{name}}"));
		Assertions.assertEquals("hello joe...", bb.format("hello {{name}}..."));
		Assertions.assertEquals("hello joe diMagio", bb.format("hello {{name}} {{lastname}}"));
		Assertions.assertThrows(IllegalStateException.class,
				() -> bb.format("hello {{name}"));
		Assertions.assertThrows(IllegalStateException.class,
				() -> bb.format("hello {{name"));
		Assertions.assertThrows(IllegalStateException.class,
				() -> bb.format("hello name}}"));
	}

	@Test
	public void testFormatter2() {
		bb.put("u/1/name", "alan");
		bb.put("u/2/name", "ada");
		bb.put("u/idx", "2");
		Assertions.assertEquals("hello ada", bb.format("hello {{u/{{u/idx}}/name}}"));
	}
}
