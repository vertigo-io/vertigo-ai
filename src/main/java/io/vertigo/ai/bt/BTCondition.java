package io.vertigo.ai.bt;

import java.util.function.Supplier;

import io.vertigo.core.lang.Assertion;

/**
 * A condition is a special node that returns a status 
 * - Succeeded
 * or 
 * - Failed 
 *  
 *  A condition must be evaluated very quickly.  
 *  If not it's a task !
 *  
 * @author pchretien
 */
public final class BTCondition implements BTNode {
	private final Supplier<Boolean> test;
	static final BTCondition SUCCEED = new BTCondition(() -> true);
	static final BTCondition FAIL = new BTCondition(() -> false);

	BTCondition(final Supplier<Boolean> test) {
		Assertion.check()
				.isNotNull(test);
		//---
		this.test = test;
	}

	@Override
	public BTStatus eval() {
		return test.get()
				? BTStatus.Succeeded
				: BTStatus.Failed;
	}
}
