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
			Assertions.assertFalse(blackBoardManager.exists(BlackBoardManager.MAIN_STORE_NAME, "samplekey"));
			blackBoardManager.incr(BlackBoardManager.MAIN_STORE_NAME, "samplekey");
			Assertions.assertTrue(blackBoardManager.exists(BlackBoardManager.MAIN_STORE_NAME, "samplekey"));
			//--only some characters are accepted ; blanks are not permitted
			Assertions.assertThrows(Exception.class, () -> blackBoardManager.exists(BlackBoardManager.MAIN_STORE_NAME, "sample key"));
		}
	}

	@Test
	public void testKeys() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			Assertions.assertEquals(0, blackBoardManager.keys(BlackBoardManager.MAIN_STORE_NAME, "test").size());
			Assertions.assertEquals(0, blackBoardManager.keys(BlackBoardManager.MAIN_STORE_NAME, "test/*").size());
			Assertions.assertEquals(0, blackBoardManager.keys(BlackBoardManager.MAIN_STORE_NAME, "*").size());
			//---
			blackBoardManager.incr(BlackBoardManager.MAIN_STORE_NAME, "test");
			Assertions.assertEquals(1, blackBoardManager.keys(BlackBoardManager.MAIN_STORE_NAME, "test").size());
			Assertions.assertEquals(0, blackBoardManager.keys(BlackBoardManager.MAIN_STORE_NAME, "test/*").size());
			Assertions.assertEquals(1, blackBoardManager.keys(BlackBoardManager.MAIN_STORE_NAME, "*").size());
			//---
			blackBoardManager.removeAll(BlackBoardManager.MAIN_STORE_NAME);
			blackBoardManager.incr(BlackBoardManager.MAIN_STORE_NAME, "test");
			blackBoardManager.incr(BlackBoardManager.MAIN_STORE_NAME, "test/1");
			blackBoardManager.incr(BlackBoardManager.MAIN_STORE_NAME, "test/2");
			Assertions.assertEquals(1, blackBoardManager.keys(BlackBoardManager.MAIN_STORE_NAME, "test").size());
			Assertions.assertEquals(2, blackBoardManager.keys(BlackBoardManager.MAIN_STORE_NAME, "test/*").size());
			Assertions.assertEquals(3, blackBoardManager.keys(BlackBoardManager.MAIN_STORE_NAME, "*").size());
			//--check the key pattern
			blackBoardManager.removeAll(BlackBoardManager.MAIN_STORE_NAME);
			Assertions.assertThrows(Exception.class,
					() -> blackBoardManager.keys(BlackBoardManager.MAIN_STORE_NAME, " sample"));
			Assertions.assertThrows(Exception.class,
					() -> blackBoardManager.keys(BlackBoardManager.MAIN_STORE_NAME, "sample**"));
			Assertions.assertThrows(Exception.class,
					() -> blackBoardManager.keys(BlackBoardManager.MAIN_STORE_NAME, "sample key"));
			Assertions.assertThrows(Exception.class,
					() -> blackBoardManager.keys(BlackBoardManager.MAIN_STORE_NAME, "/samplekey").isEmpty());
			Assertions.assertThrows(Exception.class,
					() -> blackBoardManager.keys(BlackBoardManager.MAIN_STORE_NAME, "samplekey/*/test").isEmpty());
			//--- keys and keys("*")
			blackBoardManager.removeAll(BlackBoardManager.MAIN_STORE_NAME);
			blackBoardManager.incr(BlackBoardManager.MAIN_STORE_NAME, "test");
			blackBoardManager.incr(BlackBoardManager.MAIN_STORE_NAME, "test/1");
			blackBoardManager.incr(BlackBoardManager.MAIN_STORE_NAME, "test/3");
			blackBoardManager.incr(BlackBoardManager.MAIN_STORE_NAME, "test/4");
			Assertions.assertEquals(4, blackBoardManager.keys(BlackBoardManager.MAIN_STORE_NAME, "*").size());
			Assertions.assertEquals(4, blackBoardManager.keys(BlackBoardManager.MAIN_STORE_NAME).size());
		}
	}

	@Test
	public void testGetPut() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			Assertions.assertEquals(null, blackBoardManager.get(BlackBoardManager.MAIN_STORE_NAME, "sample/key"));
			blackBoardManager.put(BlackBoardManager.MAIN_STORE_NAME, "sample/key", "test");
			Assertions.assertEquals("test", blackBoardManager.get(BlackBoardManager.MAIN_STORE_NAME, "sample/key"));
			blackBoardManager.put(BlackBoardManager.MAIN_STORE_NAME, "sample/key", "test2");
			Assertions.assertEquals("test2", blackBoardManager.get(BlackBoardManager.MAIN_STORE_NAME, "sample/key"));
		}
	}

	@Test
	public void testFormat() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			Assertions.assertEquals("", blackBoardManager.format(BlackBoardManager.MAIN_STORE_NAME, ""));
			Assertions.assertEquals("hello", blackBoardManager.format(BlackBoardManager.MAIN_STORE_NAME, "hello"));
			blackBoardManager.put(BlackBoardManager.MAIN_STORE_NAME, "sample/key", "test");
			Assertions.assertEquals("hello test", blackBoardManager.format(BlackBoardManager.MAIN_STORE_NAME, "hello {{sample/key}}"));
		}
	}

	@Test
	public void testInc() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			Assertions.assertEquals(null, blackBoardManager.get(BlackBoardManager.MAIN_STORE_NAME, "key"));
			blackBoardManager.incr(BlackBoardManager.MAIN_STORE_NAME, "key");
			Assertions.assertEquals("1", blackBoardManager.get(BlackBoardManager.MAIN_STORE_NAME, "key"));
			blackBoardManager.incr(BlackBoardManager.MAIN_STORE_NAME, "key");
			Assertions.assertEquals("2", blackBoardManager.get(BlackBoardManager.MAIN_STORE_NAME, "key"));
			blackBoardManager.incrBy(BlackBoardManager.MAIN_STORE_NAME, "key", 10);
			Assertions.assertEquals("12", blackBoardManager.get(BlackBoardManager.MAIN_STORE_NAME, "key"));
		}
	}

	@Test
	public void testDec() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			Assertions.assertEquals(null, blackBoardManager.get(BlackBoardManager.MAIN_STORE_NAME, "key"));
			blackBoardManager.incrBy(BlackBoardManager.MAIN_STORE_NAME, "key", 10);
			Assertions.assertEquals("10", blackBoardManager.get(BlackBoardManager.MAIN_STORE_NAME, "key"));
			blackBoardManager.decr(BlackBoardManager.MAIN_STORE_NAME, "key");
			Assertions.assertEquals("9", blackBoardManager.get(BlackBoardManager.MAIN_STORE_NAME, "key"));
			blackBoardManager.decr(BlackBoardManager.MAIN_STORE_NAME, "key");
			Assertions.assertEquals("8", blackBoardManager.get(BlackBoardManager.MAIN_STORE_NAME, "key"));
			blackBoardManager.incrBy(BlackBoardManager.MAIN_STORE_NAME, "key", -5);
			Assertions.assertEquals("3", blackBoardManager.get(BlackBoardManager.MAIN_STORE_NAME, "key"));
		}
	}

	@Test
	public void testRemove() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			Assertions.assertEquals(0, blackBoardManager.keys(BlackBoardManager.MAIN_STORE_NAME).size());
			blackBoardManager.incr(BlackBoardManager.MAIN_STORE_NAME, "sample/1");
			blackBoardManager.incr(BlackBoardManager.MAIN_STORE_NAME, "sample/2");
			blackBoardManager.incr(BlackBoardManager.MAIN_STORE_NAME, "sample/3");
			blackBoardManager.incr(BlackBoardManager.MAIN_STORE_NAME, "sample/4");
			Assertions.assertEquals(4, blackBoardManager.keys(BlackBoardManager.MAIN_STORE_NAME).size());
			blackBoardManager.remove(BlackBoardManager.MAIN_STORE_NAME, "sample/1");
			Assertions.assertEquals(3, blackBoardManager.keys(BlackBoardManager.MAIN_STORE_NAME).size());
			blackBoardManager.remove(BlackBoardManager.MAIN_STORE_NAME, "*");
			Assertions.assertEquals(0, blackBoardManager.keys(BlackBoardManager.MAIN_STORE_NAME).size());
			blackBoardManager.incr(BlackBoardManager.MAIN_STORE_NAME, "sample/1");
			blackBoardManager.incr(BlackBoardManager.MAIN_STORE_NAME, "sample/2");
			blackBoardManager.incr(BlackBoardManager.MAIN_STORE_NAME, "sample/3");
			blackBoardManager.incr(BlackBoardManager.MAIN_STORE_NAME, "sample/4");
			blackBoardManager.removeAll(BlackBoardManager.MAIN_STORE_NAME);
			Assertions.assertEquals(0, blackBoardManager.keys(BlackBoardManager.MAIN_STORE_NAME).size());
		}
	}

	@Test
	public void testInteger() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			Assertions.assertEquals(null, blackBoardManager.getInteger(BlackBoardManager.MAIN_STORE_NAME, "sample"));
			blackBoardManager.incr(BlackBoardManager.MAIN_STORE_NAME, "sample");
			Assertions.assertEquals(1, blackBoardManager.getInteger(BlackBoardManager.MAIN_STORE_NAME, "sample"));
			blackBoardManager.incr(BlackBoardManager.MAIN_STORE_NAME, "sample");
			Assertions.assertEquals(2, blackBoardManager.getInteger(BlackBoardManager.MAIN_STORE_NAME, "sample"));
			blackBoardManager.putInteger(BlackBoardManager.MAIN_STORE_NAME, "sample", 56);
			Assertions.assertEquals(56, blackBoardManager.getInteger(BlackBoardManager.MAIN_STORE_NAME, "sample"));
			Assertions.assertEquals(false, blackBoardManager.lt(BlackBoardManager.MAIN_STORE_NAME, "sample", "50"));
			Assertions.assertEquals(true, blackBoardManager.gt(BlackBoardManager.MAIN_STORE_NAME, "sample", "50"));
			Assertions.assertEquals(false, blackBoardManager.eq(BlackBoardManager.MAIN_STORE_NAME, "sample", "50"));
			Assertions.assertEquals(true, blackBoardManager.eq(BlackBoardManager.MAIN_STORE_NAME, "sample", "56"));
			blackBoardManager.putInteger(BlackBoardManager.MAIN_STORE_NAME, "sample", -55);
			Assertions.assertEquals("-55", blackBoardManager.get(BlackBoardManager.MAIN_STORE_NAME, "sample"));
			blackBoardManager.incrBy(BlackBoardManager.MAIN_STORE_NAME, "sample", 100);
			Assertions.assertEquals(45, blackBoardManager.getInteger(BlackBoardManager.MAIN_STORE_NAME, "sample"));
		}
	}

	@Test
	public void testString() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			Assertions.assertEquals(null, blackBoardManager.get(BlackBoardManager.MAIN_STORE_NAME, "sample"));
			blackBoardManager.put(BlackBoardManager.MAIN_STORE_NAME, "sample", "test");
			Assertions.assertEquals("test", blackBoardManager.get(BlackBoardManager.MAIN_STORE_NAME, "sample"));
			blackBoardManager.removeAll(BlackBoardManager.MAIN_STORE_NAME);
			blackBoardManager.append(BlackBoardManager.MAIN_STORE_NAME, "sample", "hello");
			blackBoardManager.append(BlackBoardManager.MAIN_STORE_NAME, "sample", " ");
			blackBoardManager.append(BlackBoardManager.MAIN_STORE_NAME, "sample", "world");
			Assertions.assertEquals("hello world", blackBoardManager.get(BlackBoardManager.MAIN_STORE_NAME, "sample"));
		}
	}

	@Test
	public void testList() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			Assertions.assertEquals(0, blackBoardManager.len(BlackBoardManager.MAIN_STORE_NAME, "sample"));
			blackBoardManager.push(BlackBoardManager.MAIN_STORE_NAME, "sample", "a");
			blackBoardManager.push(BlackBoardManager.MAIN_STORE_NAME, "sample", "b");
			blackBoardManager.push(BlackBoardManager.MAIN_STORE_NAME, "sample", "c");
			Assertions.assertEquals(3, blackBoardManager.len(BlackBoardManager.MAIN_STORE_NAME, "sample"));
			Assertions.assertEquals("c", blackBoardManager.pop(BlackBoardManager.MAIN_STORE_NAME, "sample"));
			Assertions.assertEquals(2, blackBoardManager.len(BlackBoardManager.MAIN_STORE_NAME, "sample"));
			Assertions.assertEquals("b", blackBoardManager.peek(BlackBoardManager.MAIN_STORE_NAME, "sample"));
			Assertions.assertEquals(2, blackBoardManager.len(BlackBoardManager.MAIN_STORE_NAME, "sample"));
			blackBoardManager.push(BlackBoardManager.MAIN_STORE_NAME, "sample", "c");
			Assertions.assertEquals(3, blackBoardManager.len(BlackBoardManager.MAIN_STORE_NAME, "sample"));
			Assertions.assertEquals("a", blackBoardManager.get(BlackBoardManager.MAIN_STORE_NAME, "sample", 0));
			Assertions.assertEquals("b", blackBoardManager.get(BlackBoardManager.MAIN_STORE_NAME, "sample", 1));
			Assertions.assertEquals("c", blackBoardManager.get(BlackBoardManager.MAIN_STORE_NAME, "sample", 2));
			Assertions.assertEquals("c", blackBoardManager.get(BlackBoardManager.MAIN_STORE_NAME, "sample", -1));
			Assertions.assertEquals("b", blackBoardManager.get(BlackBoardManager.MAIN_STORE_NAME, "sample", -2));
			Assertions.assertEquals("a", blackBoardManager.get(BlackBoardManager.MAIN_STORE_NAME, "sample", -3));
			blackBoardManager.pop(BlackBoardManager.MAIN_STORE_NAME, "sample");
			blackBoardManager.pop(BlackBoardManager.MAIN_STORE_NAME, "sample");
			blackBoardManager.pop(BlackBoardManager.MAIN_STORE_NAME, "sample");
			blackBoardManager.pop(BlackBoardManager.MAIN_STORE_NAME, "sample");
			Assertions.assertEquals(0, blackBoardManager.len(BlackBoardManager.MAIN_STORE_NAME, "sample"));
		}
	}

}
