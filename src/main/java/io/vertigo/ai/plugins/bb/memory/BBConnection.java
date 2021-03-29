package io.vertigo.ai.plugins.bb.memory;

import java.sql.SQLException;

import io.vertigo.commons.transaction.VTransactionResource;
import io.vertigo.core.lang.Assertion;

/**
 * Represents a connection to a blackboard
 * @author mlaroche
 */
public final class BBConnection implements VTransactionResource {
	private final String storeName;

	/**
	 * Constructor.
	 *
	 */
	public BBConnection(final String storeName) {
		Assertion.check()
				.isNotBlank(storeName);
		//-----
		this.storeName = storeName;
	}

	public String getStoreName() {
		return storeName;
	}

	/** {@inheritDoc} */
	@Override
	public void commit() throws SQLException {
		// nothing for now
	}

	/** {@inheritDoc} */
	@Override
	public void rollback() throws SQLException {
		// nothing for now
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws SQLException {
		// nothing for now
	}
}
