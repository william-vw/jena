package org.apache.jena.reasoner.rulesys.impl;

import java.util.Iterator;

public abstract class RETEQueue implements RETESourceNode {

	/** The node that results should be passed on to */
	protected RETESinkNode continuation;

	/** The sibling queue which forms the other half of the join node */
	protected RETEQueue sibling;

	/** Flags whether the rule is transactional */
	protected boolean isTransactional = false;

	/** For testing only **/
	protected String id;

	public RETEQueue(boolean isTransactional) {
		this.isTransactional = isTransactional;
	}

	public String getId() {
		return id;
	}

	/**
	 * Set the continuation node for this node (and any sibling).
	 * 
	 * This will automatically set the preceding nodes as well.
	 */
	@Override
	public void setContinuation(RETESinkNode continuation) {
		this.continuation = continuation;
		if (sibling != null)
			sibling.continuation = continuation;
	}

	/**
	 * Get the continuation node for this node.
	 */
	@Override
	public RETESinkNode getContinuation() {
		return continuation;
	}

	/**
	 * Set the sibling for this node.
	 */
	public void setSibling(RETEQueue sibling) {
		this.sibling = sibling;
	}

	protected abstract void propagateToPreceding(BindingVector env);

	public abstract Iterator<BindingVector> getSubSet(BindingVector env, boolean isAdd);
}
