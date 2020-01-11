package org.apache.jena.reasoner.rulesys;

import java.util.Arrays;

import org.apache.jena.graph.Node;
import org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin;

public class Within extends BaseBuiltin {

	/**
	 * Return a name for this builtin, normally this will be the name of the functor
	 * that will be used to invoke it.
	 */
	@Override
	public String getName() {
		return "within";
	}

	/**
	 * Return the expected number of arguments for this functor or 0 if the number
	 * is flexible.
	 */
	@Override
	public int getArgLength() {
		return 0;
	}

	/**
	 * This method is invoked when the builtin is called in a rule body.
	 * 
	 * @param args    the array of argument values for the builtin, this is an array
	 *                of Nodes, some of which may be Node_RuleVariables.
	 * @param length  the length of the argument list, may be less than the length
	 *                of the args array for some rule engines
	 * @param context an execution context giving access to other relevant data
	 * @return return true if the buildin predicate is deemed to have succeeded in
	 *         the current environment
	 */
	@Override
	public boolean bodyCall(Node[] args, int length, RuleContext context) {
		checkArgs(length, context);

		if (length < 2)
			throw new BuiltinException(this, context, "builtin " + getName() + " requires at least 2 arguments");

//		System.out.println(Arrays.asList(args).subList(0, length - 1) + " within " + args[length - 1]);

		return true;
	}
}