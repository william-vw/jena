package org.apache.jena.reasoner.rulesys.test.tr;

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

public class TaskDiscard extends Transition {

	public static final String name = "task:discard";

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
		return 1;
	}

	@Override
	public boolean bodyCall(Node[] args, int length, RuleContext context) {
		logCall(args);

		if (!check(context, length, args))
			return false;

		// ...

		return true;
	}

	/**
	 * In case of a transitional builtin, rollback the underlying action.
	 */
	@Override
	public void rollback(BindingEnvironment env) {
		logRollback(env);
	}
}