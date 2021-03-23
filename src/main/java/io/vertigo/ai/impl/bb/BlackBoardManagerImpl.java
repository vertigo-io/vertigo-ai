package io.vertigo.ai.impl.bb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import io.vertigo.ai.bb.BlackBoardManager;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.core.lang.Assertion;

public final class BlackBoardManagerImpl implements BlackBoardManager {

	private final Map<String, BlackBoardStorePlugin> blackBoardPluginByStore = new HashMap<>();
	//private final ThreadLocal<String> connectedStore = new ThreadLocal<>();
	private final VTransactionManager transactionManager;

	@Inject
	public BlackBoardManagerImpl(
			final VTransactionManager transactionManager,
			final List<BlackBoardStorePlugin> blackBoardStorePlugins) {
		Assertion.check()
				.isNotNull(transactionManager)
				.isNotNull(blackBoardStorePlugins);
		// ---
		this.transactionManager = transactionManager;
		blackBoardStorePlugins.forEach(
				plugin -> {
					final var storeName = plugin.getStoreName();
					Assertion.check().isFalse(blackBoardPluginByStore.containsKey(storeName), "BlackBoard Store '{0}' already registered ", storeName);
					//---
					blackBoardPluginByStore.put(storeName, plugin);
				});
	}

	//------------------------------------
	//--- Keys
	//------------------------------------
	/**
	 * Returns if the keys exist
	 *
	 * @param key the key
	 * @return if the key exists
	 */
	@Override
	public boolean exists(final String storeName, final String key) {
		checkKey(key);
		//---
		return getPlugin(storeName)
				.exists(key);
	}

	/**
	 * Returns all the keys matching the pattern
	 * @param keyPattern the pattern
	 * @return A list of keys
	 */
	@Override
	public Set<String> keys(final String storeName, final String keyPattern) {
		checkKeyPattern(keyPattern);
		//---
		return getPlugin(storeName)
				.keys(keyPattern);
	}

	@Override
	public Set<String> keys(final String storeName) {
		return keys(storeName, "*");
	}

	@Override
	public void removeAll(final String storeName) {
		remove(storeName, "*");
	}

	@Override
	public void remove(final String storeName, final String keyPattern) {
		checkKeyPattern(keyPattern);
		getPlugin(storeName)
				.remove(keyPattern);
	}

	//------------------------------------
	//--- KV
	//------------------------------------

	/**
	 * Returns the value or null if the key does not exist
	 * @param key the key
	 * @return the value mapped with the key or null if the key does not exist
	 */
	@Override
	public String get(final String storeName, final String key) {
		checkKey(key);
		//---
		return getPlugin(storeName)
				.get(key);
	}

	@Override
	public Integer getInteger(final String storeName, final String key) {
		final String value = get(storeName, key);
		checkType(storeName, key, Type.Integer);
		return formatToInteger(value);
	}

	@Override
	public void putInteger(final String storeName, final String key, final int value) {
		doPut(storeName, key, Type.Integer, formatToString(value));
	}

	@Override
	public void put(final String storeName, final String key, final String value) {
		doPut(storeName, key, Type.String, value);
	}

	private void doPut(final String storeName, final String key, final Type type, final String value) {
		checkKey(key);
		checkType(storeName, key, type);
		//---

		getPlugin(storeName)
				.put(key, type, value);
	}

	@Override
	public String format(final String storeName, final String msg) {
		Assertion.check()
				.isNotNull(msg);
		//---
		final String START_TOKEN = "{{";
		final String END_TOKEN = "}}";

		final StringBuilder builder = new StringBuilder(msg);
		int start = 0;
		int end;
		while ((end = builder.indexOf(END_TOKEN, start)) >= 0) {
			start = builder.lastIndexOf(START_TOKEN, end);
			if (start < 0) {
				throw new IllegalStateException("An end token '" + END_TOKEN + "+'has been found without a start token " + START_TOKEN);
			}
			final var paramName = builder.substring(start + START_TOKEN.length(), end);
			final var paramVal = Optional.ofNullable(getPlugin(storeName).get(paramName))
					.orElse("not found:" + paramName);
			builder.replace(start, end + END_TOKEN.length(), paramVal);
		}
		if (builder.indexOf(START_TOKEN) > 0) {
			throw new IllegalStateException("A start token '" + START_TOKEN + "+'has been found without an end token " + END_TOKEN);
		}
		return builder.toString();
	}

