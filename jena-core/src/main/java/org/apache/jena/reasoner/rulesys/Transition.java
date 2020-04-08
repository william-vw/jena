package org.apache.jena.reasoner.rulesys;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin;
import org.apache.jena.reasoner.rulesys.impl.BindingVector;

/**
 * A special type of built-in that represents a transition (or update) in
 * transaction-logic rules.
 * 
 * @author wvw
 *
 */

public abstract class Transition extends BaseBuiltin {

	protected Node[] ruleArgs;

	protected TransitionMemory memory;
	protected RollbackListener listener;

	/**
	 * Returns whether this is a transitional builtin, i.e., which needs to be
	 * rolled back when the rule ultimately fails.
	 */
	public boolean isTransition() {
		return true;
	}

	public void setMemory(TransitionMemory memory) {
		this.memory = memory;
	}

	public void setRollbackListener(RollbackListener listener) {
		this.listener = listener;
	}

	/**
	 * Pass a list of nodes, representing the arguments, to the builtin
	 * 
	 * @param args
	 */
	public void setRuleArgs(List<Node> args) {
		this.ruleArgs = args.toArray(new Node[args.size()]);
	}

	protected boolean check(RuleContext context, int len, Node... args) {
		if (args.length != len)
			throw new BuiltinException(this, context, "builtin expects " + len + " argument" + (len > 1 ? "s" : ""));

		for (Node arg : args) {
			if (!arg.isConcrete())
				throw new BuiltinException(this, context,
						"builtin expects concrete argument" + (len > 1 ? "s" : "") + ": " + arg);
		}
		return true;
	}

	// bridge between bodyCall() and rollback() methods

	protected Node[] toNodeArgs(BindingEnvironment env) {
		Node[] args = new Node[ruleArgs.length];
		for (int i = 0; i < args.length; i++)
			args[i] = env.getGroundVersion(ruleArgs[i]);

		return args;
	}

	protected void logCall(Node[] args) {
		info("call = " + Arrays.deepToString(args));
	}

	protected void logRollback(BindingEnvironment env) {
		info("rollback = " + env);
	}

	protected void info(Object msg) {
		System.out.println("[" + getName() + "] " + msg);
	}

	protected void error(Object msg) {
		System.err.println("[" + getName() + "] " + msg);
	}

	/**
	 * Represents a previous join attempt (kept by {@link TransitionMemory})
	 * 
	 * 
	 * @author wvw
	 *
	 */

	public static class JoinAttempt {

		private BindingVector token;
		private boolean joined;

		public JoinAttempt(BindingVector token, boolean joined) {
			this.token = token;
			this.joined = joined;
		}

		public BindingVector getToken() {
			return token;
		}

		public boolean isJoined() {
			return joined;
		}

		public void setJoined(boolean joined) {
			this.joined = joined;
		}

		public String toString() {
			return "<joined? " + joined + " - " + token + ">";
		}
	}

	/**
	 * Listens for actions originating from this transition.
	 * 
	 * 
	 * @author wvw
	 *
	 */

	public interface RollbackListener {

		/**
		 * Notifies that the given join attempt has become successful.
		 * 
		 * @param a
		 */

		public void joinFromFunctor(JoinAttempt a);

		/**
		 * Notifies that the given join attempt is no longer successful.
		 * 
		 * @param a
		 */

		public void rollbackFromFunctor(JoinAttempt a);
	}

	/**
	 * Keeps previous join attempts.
	 * 
	 * A difference with "regular" RETE memories is that failed joins with this
	 * transition are also kept {@link JoinAttempt}.
	 * 
	 * Here, a "failed join" means a token for which this transition was called, but
	 * was unsuccessful. For instance, this can mean a temporal constraint that was
	 * registered with the planner, but is currently not satisfied.
	 * 
	 * 
	 * @author wvw
	 *
	 */

	public interface TransitionMemory {

		public Collection<JoinAttempt> state();
	}
}