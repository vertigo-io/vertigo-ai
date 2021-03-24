package io.vertigo.ai.impl.bb;

import java.util.Set;

import io.vertigo.ai.impl.bb.BlackBoardManagerImpl.Type;
import io.vertigo.core.node.component.Plugin;

public interface BlackBoardStorePlugin extends Plugin {

	//------------------------------------
	//--- Keys
	//------------------------------------
	/**
	 * Returns if the keys exist
	 *
	 * @param key the key
	 * @return if the key exists
	 */
	boolean exists(final String key);

	/**
	 * Returns all the keys matching the pattern
	 * @param keyPattern the pattern
	 * @return A list of keys
	 */
	Set<String> keys(final String keyPattern);

	void remove(final String keyPattern);

	//------------------------------------
	//--- KV
	//------------------------------------

	/**
	 * Returns the value or null if the key does not exist
	 * @param key the key
	 * @return the value mapped with the key or null if the key does not exist
	 */
	String get(final String key);

	void put(final String key, final Type type, final String value);

	void incrBy(final String key, final int value);

	Type getType(final String key);

	//------------------------------------
	//- List                             -
	//- All methods are prefixed with l  -
	//------------------------------------

	int len(final String key);

	void push(final String key, final String value);

	String pop(final String key);

	String peek(final String key);

	String get(final String key, final int idx);

	//------------------------------------
	//- Plugin                             -
	//------------------------------------

	String getStoreName();
}
