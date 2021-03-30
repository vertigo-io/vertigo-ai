package io.vertigo.ai.plugins.bb.memory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.ai.bb.BlackBoard.Type;
import io.vertigo.ai.bb.BlackBoardManager;
import io.vertigo.ai.impl.bb.BlackBoardStorePlugin;
import io.vertigo.commons.transaction.VTransaction;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionResourceId;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.param.ParamValue;

public final class MemoryBlackBoardStorePlugin implements BlackBoardStorePlugin {

	private final Map<String, Type> keys = Collections.synchronizedMap(new LinkedHashMap<>());
	private final Map<String, Object> values = Collections.synchronizedMap(new LinkedHashMap<>());
	private final Map<String, BBList> lists = Collections.synchronizedMap(new LinkedHashMap<>());

	private final Optional<String> storeNameOpt;
	private final VTransactionManager transactionManager;

	/**
	 * Identifiant de ressource SQL par défaut.
	 */
	public static final VTransactionResourceId<BBConnection> MEMORY_BB_MAIN_RESOURCE_ID = new VTransactionResourceId<>(VTransactionResourceId.Priority.NORMAL, "Memory-BlackBoard-main");

	@Inject
	public MemoryBlackBoardStorePlugin(
			final VTransactionManager transactionManager,
			final @ParamValue("storeName") Optional<String> storeNameOpt) {
		Assertion.check()
				.isNotNull(transactionManager)
				.isNotNull(storeNameOpt);
		// ---
		this.transactionManager = transactionManager;
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
	public boolean exists(final String key) {
		Assertion.check().isNotNull(key);
		// ---
		return keys.containsKey(key);
	}

	/**
	 * Returns all the keys matching the pattern
	 * @param keyPattern the pattern
	 * @return A list of keys
	 */
	@Override
	public Set<String> keys(final String keyPattern) {
		Assertion.check().isNotNull(keyPattern);
		//---
		if ("*".equals(keyPattern)) {
			return keys();
		}
		if (keyPattern.endsWith("*")) {
			final var prefix = keyPattern.replaceAll("\\*", "");
			return keys.keySet().stream()
					.filter(s -> s.startsWith(prefix))
					.collect(Collectors.toSet());
		}
		final var key = keyPattern;
		return keys.containsKey(key)
				? Set.of(key)
				: Collections.emptySet();
	}

	private Set<String> keys() {
		return keys.keySet();
	}

	private void removeAll() {
		values.clear();
		keys.clear();
		lists.clear();
	}

	@Override
	public void remove(final String keyPattern) {
		Assertion.check().isNotNull(keyPattern);
		if ("*".equals(keyPattern)) {
			removeAll();
		} else if (keyPattern.endsWith("*")) {
			final var prefix = keyPattern.replaceAll("\\*", "");
			values.keySet().removeIf(s -> s.startsWith(prefix));
			lists.keySet().removeIf(s -> s.startsWith(prefix));
			keys.keySet().removeIf(s -> s.startsWith(prefix));
		} else {
			final var key = keyPattern;
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
	public String get(final String key) {
		return String.valueOf(values.get(key));
	}

	/**
	 * Returns the value or null if the key does not exist
	 * @param key the key
	 * @return the value mapped with the key or null if the key does not exist
	 */
	@Override
	public String getString(final String key) {
		Assertion.check().isNotNull(key);
		// ---
		return (String) values.get(key);
	}

	@Override
	public Integer getInteger(final String key) {
		Assertion.check().isNotNull(key);
		// ---
		return (Integer) values.get(key);
	}

	@Override
	public void putString(final String key, final String value) {
		doPut(key, Type.String, value);
	}

	@Override
	public void putInteger(final String key, final Integer value) {
		doPut(key, Type.Integer, value);
	}

	private void doPut(final String key, final Type type, final Object value) {
		Assertion.check()
				.isNotNull(key)
				.isNotNull(type);
		// ---
		//---
		final Type previousType = keys.put(key, type);
		if (previousType != null && type != previousType) {
			throw new IllegalStateException("the type is already defined" + previousType);
		}
		values.put(key, value);
	}

	@Override
	public void incrBy(final String key, final int value) {
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
	public Type getType(final String key) {
		return keys.get(key);
	}

	//------------------------------------
	//- List                             -
	//- All methods are prefixed with l  -
	//------------------------------------
	private BBList getListOrCreate(final String key) {
		Assertion.check()
				.isNotNull(key);
		//---
		BBList list = lists.get(key);
		if (list != null) {
			return list;
		}
		list = new BBList();
		lists.put(key, list);
		return list;
	}

	private BBList getListOrEmpty(final String key) {
		Assertion.check()
				.isNotNull(key);
		//---
		final BBList list = lists.get(key);
		return list == null
				? BBList.EMPTY
				: list;
	}

	@Override
	public int listSize(final String key) {
		return getListOrEmpty(key)
				.size();
	}

	@Override
	public void listPush(final String key, final String value) {
		getListOrCreate(key)
				.push(value);
	}

	@Override
	public String listPop(final String key) {
		return getListOrEmpty(key)
				.pop();
	}

	@Override
	public String listPeek(final String key) {
		return getListOrEmpty(key)
				.peek();
	}

	@Override
	public String listGet(final String key, final int idx) {
		return getListOrEmpty(key)
				.get(idx);
	}

	@Override
	public String getStoreName() {
		return storeNameOpt.orElse(BlackBoardManager.MAIN_STORE_NAME);
	}

	/**
	 * Retourne la connexion SQL de cette transaction en la demandant au pool de connexion si nécessaire.
	 * @return Connexion SQL
	 */
	private BBConnection obtainConnection(final String storeName) {
		final VTransaction transaction = transactionManager.getCurrentTransaction();
		BBConnection connection = transaction.getResource(getVTransactionResourceId(storeName));
		if (connection == null) {
			// On récupère une connexion du pool
			// Utilise le provider de connexion déclaré sur le Container.
			connection = new BBConnection(storeName);
			transaction.addResource(getVTransactionResourceId(storeName), connection);
		}
		return connection;
	}

	/**
	 * @return Id de la Ressource Connexion SQL dans la transaction
	 */
	protected VTransactionResourceId<BBConnection> getVTransactionResourceId(final String storeName) {
		if (BlackBoardManager.MAIN_STORE_NAME.equals(storeName)) {
			return MEMORY_BB_MAIN_RESOURCE_ID;
		}
		return new VTransactionResourceId<>(VTransactionResourceId.Priority.NORMAL, "Memory-BlackBoard-" + storeName);
	}
}
