package io.vertigo.ai.bb;

import java.util.Set;

import io.vertigo.core.node.component.Component;

public interface BlackBoardManager extends Component {

	public static final String KEY_REGEX = "[a-z]+(/[a-z0-9]*)*";
	public static final String KEY_PATTERN_REGEX = "(" + KEY_REGEX + "[\\*]?)|[\\*]";
	public static final String MAIN_STORE_NAME = "main";

	public enum Type {
		String, Integer, List
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
	public boolean exists(final String storeName, final String key);

	/**
	 * Returns all the keys matching the pattern
	 * @param keyPattern the pattern
	 * @return A list of keys
	 */
	public Set<String> keys(final String storeName, final String keyPattern);

	public Set<String> keys(final String storeName);

	public void removeAll(final String storeName);

	public void remove(final String storeName, final String keyPattern);

	//------------------------------------
	//--- KV
	//------------------------------------

	/**
	 * Returns the value or null if the key does not exist
	 * @param key the key
	 * @return the value mapped with the key or null if the key does not exist
	 */
	public String get(final String storeName, final String key);

	public Integer getInteger(final String storeName, final String key);

	public void putInteger(final String storeName, final String key, final int value);

	public void put(final String storeName, final String key, final String value);

	public String format(final String storeName, final String msg);

	public void append(final String storeName, final String key, final String something);

	public void decr(final String storeName, final String key);

	public void incr(final String storeName, final String key);

	public void incrBy(final String storeName, final String key, final int value);

	public boolean lt(final String storeName, final String key, final String compare);

	public boolean eq(final String storeName, final String key, final String compare);

	public boolean gt(final String storeName, final String key, final String compare);

	//------------------------------------
	//- List                             -
	//- All methods are prefixed with l  -
	//------------------------------------

	public int len(final String storeName, final String key);

	public void push(final String storeName, final String key, final String value);

	public String pop(final String storeName, final String key);

	public String peek(final String storeName, final String key);

	public String get(final String storeName, final String key, final int idx);

}