	@Override
	public void append(final String storeName, final String key, final String something) {
		String value = get(storeName, key);
		if (value == null) {
			value = "";
		}
		put(storeName, key, value + something);
	}

	@Override
	public void decr(final String storeName, final String key) {
		incrBy(storeName, key, -1);
	}

	@Override
	public void incr(final String storeName, final String key) {
		incrBy(storeName, key, 1);
	}

	@Override
	public void incrBy(final String storeName, final String key, final int value) {
		checkKey(key);
		checkType(storeName, key, Type.Integer);
		//---
		getPlugin(storeName).incrBy(key, value);
	}

	private int compare(final String storeName, final String key, final String compare) {
		checkKey(key);
		//---
		final Type type = getPlugin(storeName).getType(key);
		final String k = get(storeName, key);
		final String c = format(storeName, compare);
		if (k == null) {
			return c == null
					? 0
					: -1;
		}

		switch (type) {
			case String:
				return k.compareTo(c);
			case Integer:
				return Integer.valueOf(k).compareTo(Integer.valueOf(c));
			default:
				throw new IllegalStateException();
		}
	}

	@Override
	public boolean lt(final String storeName, final String key, final String compare) {
		return compare(storeName, key, compare) < 0;
	}

	@Override
	public boolean eq(final String storeName, final String key, final String compare) {
		return compare(storeName, key, compare) == 0;
	}

	@Override
	public boolean gt(final String storeName, final String key, final String compare) {
		return compare(storeName, key, compare) > 0;
	}

	//------------------------------------
	//- List                             -
	//- All methods are prefixed with l  -
	//------------------------------------

	@Override
	public int len(final String storeName, final String key) {
		return getPlugin(storeName)
				.len(key);
	}

	@Override
	public void push(final String storeName, final String key, final String value) {
		getPlugin(storeName)
				.push(key, value);
	}

	@Override
	public String pop(final String storeName, final String key) {
		return getPlugin(storeName)
				.pop(key);
	}

	@Override
	public String peek(final String storeName, final String key) {
		return getPlugin(storeName)
				.peek(key);
	}

	@Override
	public String get(final String storeName, final String key, final int idx) {
		return getPlugin(storeName)
				.get(key, idx);
	}

	//------------------------------------
	//- Utils                             -
	//------------------------------------

	private BlackBoardStorePlugin getPlugin(final String storeName) {
		// ---
		Assertion.check()
				.isTrue(blackBoardPluginByStore.containsKey(storeName), " Store with name '{0}' doesn't exists", storeName);
		return blackBoardPluginByStore.get(storeName);
	}

	private static void checkKey(final String key) {
		Assertion.check()
				.isNotBlank(key)
				.isTrue(key.matches(KEY_REGEX), "the key '{0}' must contain only a-z 1-9 words separated with /", key);
	}

	private void checkType(final String storeName, final String key, final Type type) {
		Assertion.check()
				.isNotNull(key)
				.isNotNull(type);
		//---
		final Type t = getPlugin(storeName).getType(key);
		if (t != null && !type.equals(t)) {
			throw new IllegalStateException("the type of the key " + t + " is not the one expected " + type);
		}
	}

	private static void checkKeyPattern(final String keyPattern) {
		Assertion.check()
				.isNotBlank(keyPattern)
				.isTrue(keyPattern.matches(KEY_PATTERN_REGEX), "the key pattern '{0}' must contain only a-z 1-9 words separated with / and is finished by a * or nothing", keyPattern);
	}

	private static String formatToString(final Integer i) {
		return i == null
				? null
				: String.valueOf(i);
	}

	private static Integer formatToInteger(final String s) {
		return s == null
				? null
				: Integer.valueOf(s);
	}

}
