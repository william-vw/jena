package org.apache.jena.reasoner.rulesys.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.jena.reasoner.rulesys.Functor;
import org.apache.jena.util.iterator.EmptyIterator;
import org.apache.jena.util.iterator.SingletonIterator;

public class RETEFunctorFilter extends RETEQueue {

	protected Functor functor;
	protected RETERuleContext context;
	// only used for transitions
	protected Set<BindingVector> joins = new HashSet<>();

	public RETEFunctorFilter(Functor functor, RETERuleContext context, boolean isTransactional) {
		super(isTransactional);

		this.functor = functor;
		this.context = context;
	}

	@Override
	public void setContinuation(RETESinkNode continuation) {
		this.continuation = continuation;
	}

	@Override
	public Iterator<BindingVector> getSubSet(BindingVector env, boolean isAdd) {
		if (!functor.isTransition() || isAdd) {
			try {
				context.setEnv(env);

				if (functor.evalAsBodyClause(context)) {
					
					if (functor.isTransition())
						joins.add(env);
					
					return new SingletonIterator<BindingVector>(env);

				} else
					return new EmptyIterator<BindingVector>();

			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

			// when removing a token, a join will be attempted with this queue
			// but we don't want to re-apply the builtin just for the purpose of
			// re-confirming the join - this was already done previously

			// so, keep successful joins here and use them to confirm deletes

		} else {
			if (joins.remove(env))
				return new SingletonIterator<BindingVector>(env);
			else
				return new EmptyIterator<BindingVector>();
		}
	}

	@Override
	protected void propagateToPreceding(BindingVector env) {
		rollback(env);
	}

	@Override
	public void rollback(BindingVector env) {
		functor.rollback(env);
	}

	@Override
	public RETENode clone(Map<RETENode, RETENode> netCopy, RETERuleContext context) {
		// TODO
		return null;
	}
}