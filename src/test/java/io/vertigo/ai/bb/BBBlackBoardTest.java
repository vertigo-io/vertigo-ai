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
	private BlackBoardManager blackBoardManager;

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
		final BlackBoard blackBoard = blackBoardManager.connect();
		//---
		Assertions.assertFalse(blackBoard.exists("samplekey"));
		blackBoard.incr("samplekey");
		Assertions.assertTrue(blackBoard.exists("samplekey"));
		//--only some characters are accepted ; blanks are not permitted
		Assertions.assertThrows(Exception.class, () -> blackBoard.exists("sample key"));
	}

	@Test
	public void testKeys() {
		final BlackBoard blackBoard = blackBoardManager.connect();
		//---
		Assertions.assertEquals(0, blackBoard.keys("test").size());
		Assertions.assertEquals(0, blackBoard.keys("test/*").size());
		Assertions.assertEquals(0, blackBoard.keys("*").size());
		//---
		blackBoard.incr("test");
		Assertions.assertEquals(1, blackBoard.keys("test").size());
		Assertions.assertEquals(0, blackBoard.keys("test/*").size());
		Assertions.assertEquals(1, blackBoard.keys("*").size());
		//---
		blackBoard.delete("*");
		blackBoard.incr("test");
		blackBoard.incr("test/1");
		blackBoard.incr("test/2");
		Assertions.assertEquals(1, blackBoard.keys("test").size());
		Assertions.assertEquals(2, blackBoard.keys("test/*").size());
		Assertions.assertEquals(3, blackBoard.keys("*").size());
		//--check the key pattern
		blackBoard.delete("*");
		Assertions.assertThrows(Exception.class,
				() -> blackBoard.keys(" sample"));
		Assertions.assertThrows(Exception.class,
				() -> blackBoard.keys("sample**"));
		Assertions.assertThrows(Exception.class,
				() -> blackBoard.keys("sample key"));
		Assertions.assertThrows(Exception.class,
				() -> blackBoard.keys("/samplekey").isEmpty());
		Assertions.assertThrows(Exception.class,
				() -> blackBoard.keys("samplekey/*/test").isEmpty());
		//--- keys and keys("*")
		blackBoard.delete("*");
		blackBoard.incr("test");
		blackBoard.incr("test/1");
		blackBoard.incr("test/3");
		blackBoard.incr("test/4");
		Assertions.assertEquals(4, blackBoard.keys("*").size());
		Assertions.assertEquals(4, blackBoard.keys("*").size());
	}

	@Test
	public void testGetPut() {
		final BlackBoard blackBoard = blackBoardManager.connect();
		//---
		Assertions.assertEquals(null, blackBoard.getString("sample/key"));
		blackBoard.putString("sample/key", "test");
		Assertions.assertEquals("test", blackBoard.getString("sample/key"));
		blackBoard.putString("sample/key", "test2");
		Assertions.assertEquals("test2", blackBoard.getString("sample/key"));
	}

	@Test
	public void testFormat() {
		final BlackBoard blackBoard = blackBoardManager.connect();
		//---
		Assertions.assertEquals("", blackBoard.format(""));
		Assertions.assertEquals("hello", blackBoard.format("hello"));
		blackBoard.putString("sample/key", "test");
		Assertions.assertEquals("hello test", blackBoard.format("hello {{sample/key}}"));
	}

	@Test
	public void testInc() {
		final BlackBoard blackBoard = blackBoardManager.connect();
		//---
		Assertions.assertEquals(null, blackBoard.getInteger("key"));
		blackBoard.incr("key");
		Assertions.assertEquals(1, blackBoard.getInteger("key"));
		blackBoard.incr("key");
		Assertions.assertEquals(2, blackBoard.getInteger("key"));
		blackBoard.incrBy("key", 10);
		Assertions.assertEquals(12, blackBoard.getInteger("key"));
	}

	@Test
	public void testDec() {
		final BlackBoard blackBoard = blackBoardManager.connect();
		//---
		Assertions.assertEquals(null, blackBoard.getInteger("key"));
		blackBoard.incrBy("key", 10);
		Assertions.assertEquals(10, blackBoard.getInteger("key"));
		blackBoard.decr("key");
		Assertions.assertEquals(9, blackBoard.getInteger("key"));
		blackBoard.decr("key");
		Assertions.assertEquals(8, blackBoard.getInteger("key"));
		blackBoard.incrBy("key", -5);
		Assertions.assertEquals(3, blackBoard.getInteger("key"));
	}

	@Test
	public void testRemove() {
		final BlackBoard blackBoard = blackBoardManager.connect();
		//---
		Assertions.assertEquals(0, blackBoard.keys("*").size());
		blackBoard.incr("sample/1");
		blackBoard.incr("sample/2");
		blackBoard.incr("sample/3");
		blackBoard.incr("sample/4");
		Assertions.assertEquals(4, blackBoard.keys("*").size());
		blackBoard.delete("sample/1");
		Assertions.assertEquals(3, blackBoard.keys("*").size());
		blackBoard.delete("*");
		Assertions.assertEquals(0, blackBoard.keys("*").size());
		blackBoard.incr("sample/1");
		blackBoard.incr("sample/2");
		blackBoard.incr("sample/3");
		blackBoard.incr("sample/4");
		blackBoard.delete("*");
		Assertions.assertEquals(0, blackBoard.keys("*").size());
	}

	@Test
	public void testInteger() {
		final BlackBoard blackBoard = blackBoardManager.connect();
		//---
		Assertions.assertEquals(null, blackBoard.getInteger("sample"));
		blackBoard.incr("sample");
		Assertions.assertEquals(1, blackBoard.getInteger("sample"));
		blackBoard.incr("sample");
		Assertions.assertEquals(2, blackBoard.getInteger("sample"));
		blackBoard.putInteger("sample", 56);
		Assertions.assertEquals(56, blackBoard.getInteger("sample"));
		Assertions.assertEquals(false, blackBoard.lt("sample", 50));
		Assertions.assertEquals(true, blackBoard.gt("sample", 50));
		Assertions.assertEquals(false, blackBoard.eq("sample", 50));
		Assertions.assertEquals(true, blackBoard.eq("sample", 56));
		blackBoard.putInteger("sample", -55);
		Assertions.assertEquals(-55, blackBoard.getInteger("sample"));
		blackBoard.incrBy("sample", 100);
		Assertions.assertEquals(45, blackBoard.getInteger("sample"));
	}

	@Test
	public void testString() {
		final BlackBoard blackBoard = blackBoardManager.connect();
		//---
		Assertions.assertEquals(null, blackBoard.getString("sample"));
		blackBoard.putString("sample", "test");
		Assertions.assertEquals("test", blackBoard.getString("sample"));
		blackBoard.delete("*");
		blackBoard.append("sample", "hello");
		blackBoard.append("sample", " ");
		blackBoard.append("sample", "world");
		Assertions.assertEquals("hello world", blackBoard.getString("sample"));
	}

	@Test
	public void testList() {
		final BlackBoard blackBoard = blackBoardManager.connect();
		//---
		Assertions.assertEquals(0, blackBoard.listSize("sample"));
		blackBoard.listPush("sample", "a");
		blackBoard.listPush("sample", "b");
		blackBoard.listPush("sample", "c");
		Assertions.assertEquals(3, blackBoard.listSize("sample"));
		Assertions.assertEquals("c", blackBoard.listPop("sample"));
		Assertions.assertEquals(2, blackBoard.listSize("sample"));
		Assertions.assertEquals("b", blackBoard.listPeek("sample"));
		Assertions.assertEquals(2, blackBoard.listSize("sample"));
		blackBoard.listPush("sample", "c");
		Assertions.assertEquals(3, blackBoard.listSize("sample"));
		Assertions.assertEquals("a", blackBoard.listGet("sample", 0));
		Assertions.assertEquals("b", blackBoard.listGet("sample", 1));
		Assertions.assertEquals("c", blackBoard.listGet("sample", 2));
		Assertions.assertEquals("c", blackBoard.listGet("sample", -1));
		Assertions.assertEquals("b", blackBoard.listGet("sample", -2));
		Assertions.assertEquals("a", blackBoard.listGet("sample", -3));
		blackBoard.listPop("sample");
		blackBoard.listPop("sample");
		blackBoard.listPop("sample");
		blackBoard.listPop("sample");
		Assertions.assertEquals(0, blackBoard.listSize("sample"));
	}

}
