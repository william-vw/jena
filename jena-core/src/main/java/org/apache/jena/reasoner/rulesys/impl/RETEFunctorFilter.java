package org.apache.jena.reasoner.rulesys.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.jena.reasoner.rulesys.Functor;
import org.apache.jena.reasoner.rulesys.Transition;
import org.apache.jena.reasoner.rulesys.Transition.JoinAttempt;
import org.apache.jena.reasoner.rulesys.Transition.RollbackListener;
import org.apache.jena.reasoner.rulesys.Transition.TransitionMemory;
import org.apache.jena.util.iterator.EmptyIterator;
import org.apache.jena.util.iterator.SingletonIterator;

/**
 * A clause representing a functor (built-in) call. It doubles as an alpha node
 * as well as a beta node.
 * 
 * 
 * @author wvw
 *
 */

public class RETEFunctorFilter extends RETEQueue implements TransitionMemory, RollbackListener {

	protected Functor functor;
	protected RETERuleContext context;

	// only used for transitions
	// (~ acts as a special type of join memory)
	protected Map<BindingVector, JoinAttempt> state = new HashMap<>();

	public RETEFunctorFilter(Functor functor, RETERuleContext context, boolean isTransactional) {
		super(isTransactional);

		this.functor = functor;
		this.context = context;

		if (functor.isTransition()) {
			Transition tr = (Transition) functor.getImplementor();
			tr.setRollbackListener(this);
			tr.setMemory(this);
		}
	}

	@Override
	public void setContinuation(RETESinkNode continuation) {
		this.continuation = continuation;
	}

	@Override
	public Iterator<BindingVector> getSubSet(BindingVector env, boolean isAdd) {
		context.setEnv(env);

		// in case of a transition:
		// when removing a token, a join will be attempted with this queue
		// but we don't want to re-apply the builtin just for the purpose of
		// re-confirming a join - either it was successful previously or not

		// so, keep joins here and use them to confirm deletes

		// note that this is not the only reason for keeping state;
		// state can also be utilized by transition built-ins

		if (functor.isTransition() && !isAdd) {
			JoinAttempt a = state.remove(env);
			if (a != null && a.isJoined())
				return new SingletonIterator<BindingVector>(env);
			else
				return new EmptyIterator<BindingVector>();

		} else {
			boolean joined = functor.evalAsBodyClause(context);
			if (functor.isTransition())
				state.put(env, new JoinAttempt(env, joined));

			if (joined)
				return new SingletonIterator<BindingVector>(env);
			else
				return new EmptyIterator<BindingVector>();
		}
	}

	@Override
	public Collection<JoinAttempt> state() {
		return state.values();
	}

	@Override
	public void joinFromFunctor(JoinAttempt a) {
		a.setJoined(true);

		continuation.fire(a.getToken(), true);
	}

	@Override
	public void rollbackFromFunctor(JoinAttempt a) {
		a.setJoined(false);

		sibling.propagateToPreceding(a.getToken());
		continuation.fire(a.getToken(), false);
	}

	@Override
	protected void propagateToPreceding(BindingVector env) {
		rollback(env);
	}

	@Override
	public boolean isAlphaNode() {
		return true;
	}

	@Override
	public void rollback(BindingVector env) {
		// System.out.println("[RETEFunctorFilter] rollback: " +
		// functor.getImplementor().getName() + " - " + env);
		functor.rollback(env);

		if (functor.isTransition())
			state.remove(env);
	}

	@Override
	public RETENode clone(Map<RETENode, RETENode> netCopy, RETERuleContext context) {
		// TODO
		return null;
	}
}