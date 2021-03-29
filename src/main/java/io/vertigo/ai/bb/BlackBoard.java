package io.vertigo.ai.bb;

import java.util.Set;

public interface BlackBoard {

	//------------------------------------
	//--- Keys
	//------------------------------------
	/**
	 * Returns if the keys exist
	 *
	 * @param key the key
	 * @return if the key exists
	 */
	public boolean exists(final String key);

	/**
	 * Returns all the keys matching the pattern
	 * @param keyPattern the pattern
	 * @return A list of keys
	 */
	public Set<String> keys(final String keyPattern);

	public Set<String> keys();

	public void removeAll();

	public void remove(final String keyPattern);

	//------------------------------------
	//--- KV
	//------------------------------------

	/**
	 * Returns the value or null if the key does not exist
	 * @param key the key
	 * @return the value mapped with the key or null if the key does not exist
	 */
	public String getString(final String key);

	public Integer getInteger(final String key);

	public void putInteger(final String key, final Integer value);

	public void putString(final String key, final String value);

	public String format(final String msg);

	public void append(final String key, final String something);

	public void decr(final String key);

	public void incr(final String key);

	public void incrBy(final String key, final int value);

	//String
	public boolean eq(final String key, final String compare);

	public boolean eqCaseInsensitive(final String key, final String compare);

	public boolean startsWith(final String key, final String compare);

	//Integer
	public boolean lt(final String key, final Integer compare);

	public boolean eq(final String key, final Integer compare);

	public boolean gt(final String key, final Integer compare);

	//------------------------------------
	//- List                             -
	//- All methods are prefixed with list  -
	//------------------------------------

	public int listLen(final String key);

	public void listPush(final String key, final String value);

	public String listPop(final String key);

	public String listPeek(final String key);

	public String listGet(final String key, final int idx);

}
