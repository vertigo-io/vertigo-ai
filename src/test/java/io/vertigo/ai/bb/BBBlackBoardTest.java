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

public class BBBlackBoardTest {

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
	public void testExists() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			blackBoardManager.useDefaultStore();
			Assertions.assertFalse(blackBoardManager.exists("samplekey"));
			blackBoardManager.incr("samplekey");
			Assertions.assertTrue(blackBoardManager.exists("samplekey"));
			//--only some characters are accepted ; blanks are not permitted
			Assertions.assertThrows(Exception.class, () -> blackBoardManager.exists("sample key"));
		}
	}

	@Test
	public void testKeys() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			blackBoardManager.useDefaultStore();
			Assertions.assertEquals(0, blackBoardManager.keys("test").size());
			Assertions.assertEquals(0, blackBoardManager.keys("test/*").size());
			Assertions.assertEquals(0, blackBoardManager.keys("*").size());
			//---
			blackBoardManager.incr("test");
			Assertions.assertEquals(1, blackBoardManager.keys("test").size());
			Assertions.assertEquals(0, blackBoardManager.keys("test/*").size());
			Assertions.assertEquals(1, blackBoardManager.keys("*").size());
			//---
			blackBoardManager.removeAll();
			blackBoardManager.incr("test");
			blackBoardManager.incr("test/1");
			blackBoardManager.incr("test/2");
			Assertions.assertEquals(1, blackBoardManager.keys("test").size());
			Assertions.assertEquals(2, blackBoardManager.keys("test/*").size());
			Assertions.assertEquals(3, blackBoardManager.keys("*").size());
			//--check the key pattern
			blackBoardManager.removeAll();
			Assertions.assertThrows(Exception.class,
					() -> blackBoardManager.keys(" sample"));
			Assertions.assertThrows(Exception.class,
					() -> blackBoardManager.keys("sample**"));
			Assertions.assertThrows(Exception.class,
					() -> blackBoardManager.keys("sample key"));
			Assertions.assertThrows(Exception.class,
					() -> blackBoardManager.keys("/samplekey").isEmpty());
			Assertions.assertThrows(Exception.class,
					() -> blackBoardManager.keys("samplekey/*/test").isEmpty());
			//--- keys and keys("*")
			blackBoardManager.removeAll();
			blackBoardManager.incr("test");
			blackBoardManager.incr("test/1");
			blackBoardManager.incr("test/3");
			blackBoardManager.incr("test/4");
			Assertions.assertEquals(4, blackBoardManager.keys("*").size());
			Assertions.assertEquals(4, blackBoardManager.keys().size());
		}
	}

	@Test
	public void testGetPut() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			blackBoardManager.useDefaultStore();
			Assertions.assertEquals(null, blackBoardManager.get("sample/key"));
			blackBoardManager.put("sample/key", "test");
			Assertions.assertEquals("test", blackBoardManager.get("sample/key"));
			blackBoardManager.put("sample/key", "test2");
			Assertions.assertEquals("test2", blackBoardManager.get("sample/key"));
		}
	}

	@Test
	public void testFormat() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			blackBoardManager.useDefaultStore();
			Assertions.assertEquals("", blackBoardManager.format(""));
			Assertions.assertEquals("hello", blackBoardManager.format("hello"));
			blackBoardManager.put("sample/key", "test");
			Assertions.assertEquals("hello test", blackBoardManager.format("hello {{sample/key}}"));
		}
	}

	@Test
	public void testInc() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			blackBoardManager.useDefaultStore();
			Assertions.assertEquals(null, blackBoardManager.get("key"));
			blackBoardManager.incr("key");
			Assertions.assertEquals("1", blackBoardManager.get("key"));
			blackBoardManager.incr("key");
			Assertions.assertEquals("2", blackBoardManager.get("key"));
			blackBoardManager.incrBy("key", 10);
			Assertions.assertEquals("12", blackBoardManager.get("key"));
		}
	}

	@Test
	public void testDec() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			blackBoardManager.useDefaultStore();
			Assertions.assertEquals(null, blackBoardManager.get("key"));
			blackBoardManager.incrBy("key", 10);
			Assertions.assertEquals("10", blackBoardManager.get("key"));
			blackBoardManager.decr("key");
			Assertions.assertEquals("9", blackBoardManager.get("key"));
			blackBoardManager.decr("key");
			Assertions.assertEquals("8", blackBoardManager.get("key"));
			blackBoardManager.incrBy("key", -5);
			Assertions.assertEquals("3", blackBoardManager.get("key"));
		}
	}

	@Test
	public void testRemove() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			blackBoardManager.useDefaultStore();
			Assertions.assertEquals(0, blackBoardManager.keys().size());
			blackBoardManager.incr("sample/1");
			blackBoardManager.incr("sample/2");
			blackBoardManager.incr("sample/3");
			blackBoardManager.incr("sample/4");
			Assertions.assertEquals(4, blackBoardManager.keys().size());
			blackBoardManager.remove("sample/1");
			Assertions.assertEquals(3, blackBoardManager.keys().size());
			blackBoardManager.remove("*");
			Assertions.assertEquals(0, blackBoardManager.keys().size());
			blackBoardManager.incr("sample/1");
			blackBoardManager.incr("sample/2");
			blackBoardManager.incr("sample/3");
			blackBoardManager.incr("sample/4");
			blackBoardManager.removeAll();
			Assertions.assertEquals(0, blackBoardManager.keys().size());
		}
	}

	@Test
	public void testInteger() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			blackBoardManager.useDefaultStore();
			Assertions.assertEquals(null, blackBoardManager.getInteger("sample"));
			blackBoardManager.incr("sample");
			Assertions.assertEquals(1, blackBoardManager.getInteger("sample"));
			blackBoardManager.incr("sample");
			Assertions.assertEquals(2, blackBoardManager.getInteger("sample"));
			blackBoardManager.putInteger("sample", 56);
			Assertions.assertEquals(56, blackBoardManager.getInteger("sample"));
			Assertions.assertEquals(false, blackBoardManager.lt("sample", "50"));
			Assertions.assertEquals(true, blackBoardManager.gt("sample", "50"));
			Assertions.assertEquals(false, blackBoardManager.eq("sample", "50"));
			Assertions.assertEquals(true, blackBoardManager.eq("sample", "56"));
			blackBoardManager.putInteger("sample", -55);
			Assertions.assertEquals("-55", blackBoardManager.get("sample"));
			blackBoardManager.incrBy("sample", 100);
			Assertions.assertEquals(45, blackBoardManager.getInteger("sample"));
		}
	}

	@Test
	public void testString() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			blackBoardManager.useDefaultStore();
			Assertions.assertEquals(null, blackBoardManager.get("sample"));
			blackBoardManager.put("sample", "test");
			Assertions.assertEquals("test", blackBoardManager.get("sample"));
			blackBoardManager.removeAll();
			blackBoardManager.append("sample", "hello");
			blackBoardManager.append("sample", " ");
			blackBoardManager.append("sample", "world");
			Assertions.assertEquals("hello world", blackBoardManager.get("sample"));
		}
	}

	@Test
	public void testList() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			blackBoardManager.useDefaultStore();
			Assertions.assertEquals(0, blackBoardManager.len("sample"));
			blackBoardManager.push("sample", "a");
			blackBoardManager.push("sample", "b");
			blackBoardManager.push("sample", "c");
			Assertions.assertEquals(3, blackBoardManager.len("sample"));
			Assertions.assertEquals("c", blackBoardManager.pop("sample"));
			Assertions.assertEquals(2, blackBoardManager.len("sample"));
			Assertions.assertEquals("b", blackBoardManager.peek("sample"));
			Assertions.assertEquals(2, blackBoardManager.len("sample"));
			blackBoardManager.push("sample", "c");
			Assertions.assertEquals(3, blackBoardManager.len("sample"));
			Assertions.assertEquals("a", blackBoardManager.get("sample", 0));
			Assertions.assertEquals("b", blackBoardManager.get("sample", 1));
			Assertions.assertEquals("c", blackBoardManager.get("sample", 2));
			Assertions.assertEquals("c", blackBoardManager.get("sample", -1));
			Assertions.assertEquals("b", blackBoardManager.get("sample", -2));
			Assertions.assertEquals("a", blackBoardManager.get("sample", -3));
			blackBoardManager.pop("sample");
			blackBoardManager.pop("sample");
			blackBoardManager.pop("sample");
			blackBoardManager.pop("sample");
			Assertions.assertEquals(0, blackBoardManager.len("sample"));
		}
	}

}
