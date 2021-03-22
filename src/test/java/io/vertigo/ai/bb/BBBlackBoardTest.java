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

public class BBBlackBoardTest {

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
	public void testExists() {
		Assertions.assertFalse(bb.exists("samplekey"));
		bb.incr("samplekey");
		Assertions.assertTrue(bb.exists("samplekey"));
		//--only some characters are accepted ; blanks are not permitted
		Assertions.assertThrows(Exception.class, () -> bb.exists("sample key"));
	}

	@Test
	public void testKeys() {
		Assertions.assertEquals(0, bb.keys("test").size());
		Assertions.assertEquals(0, bb.keys("test/*").size());
		Assertions.assertEquals(0, bb.keys("*").size());
		//---
		bb.incr("test");
		Assertions.assertEquals(1, bb.keys("test").size());
		Assertions.assertEquals(0, bb.keys("test/*").size());
		Assertions.assertEquals(1, bb.keys("*").size());
		//---
		bb.removeAll();
		bb.incr("test");
		bb.incr("test/1");
		bb.incr("test/2");
		Assertions.assertEquals(1, bb.keys("test").size());
		Assertions.assertEquals(2, bb.keys("test/*").size());
		Assertions.assertEquals(3, bb.keys("*").size());
		//--check the key pattern
		bb.removeAll();
		Assertions.assertThrows(Exception.class,
				() -> bb.keys(" sample"));
		Assertions.assertThrows(Exception.class,
				() -> bb.keys("sample**"));
		Assertions.assertThrows(Exception.class,
				() -> bb.keys("sample key"));
		Assertions.assertThrows(Exception.class,
				() -> bb.keys("/samplekey").isEmpty());
		Assertions.assertThrows(Exception.class,
				() -> bb.keys("samplekey/*/test").isEmpty());
		//--- keys and keys("*")
		bb.removeAll();
		bb.incr("test");
		bb.incr("test/1");
		bb.incr("test/3");
		bb.incr("test/4");
		Assertions.assertEquals(4, bb.keys("*").size());
		Assertions.assertEquals(4, bb.keys().size());
	}

	@Test
	public void testGetPut() {
		Assertions.assertEquals(null, bb.get("sample/key"));
		bb.put("sample/key", "test");
		Assertions.assertEquals("test", bb.get("sample/key"));
		bb.put("sample/key", "test2");
		Assertions.assertEquals("test2", bb.get("sample/key"));
	}

	@Test
	public void testFormat() {
		Assertions.assertEquals("", bb.format(""));
		Assertions.assertEquals("hello", bb.format("hello"));
		bb.put("sample/key", "test");
		Assertions.assertEquals("hello test", bb.format("hello {{sample/key}}"));
	}

	@Test
	public void testInc() {
		Assertions.assertEquals(null, bb.get("key"));
		bb.incr("key");
		Assertions.assertEquals("1", bb.get("key"));
		bb.incr("key");
		Assertions.assertEquals("2", bb.get("key"));
		bb.incrBy("key", 10);
		Assertions.assertEquals("12", bb.get("key"));
	}

	@Test
	public void testDec() {
		Assertions.assertEquals(null, bb.get("key"));
		bb.incrBy("key", 10);
		Assertions.assertEquals("10", bb.get("key"));
		bb.decr("key");
		Assertions.assertEquals("9", bb.get("key"));
		bb.decr("key");
		Assertions.assertEquals("8", bb.get("key"));
		bb.incrBy("key", -5);
		Assertions.assertEquals("3", bb.get("key"));
	}

	@Test
	public void testRemove() {
		Assertions.assertEquals(0, bb.keys().size());
		bb.incr("sample/1");
		bb.incr("sample/2");
		bb.incr("sample/3");
		bb.incr("sample/4");
		Assertions.assertEquals(4, bb.keys().size());
		bb.remove("sample/1");
		Assertions.assertEquals(3, bb.keys().size());
		bb.remove("*");
		Assertions.assertEquals(0, bb.keys().size());
		bb.incr("sample/1");
		bb.incr("sample/2");
		bb.incr("sample/3");
		bb.incr("sample/4");
		bb.removeAll();
		Assertions.assertEquals(0, bb.keys().size());
	}

	@Test
	public void testInteger() {
		Assertions.assertEquals(null, bb.getInteger("sample"));
		bb.incr("sample");
		Assertions.assertEquals(1, bb.getInteger("sample"));
		bb.incr("sample");
		Assertions.assertEquals(2, bb.getInteger("sample"));
		bb.putInteger("sample", 56);
		Assertions.assertEquals(56, bb.getInteger("sample"));
		Assertions.assertEquals(false, bb.lt("sample", "50"));
		Assertions.assertEquals(true, bb.gt("sample", "50"));
		Assertions.assertEquals(false, bb.eq("sample", "50"));
		Assertions.assertEquals(true, bb.eq("sample", "56"));
		bb.putInteger("sample", -55);
		Assertions.assertEquals("-55", bb.get("sample"));
		bb.incrBy("sample", 100);
		Assertions.assertEquals(45, bb.getInteger("sample"));
	}

	@Test
	public void testString() {
		Assertions.assertEquals(null, bb.get("sample"));
		bb.put("sample", "test");
		Assertions.assertEquals("test", bb.get("sample"));
		bb.removeAll();
		bb.append("sample", "hello");
		bb.append("sample", " ");
		bb.append("sample", "world");
		Assertions.assertEquals("hello world", bb.get("sample"));
	}

	@Test
	public void testList() {
		Assertions.assertEquals(0, bb.len("sample"));
		bb.push("sample", "a");
		bb.push("sample", "b");
		bb.push("sample", "c");
		Assertions.assertEquals(3, bb.len("sample"));
		Assertions.assertEquals("c", bb.pop("sample"));
		Assertions.assertEquals(2, bb.len("sample"));
		Assertions.assertEquals("b", bb.peek("sample"));
		Assertions.assertEquals(2, bb.len("sample"));
		bb.push("sample", "c");
		Assertions.assertEquals(3, bb.len("sample"));
		Assertions.assertEquals("a", bb.get("sample", 0));
		Assertions.assertEquals("b", bb.get("sample", 1));
		Assertions.assertEquals("c", bb.get("sample", 2));
		Assertions.assertEquals("c", bb.get("sample", -1));
		Assertions.assertEquals("b", bb.get("sample", -2));
		Assertions.assertEquals("a", bb.get("sample", -3));
		bb.pop("sample");
		bb.pop("sample");
		bb.pop("sample");
		bb.pop("sample");
		Assertions.assertEquals(0, bb.len("sample"));
	}

}
