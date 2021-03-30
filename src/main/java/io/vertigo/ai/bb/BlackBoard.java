package io.vertigo.ai.bb;

import java.util.Set;

public interface BlackBoard {
	String KEY_REGEX = "[a-z]+(/[a-z0-9]*)*";
	String KEY_PATTERN_REGEX = "(" + KEY_REGEX + "[\\*]?)|[\\*]";

	//------------------------------------
	//--- Keys
	//------------------------------------
	/**
	 * Returns if the keys exists
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

	Set<String> keys();

	void removeAll();

	void remove(final String keyPattern);

	//------------------------------------
	//--- KV
	//------------------------------------
	String format(final String msg);

	//--- KV String 
	/**
	 * Returns the value or null if the key does not exist
	 * @param key the key
	 * @return the value mapped with the key or null if the key does not exist
	 */
	String getString(final String key);

	void putString(final String key, final String value);

	void append(final String key, final String something);

	boolean eq(final String key, final String compare);

	boolean eqCaseInsensitive(final String key, final String compare);

	boolean startsWith(final String key, final String compare);

	//--- KV Integer
	Integer getInteger(final String key);

	void putInteger(final String key, final Integer value);

	void decr(final String key);

	void incr(final String key);

	void incrBy(final String key, final int value);

	boolean lt(final String key, final Integer compare);

	boolean eq(final String key, final Integer compare);

	boolean gt(final String key, final Integer compare);

	//------------------------------------
	//- List                             
	//- All methods are prefixed with list  
	//------------------------------------

	int listLen(final String key);

	void listPush(final String key, final String value);

	String listPop(final String key);

	String listPeek(final String key);

	String listGet(final String key, final int idx);

}
