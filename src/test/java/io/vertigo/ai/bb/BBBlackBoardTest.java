package io.vertigo.ai.bb;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.ai.AiFeatures;
import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.di.DIInjector;
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
						new AiFeatures()
								.withBlackboard()
								.withMemoryBlackboard()
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
		Assertions.assertFalse(blackBoard.exists(BBKey.of("samplekey")));
		blackBoard.incr(BBKey.of("samplekey"));
		Assertions.assertTrue(blackBoard.exists(BBKey.of("samplekey")));
		//--only some characters are accepted ; blanks are not permitted
		Assertions.assertThrows(Exception.class, () -> blackBoard.exists(BBKey.of("sample key")));
	}

	@Test
	public void testKeys() {
		final BlackBoard blackBoard = blackBoardManager.connect();
		//---
		Assertions.assertEquals(0, blackBoard.keys(KeyPattern.of("test")).size());
		Assertions.assertEquals(0, blackBoard.keys(KeyPattern.of("test/*")).size());
		Assertions.assertEquals(0, blackBoard.keys(KeyPattern.of("*")).size());
		//---
		blackBoard.incr(BBKey.of("test"));
		Assertions.assertEquals(1, blackBoard.keys(KeyPattern.of("test")).size());
		Assertions.assertEquals(0, blackBoard.keys(KeyPattern.of("test/*")).size());
		Assertions.assertEquals(1, blackBoard.keys(KeyPattern.of("*")).size());
		//---
		blackBoard.delete(KeyPattern.of("*"));
		blackBoard.incr(BBKey.of("test"));
		blackBoard.incr(BBKey.of("test/1"));
		blackBoard.incr(BBKey.of("test/2"));
		Assertions.assertEquals(1, blackBoard.keys(KeyPattern.of("test")).size());
		Assertions.assertEquals(2, blackBoard.keys(KeyPattern.of("test/*")).size());
		Assertions.assertEquals(3, blackBoard.keys(KeyPattern.of("*")).size());
		//--check the key pattern
		blackBoard.delete(KeyPattern.of("*"));
		Assertions.assertThrows(Exception.class,
				() -> blackBoard.keys(KeyPattern.of(" sample")));
		Assertions.assertThrows(Exception.class,
				() -> blackBoard.keys(KeyPattern.of("sample**")));
		Assertions.assertThrows(Exception.class,
				() -> blackBoard.keys(KeyPattern.of("sample key")));
		Assertions.assertThrows(Exception.class,
				() -> blackBoard.keys(KeyPattern.of("/samplekey")).isEmpty());
		Assertions.assertThrows(Exception.class,
				() -> blackBoard.keys(KeyPattern.of("samplekey/*/test")).isEmpty());
		//--- keys and keys("*")
		blackBoard.delete(KeyPattern.of("*"));
		blackBoard.incr(BBKey.of("test"));
		blackBoard.incr(BBKey.of("test/1"));
		blackBoard.incr(BBKey.of("test/3"));
		blackBoard.incr(BBKey.of("test/4"));
		Assertions.assertEquals(4, blackBoard.keys(KeyPattern.of("*")).size());
		Assertions.assertEquals(4, blackBoard.keys(KeyPattern.of("*")).size());
	}

	@Test
	public void testGetPut() {
		final BlackBoard blackBoard = blackBoardManager.connect();
		//---
		Assertions.assertEquals(null, blackBoard.getString(BBKey.of("sample/key")));
		blackBoard.putString(BBKey.of("sample/key"), "test");
		Assertions.assertEquals("test", blackBoard.getString(BBKey.of("sample/key")));
		blackBoard.putString(BBKey.of("sample/key"), "test2");
		Assertions.assertEquals("test2", blackBoard.getString(BBKey.of("sample/key")));
	}

	@Test
	public void testFormat() {
		final BlackBoard blackBoard = blackBoardManager.connect();
		//---
		Assertions.assertEquals("", blackBoard.format(KeyTemplate.of("")));
		Assertions.assertEquals("hello", blackBoard.format(KeyTemplate.of("hello")));
		blackBoard.putString(BBKey.of("sample/key"), "test");
		Assertions.assertEquals("hello test", blackBoard.format(KeyTemplate.of("hello {{sample/key}}")));
	}

	@Test
	public void testInc() {
		final BlackBoard blackBoard = blackBoardManager.connect();
		//---
		final BBKey key = BBKey.of("key");
		Assertions.assertEquals(null, blackBoard.getInteger(key));
		blackBoard.incr(key);
		Assertions.assertEquals(1, blackBoard.getInteger(key));
		blackBoard.incr(key);
		Assertions.assertEquals(2, blackBoard.getInteger(key));
		blackBoard.incrBy(key, 10);
		Assertions.assertEquals(12, blackBoard.getInteger(key));
	}

	@Test
	public void testDec() {
		final BlackBoard blackBoard = blackBoardManager.connect();
		//---
		final BBKey key = BBKey.of("key");
		Assertions.assertEquals(null, blackBoard.getInteger(key));
		blackBoard.incrBy(key, 10);
		Assertions.assertEquals(10, blackBoard.getInteger(key));
		blackBoard.decr(key);
		Assertions.assertEquals(9, blackBoard.getInteger(key));
		blackBoard.decr(key);
		Assertions.assertEquals(8, blackBoard.getInteger(key));
		blackBoard.incrBy(key, -5);
		Assertions.assertEquals(3, blackBoard.getInteger(key));
	}

	@Test
	public void testRemove() {
		final BlackBoard blackBoard = blackBoardManager.connect();
		//---
		Assertions.assertEquals(0, blackBoard.keys(KeyPattern.of("*")).size());
		blackBoard.incr(BBKey.of("sample/1"));
		blackBoard.incr(BBKey.of("sample/2"));
		blackBoard.incr(BBKey.of("sample/3"));
		blackBoard.incr(BBKey.of("sample/4"));
		Assertions.assertEquals(4, blackBoard.keys(KeyPattern.of("*")).size());
		blackBoard.delete(KeyPattern.of("sample/1"));
		Assertions.assertEquals(3, blackBoard.keys(KeyPattern.of("*")).size());
		blackBoard.delete(KeyPattern.of("*"));
		Assertions.assertEquals(0, blackBoard.keys(KeyPattern.of("*")).size());
		blackBoard.incr(BBKey.of("sample/1"));
		blackBoard.incr(BBKey.of("sample/2"));
		blackBoard.incr(BBKey.of("sample/3"));
		blackBoard.incr(BBKey.of("sample/4"));
		blackBoard.delete(KeyPattern.of("*"));
		Assertions.assertEquals(0, blackBoard.keys(KeyPattern.of("*")).size());
	}

	@Test
	public void testInteger() {
		final BlackBoard blackBoard = blackBoardManager.connect();
		//---
		final BBKey sampleKey = BBKey.of("sample");
		Assertions.assertEquals(null, blackBoard.getInteger(sampleKey));
		blackBoard.incr(sampleKey);
		Assertions.assertEquals(1, blackBoard.getInteger(sampleKey));
		blackBoard.incr(sampleKey);
		Assertions.assertEquals(2, blackBoard.getInteger(sampleKey));
		blackBoard.putInteger(sampleKey, 56);
		Assertions.assertEquals(56, blackBoard.getInteger(sampleKey));
		Assertions.assertEquals(false, blackBoard.lt(sampleKey, 50));
		Assertions.assertEquals(true, blackBoard.gt(sampleKey, 50));
		Assertions.assertEquals(false, blackBoard.eq(sampleKey, 50));
		Assertions.assertEquals(true, blackBoard.eq(sampleKey, 56));
		blackBoard.putInteger(sampleKey, -55);
		Assertions.assertEquals(-55, blackBoard.getInteger(sampleKey));
		blackBoard.incrBy(sampleKey, 100);
		Assertions.assertEquals(45, blackBoard.getInteger(sampleKey));
	}

	@Test
	public void testString() {
		final BlackBoard blackBoard = blackBoardManager.connect();
		//---
		final BBKey sampleKey = BBKey.of("sample");
		Assertions.assertEquals(null, blackBoard.getString(sampleKey));
		blackBoard.putString(sampleKey, "test");
		Assertions.assertEquals("test", blackBoard.getString(sampleKey));
		blackBoard.delete(KeyPattern.of("*"));
		blackBoard.append(sampleKey, "hello");
		blackBoard.append(sampleKey, " ");
		blackBoard.append(sampleKey, "world");
		Assertions.assertEquals("hello world", blackBoard.getString(sampleKey));
	}

	@Test
	public void testList() {
		final BlackBoard blackBoard = blackBoardManager.connect();
		//---
		final BBKey sampleKey = BBKey.of("sample");
		Assertions.assertEquals(0, blackBoard.listSize(sampleKey));
		blackBoard.listPush(sampleKey, "a");
		blackBoard.listPush(sampleKey, "b");
		blackBoard.listPush(sampleKey, "c");
		Assertions.assertEquals(3, blackBoard.listSize(sampleKey));
		Assertions.assertEquals("c", blackBoard.listPop(sampleKey));
		Assertions.assertEquals(2, blackBoard.listSize(sampleKey));
		Assertions.assertEquals("b", blackBoard.listPeek(sampleKey));
		Assertions.assertEquals(2, blackBoard.listSize(sampleKey));
		blackBoard.listPush(sampleKey, "c");
		Assertions.assertEquals(3, blackBoard.listSize(sampleKey));
		Assertions.assertEquals("a", blackBoard.listGet(sampleKey, 0));
		Assertions.assertEquals("b", blackBoard.listGet(sampleKey, 1));
		Assertions.assertEquals("c", blackBoard.listGet(sampleKey, 2));
		Assertions.assertEquals("c", blackBoard.listGet(sampleKey, -1));
		Assertions.assertEquals("b", blackBoard.listGet(sampleKey, -2));
		Assertions.assertEquals("a", blackBoard.listGet(sampleKey, -3));
		blackBoard.listPop(sampleKey);
		blackBoard.listPop(sampleKey);
		blackBoard.listPop(sampleKey);
		blackBoard.listPop(sampleKey);
		Assertions.assertEquals(0, blackBoard.listSize(sampleKey));
	}

}
