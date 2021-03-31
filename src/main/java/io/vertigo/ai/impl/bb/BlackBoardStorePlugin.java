package io.vertigo.ai.impl.bb;

import java.util.Set;

import io.vertigo.ai.bb.BlackBoard.Type;
import io.vertigo.core.node.component.Plugin;

public interface BlackBoardStorePlugin extends Plugin {

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
	 * Deletes all the keys matching the pattern
	 * 
	 * The magic pattern * remove all the keys
	 * 
	 * @param keyPattern the pattern
	 */
	void delete(final String keyPattern);

	/**
	 * Returns the key type or null if the keys doesn't exist
	 * 
	 * @param key the key
	 * @return the key type or null 
	 */
	Type getType(final String key);

	//------------------------------------
	//--- KV
	//------------------------------------
	/**
	 * Returns the value or null if the key does not exist
	 * @param key the key
	 * @return the value mapped with the key or null if the key does not exist
	 */
	String get(final String key);

	//--- KV String 
	/**
	 * Returns the value or null if the key does not exist
	 * @param key the key
	 * @return the value mapped with the key or null if the key does not exist
	 */
	String getString(final String key);

	void putString(final String key, final String value);

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
	 * Increments the value (must be an integer) at the key by a value
	 * 
	 * @param key the key
	 * @param value the value
	 */
	void incrBy(final String key, final int value);

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

	//------------------------------------
	//- Plugin                             -
	//------------------------------------

	String getStoreName();
}
