package org.apache.jena.reasoner.rulesys.impl;

import java.util.Iterator;
import java.util.Map;

import org.apache.jena.reasoner.rulesys.Functor;
import org.apache.jena.util.iterator.EmptyIterator;
import org.apache.jena.util.iterator.SingletonIterator;

public class RETEFunctorFilter extends RETEQueue {

	protected Functor functor;
	protected RETERuleContext context;

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
	public Iterator<BindingVector> getSubSet(BindingVector env) {
		try {
			context.setEnv(env);
			if (functor.evalAsBodyClause(context))
				return new SingletonIterator<BindingVector>(env);
			else
				return new EmptyIterator<BindingVector>();

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected void propagateToPreceding(BindingVector env) {
		rollback(env);
	}

	@Override
	public void rollback(BindingVector env) {
//		System.out.println("rollback: " + functor);
		functor.rollback(env);
	}

	@Override
	public RETENode clone(Map<RETENode, RETENode> netCopy, RETERuleContext context) {
		// TODO
		return null;
	}
}