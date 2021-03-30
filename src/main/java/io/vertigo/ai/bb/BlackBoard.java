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
	 * The magic pattern * returns all the keys
	 * 
	 * @param keyPattern the pattern
	 * @return A list of keys
	 */
	Set<String> keys(final String keyPattern);

	/**
	 * Remove all the keys matching the pattern
	 * 
	 * The magic pattern * remove all the keys
	 * 
	 * @param keyPattern the pattern
	 */
	void remove(final String keyPattern);

	//------------------------------------
	//--- KV
	//------------------------------------
	String format(final String msg);

	//--- KV String 
	/**
	 * Returns the value or null if the key does not exist
	 * 
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
	/**
	 * Returns the value or null if the key does not exist
	 * 
	 * @param key the key
	 * @return the value mapped with the key or null if the key does not exist
	 */
	Integer getInteger(final String key);

	void putInteger(final String key, final Integer value);

	/**
	 * Increments the value (must be an integer) at the key
	 * 
	 * @param key the key
	 */
	void incr(final String key);

	/**
	 * Increments the value (must be an integer) at the key by a value
	 * 
	 * @param key the key
	 * @param value the value
	 */
	void incrBy(final String key, final int value);

	/**
	 * Decrements the value (must be an integer) at the key
	 * 
	 * @param key the key
	 */
	void decr(final String key);

	boolean lt(final String key, final Integer compare);

	boolean eq(final String key, final Integer compare);

	boolean gt(final String key, final Integer compare);

	//------------------------------------
	//- List                             
	//- All methods are prefixed with list  
	//------------------------------------
	/**
	 * Returns the size of the list identified by the key 
	 * 
	 * @param key the key
	 * @return the size of the list 
	 */
	int listSize(final String key);

	/**
	 * Pushes a value at the top of the list 
	 * 
	 * @param key the key
	 * @param value the value
	 */
	void listPush(final String key, final String value);

	/**
	 * Removes and returns the value at the top of the list 
	 * 
	 * @param key the key
	 * @param value the value
	 */
	String listPop(final String key);

	/**
	 * Returns the value at the top of the list 
	 * 
	 * @param key the key
	 * @param value the value
	 */
	String listPeek(final String key);

	/**
	 * Reads the value at the index of the list
	 * 
	 * @param key the key
	 * @param idx the index
	 * @return the value at the corresponding index 
	 */
	String listGet(final String key, final int idx);
}
