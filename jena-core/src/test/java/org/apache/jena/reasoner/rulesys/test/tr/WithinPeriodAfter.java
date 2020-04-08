package org.apache.jena.reasoner.rulesys.test.tr;

import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.reasoner.rulesys.BindingEnvironment;
import org.apache.jena.reasoner.rulesys.RuleContext;
import org.apache.jena.reasoner.rulesys.Transition;

/**
 * Transition built-in meant for testing.
 * 
 * 
 * @author wvw
 *
 */

public class WithinPeriodAfter extends Transition {

	public static final String name = "tmp:withinPeriodAfter";

	// initially transition is unsuccessful
	// but, becomes successful after update

	public static final int TEST_CASE1 = 0;

	// initially transition is successful
	// but, becomes unsuccessful after update

	public static final int TEST_CASE2 = 1;

	private int testCase = 1;

	public void setTestCase(int testCase) {
		this.testCase = testCase;
	}

	/**
	 * Return a name for this builtin, normally this will be the name of the functor
	 * that will be used to invoke it.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Return the expected number of arguments for this functor or 0 if the number
	 * is flexible.
	 */
	@Override
	public int getArgLength() {
		return 4;
	}

	@Override
	public boolean bodyCall(Node[] args, int length, RuleContext context) {
		logCall(args);

		if (!check(context, length, args))
			return false;

		switch (testCase) {

		case TEST_CASE1:
			return false;

		case TEST_CASE2:
			return true;
		}

		return true;
	}

	/**
	 * In case of a transitional builtin, rollback the underlying action.
	 */
	@Override
	public void rollback(BindingEnvironment env) {
		logRollback(env);
	}

	public void updated() {
		Iterator<JoinAttempt> state = memory.state().iterator();

		while (state.hasNext()) {
			JoinAttempt a = state.next();
			info("token? " + a);

			switch (testCase) {

			case TEST_CASE1:
				listener.joinFromFunctor(a);
				break;

			case TEST_CASE2:
				listener.rollbackFromFunctor(a);
				break;
			}
		}
	}
}