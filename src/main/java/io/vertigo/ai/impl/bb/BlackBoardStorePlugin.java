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

	/**
	 * Returns the value or null if the key does not exist
	 * @param key the key
	 * @return the value mapped with the key or null if the key does not exist
	 */
	String getString(final String key);

	void putString(final String key, final String value);

	Integer getInteger(final String key);

	void putInteger(final String key, final Integer value);

	void incrBy(final String key, final int value);

	Type getType(final String key);

	//------------------------------------
	//- List                             -
	//- All methods are prefixed with l  -
	//------------------------------------

	int listLen(final String key);

	void listPush(final String key, final String value);

	String listPop(final String key);

	String listPeek(final String key);

	String listGet(final String key, final int idx);

	//------------------------------------
	//- Plugin                             -
	//------------------------------------

	String getStoreName();
}
