package io.vertigo.ai.impl.bb;

import java.util.Optional;
import java.util.Set;

import io.vertigo.ai.bb.BlackBoard;
import io.vertigo.ai.impl.bb.BlackBoardManagerImpl.Type;
import io.vertigo.core.lang.Assertion;

public final class BlackBoardImpl implements BlackBoard {
	private final BlackBoardStorePlugin blackBoardStorePlugin;

	BlackBoardImpl(
			final BlackBoardStorePlugin blackBoardStorePlugin) {
		Assertion.check()
				.isNotNull(blackBoardStorePlugin);
		// ---
		this.blackBoardStorePlugin = blackBoardStorePlugin;
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
	public boolean exists(final String key) {
		checkKey(key);
		//---
		return blackBoardStorePlugin
				.exists(key);
	}

	/**
	 * Returns all the keys matching the pattern
	 * @param keyPattern the pattern
	 * @return A list of keys
	 */
	@Override
	public Set<String> keys(final String keyPattern) {
		checkKeyPattern(keyPattern);
		//---
		return blackBoardStorePlugin
				.keys(keyPattern);
	}

	@Override
	public Set<String> keys() {
		return keys("*");
	}

	@Override
	public void removeAll() {
		remove("*");
	}

	@Override
	public void remove(final String keyPattern) {
		checkKeyPattern(keyPattern);
		blackBoardStorePlugin
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
	public String getString(final String key) {
		checkKey(key);
		//---
		return blackBoardStorePlugin
				.getString(key);
	}

	@Override
	public Integer getInteger(final String key) {
		checkKey(key);
		checkType(key, Type.Integer);
		//---
		return blackBoardStorePlugin
				.getInteger(key);
	}

	@Override
	public void putInteger(final String key, final Integer value) {
		checkKey(key);
		checkType(key, Type.Integer);
		//---
		blackBoardStorePlugin
				.putInteger(key, value);
	}

	@Override
	public void putString(final String key, final String value) {
		checkKey(key);
		checkType(key, Type.String);
		//---
		blackBoardStorePlugin
				.putString(key, value);
	}

	@Override
	public String format(final String msg) {
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
			final var paramVal = Optional.ofNullable(blackBoardStorePlugin.get(paramName))
					.orElse("not found:" + paramName);
			builder.replace(start, end + END_TOKEN.length(), paramVal);
		}
		if (builder.indexOf(START_TOKEN) > 0) {
			throw new IllegalStateException("A start token '" + START_TOKEN + "+'has been found without an end token " + END_TOKEN);
		}
		return builder.toString();
	}

	@Override
	public void append(final String key, final String something) {
		String value = getString(key);
		if (value == null) {
			value = "";
		}
		putString(key, value + something);
	}

	@Override
	public void decr(final String key) {
		incrBy(key, -1);
	}

	@Override
	public void incr(final String key) {
		incrBy(key, 1);
	}

	@Override
	public void incrBy(final String key, final int value) {
		checkKey(key);
		checkType(key, Type.Integer);
		//---
		blackBoardStorePlugin.incrBy(key, value);
	}

	//Integers
	@Override
	public boolean lt(final String key, final Integer compare) {
		return compareInteger(key, compare) < 0;
	}

	@Override
	public boolean eq(final String key, final Integer compare) {
		return compareInteger(key, compare) == 0;
	}

	@Override
	public boolean gt(final String key, final Integer compare) {
		return compareInteger(key, compare) > 0;
	}

	private int compareInteger(final String key, final Integer compare) {
		checkKey(key);
		checkType(key, Type.Integer);
		//---
		final Integer k = getInteger(key);
		if (k == null) {
			return compare == null
					? 0
					: -1;
		}
		if (compare == null) {
			return k == null
					? 0
					: -1;
		}
		return k.compareTo(compare);
	}

	//String
	@Override
	public boolean eq(final String key, final String compare) {
		checkKey(key);
		checkType(key, Type.String);
		//---
		final String k = getString(key);
		return k == null ? compare == null : k.equals(compare);
	}

	@Override
	public boolean eqCaseInsensitive(final String key, final String compare) {
		checkKey(key);
		checkType(key, Type.String);
		//---
		final String k = getString(key);
		return k == null ? compare == null : k.equalsIgnoreCase(compare);
	}

	@Override
	public boolean startsWith(final String key, final String compare) {
		checkKey(key);
		checkType(key, Type.String);
		//---
		final String k = getString(key);
		return k == null ? compare == null : k.startsWith(compare);
	}

	//------------------------------------
	//- List                             -
	//- All methods are prefixed with l  -
	//------------------------------------

	@Override
	public int listLen(final String key) {
		return blackBoardStorePlugin
				.listLen(key);
	}

	@Override
	public void listPush(final String key, final String value) {
		blackBoardStorePlugin
				.listPush(key, value);
	}

	@Override
	public String listPop(final String key) {
		return blackBoardStorePlugin
				.listPop(key);
	}

	@Override
	public String listPeek(final String key) {
		return blackBoardStorePlugin
				.listPeek(key);
	}

	@Override
	public String listGet(final String key, final int idx) {
		return blackBoardStorePlugin
				.listGet(key, idx);
	}

	//------------------------------------
	//- Utils                             -
	//------------------------------------

	private static void checkKey(final String key) {
		Assertion.check()
				.isNotBlank(key)
				.isTrue(key.matches(KEY_REGEX), "the key '{0}' must contain only a-z 1-9 words separated with /", key);
	}

	private void checkType(final String key, final Type type) {
		Assertion.check()
				.isNotNull(key)
				.isNotNull(type);
		//---
		final Type t = blackBoardStorePlugin.getType(key);
		if (t != null && !type.equals(t)) {
			throw new IllegalStateException("the type of the key " + t + " is not the one expected " + type);
		}
	}

	private static void checkKeyPattern(final String keyPattern) {
		Assertion.check()
				.isNotBlank(keyPattern)
				.isTrue(keyPattern.matches(KEY_PATTERN_REGEX), "the key pattern '{0}' must contain only a-z 1-9 words separated with / and is finished by a * or nothing", keyPattern);
	}

}
