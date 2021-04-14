package io.vertigo.ai.plugins.bb.memory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.ai.bb.BBKey;
import io.vertigo.ai.bb.BlackBoard.Type;
import io.vertigo.ai.bb.BlackBoardManager;
import io.vertigo.ai.bb.KeyPattern;
import io.vertigo.ai.impl.bb.BlackBoardStorePlugin;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.param.ParamValue;

public final class MemoryBlackBoardStorePlugin implements BlackBoardStorePlugin {
	private final Map<String, Type> keys = Collections.synchronizedMap(new LinkedHashMap<>());
	private final Map<String, Object> values = Collections.synchronizedMap(new LinkedHashMap<>());
	private final Map<String, BBList> lists = Collections.synchronizedMap(new LinkedHashMap<>());

	private final Optional<String> storeNameOpt;

	@Inject
	public MemoryBlackBoardStorePlugin(final @ParamValue("storeName") Optional<String> storeNameOpt) {
		Assertion.check()
				.isNotNull(storeNameOpt);
		// ---
		this.storeNameOpt = storeNameOpt;
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
	public boolean exists(final BBKey key) {
		Assertion.check().isNotNull(key);
		// ---
		return keys.containsKey(key.getKey());
	}

	/**
	 * Returns all the keys matching the pattern
	 * @param keyPattern the pattern
	 * @return A list of keys
	 */
	@Override
	public Set<BBKey> keys(final KeyPattern keyPattern) {
		Assertion.check().isNotNull(keyPattern);
		final var keyPatternString = keyPattern.getKeyPattern();
		//---
		if ("*".equals(keyPatternString)) {
			return keys();
		}
		if (keyPatternString.endsWith("*")) {
			final var prefix = keyPatternString.replaceAll("\\*", "");
			return keys.keySet().stream()
					.map(BBKey::of)
					.filter(s -> s.startsWith(prefix))
					.collect(Collectors.toSet());
		}
		final var key = keyPatternString;
		return keys.containsKey(key)
				? Set.of(BBKey.of(key))
				: Collections.emptySet();
	}

	private Set<BBKey> keys() {
		return keys.keySet().stream().map(BBKey::of).collect(Collectors.toSet());
	}

	@Override
	public void delete(final KeyPattern keyPattern) {
		Assertion.check().isNotNull(keyPattern);
		final var keyPatternString = keyPattern.getKeyPattern();
		if ("*".equals(keyPatternString)) {
			values.clear();
			keys.clear();
			lists.clear();
		} else if (keyPatternString.endsWith("*")) {
			final var prefix = keyPatternString.replaceAll("\\*", "");
			values.keySet().removeIf(s -> s.startsWith(prefix));
			lists.keySet().removeIf(s -> s.startsWith(prefix));
			keys.keySet().removeIf(s -> s.startsWith(prefix));
		} else {
			final var key = keyPatternString;
			values.remove(key);
			lists.remove(key);
			keys.remove(key);
		}
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
	public String get(final BBKey key) {
		return String.valueOf(values.get(key.getKey()));
	}

	/**
	 * Returns the value or null if the key does not exist
	 * @param key the key
	 * @return the value mapped with the key or null if the key does not exist
	 */
	@Override
	public String getString(final BBKey key) {
		Assertion.check().isNotNull(key);
		// ---
		return (String) values.get(key.getKey());
	}

	@Override
	public Integer getInteger(final BBKey key) {
		Assertion.check().isNotNull(key);
		// ---
		return (Integer) values.get(key.getKey());
	}

	@Override
	public void putString(final BBKey key, final String value) {
		doPut(key, Type.String, value);
	}

	@Override
	public void putInteger(final BBKey key, final Integer value) {
		doPut(key, Type.Integer, value);
	}

	private void doPut(final BBKey key, final Type type, final Object value) {
		Assertion.check()
				.isNotNull(key)
				.isNotNull(type);
		// ---
		//---
		final Type previousType = keys.put(key.getKey(), type);
		if (previousType != null && type != previousType) {
			throw new IllegalStateException("the type is already defined" + previousType);
		}
		values.put(key.getKey(), value);
	}

	@Override
	public void incrBy(final BBKey key, final int value) {
		Assertion.check()
				.isNotNull(key);
		//---
		Integer i = getInteger(key);
		if (i == null) {
			i = 0;
		}
		putInteger(key, i + value);
	}

	@Override
	public Type getType(final BBKey key) {
		return keys.get(key.getKey());
	}

	//------------------------------------
	//- List                             -
	//- All methods are prefixed with l  -
	//------------------------------------
	private BBList getListOrCreate(final BBKey key) {
		Assertion.check()
				.isNotNull(key);
		//---
		BBList list = lists.get(key.getKey());
		if (list != null) {
			return list;
		}
		list = new BBList();
		lists.put(key.getKey(), list);
		return list;
	}

	private BBList getListOrEmpty(final BBKey key) {
		Assertion.check()
				.isNotNull(key);
		//---
		final BBList list = lists.get(key.getKey());
		return list == null
				? BBList.EMPTY
				: list;
	}

	@Override
	public int listSize(final BBKey key) {
		return getListOrEmpty(key)
				.size();
	}

	@Override
	public void listPush(final BBKey key, final String value) {
		getListOrCreate(key)
				.push(value);
	}

	@Override
	public String listPop(final BBKey key) {
		return getListOrEmpty(key)
				.pop();
	}

	@Override
	public String listPeek(final BBKey key) {
		return getListOrEmpty(key)
				.peek();
	}

	@Override
	public String listGet(final BBKey key, final int idx) {
		return getListOrEmpty(key)
				.get(idx);
	}

	@Override
	public String getStoreName() {
		return storeNameOpt.orElse(BlackBoardManager.MAIN_STORE_NAME);
	}
}
