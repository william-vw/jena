package org.apache.jena.reasoner.rulesys.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Represents one input left of a join node. The queue points to a sibling queue
 * representing the other leg which should be joined against.
 */
public class RETEJoinMemoryQueue extends RETEJoinQueue {

	/**
	 * A multi-set of partially bound envionments indices for matching are specified
	 * by matchIndices
	 */
	protected BindingVectorMultiSet queue;

	/** A set of variable indices which should match between the two inputs */
	protected byte[] matchIndices;

	/**
	 * Constructor. The queue is not usable until it has been bound to a sibling and
	 * a continuation node.
	 * 
	 * @param matchIndices set of variable indices which should match between the
	 *                     two inputs
	 */
	public RETEJoinMemoryQueue(byte[] matchIndices, boolean isTransactional, boolean priorTr, boolean nextTr) {
		super(isTransactional, priorTr, nextTr);

		this.matchIndices = matchIndices;
		this.queue = new BindingVectorMultiSet(matchIndices);
	}

	/**
	 * Constructor. The queue is not usable until it has been bound to a sibling and
	 * a continuation node.
	 * 
	 * @param matchIndexList List of variable indices which should match between the
	 *                       two inputs
	 */
	public RETEJoinMemoryQueue(List<? extends Byte> matchIndexList, boolean isTransactional, boolean priorTransition,
			boolean nextOrCurTransition) {

		super(isTransactional, priorTransition, nextOrCurTransition);

		int len = matchIndexList.size();
		matchIndices = new byte[len];
		for (int i = 0; i < len; i++) {
			matchIndices[i] = matchIndexList.get(i);
		}
		this.queue = new BindingVectorMultiSet(matchIndices);
	}

	/**
	 * Propagate a token to this node.
	 * 
	 * @param env   a set of variable bindings for the rule being processed.
	 * @param isAdd distinguishes between add and remove operations.
	 */
	@Override
	public void fire(BindingVector env, boolean isAdd) {
		// Store the new token in this store
		if (isAdd) {
			queue.add(env);
		} else {
			queue.remove(env);
		}

		super.fire(env, isAdd);
	}

	@Override
	public Iterator<BindingVector> getSubSet(BindingVector env, boolean isAdd) {
		// some builtins may edit the underlying KB
		// so, ensure that this does not cause a concurrent-modification exception

		List<BindingVector> ret = new ArrayList<>();
		queue.getSubSet(env).forEachRemaining(e -> ret.add(e));

		return ret.iterator();
	}

	/**
	 * Clone this node in the network.
	 * 
	 * @param context the new context to which the network is being ported
	 */
	@Override
	public RETENode clone(Map<RETENode, RETENode> netCopy, RETERuleContext context) {
		RETEJoinMemoryQueue clone = (RETEJoinMemoryQueue) netCopy.get(this);
		if (clone == null) {
			clone = new RETEJoinMemoryQueue(matchIndices, isTransactional, priorTransition, nextOrCurTransition);
			netCopy.put(this, clone);
			clone.setSibling((RETEJoinQueue) sibling.clone(netCopy, context));
			clone.setContinuation((RETESinkNode) continuation.clone(netCopy, context));
			clone.queue.putAll(queue);
		}
		return clone;
	}
}
