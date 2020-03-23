/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.reasoner.rulesys.impl;

import java.util.Iterator;
import java.util.Map;

import org.apache.jena.graph.Node;

/**
 * Represents one input left of a join node. The queue points to a sibling queue
 * representing the other leg which should be joined against.
 */
public class RETEJoinQueue extends RETEQueue implements RETESinkNode {

	/** Whether a prior RETE node involves a transition (transactional rules) **/
	protected boolean priorTransition;
	/**
	 * Whether the current or a next RETE node involves a transition (transactional
	 * rules)
	 **/
	protected boolean nextOrCurTransition;

	/** The node that results were passed on from */
	protected RETESourceNode preceding;

	/**
	 * Constructor. The queue is not usable until it has been bound to a sibling and
	 * a continuation node.
	 * 
	 */
	public RETEJoinQueue(boolean isTransactional, boolean priorTransition, boolean nextOrCurTransition) {
		super(isTransactional);

		this.priorTransition = priorTransition;
		this.nextOrCurTransition = nextOrCurTransition;
	}

	/**
	 * Set the preceding node for this node (and any sibling).
	 */
	@Override
	public void setPreceding(RETESourceNode preceding) {
		this.preceding = preceding;
	}

	/**
	 * Propagate a token to this node.
	 * 
	 * @param env   a set of variable bindings for the rule being processed.
	 * @param isAdd distinguishes between add and remove operations.
	 */
	@Override
	public void fire(BindingVector env, boolean isAdd) {
		// Cross match new token against the entries in the sibling queue
		Node[] envNodes = env.getEnvironment();

		Iterator<BindingVector> i = sibling.getSubSet(env, isAdd);
		if (i.hasNext()) {

			while (i.hasNext()) {
				Node[] candidate = i.next().getEnvironment();
				// matching is no longer required since getSubSet(env) returns
				// a HashMap with matching BindingVectors

				// Instantiate a new extended environment
				Node[] newNodes = new Node[candidate.length];
				for (int j = 0; j < candidate.length; j++) {
					Node n = candidate[j];
					newNodes[j] = (n == null) ? envNodes[j] : n;
				}

				BindingVector newEnv = new BindingVector(newNodes);
				if (isTransactional && !isAdd) {
					// - in case of the initial call:
					// (preceding node is an alpha node)
					// sibling will ask its preceding node to rollback
					// (either an alpha node or other join queue)

					// - in case of a call lower-down in network:
					// (preceding node is a join queue)
					// sibling will ask its preceding node to rollback
					// (will always be an alpha node)
					if (isAlphaQueue() || nextOrCurTransition) {
//						System.out.println("[RETEJoinQueue] delete.rollback: "
//								+ (isAlphaQueue() ? id : "join " + sibling.getId()) + "; " + env);
						sibling.propagateToPreceding(newEnv);
					}
				}

				// Fire the successor processing
				// (for a delete in transactional rule, this will also
				// propagate the delete to lower down in the network)
				continuation.fire(newEnv, isAdd);
			}

		} else {
			if (isTransactional && isAdd) {
				propagateToPreceding(env);
			}
		}
	}

	protected boolean isAlphaQueue() {
		return (preceding instanceof RETEClauseFilter);
	}

	@Override
	protected void propagateToPreceding(BindingVector env) {
		if (isAlphaQueue() || priorTransition) {
			preceding.rollback(env);
		}
	}

	@Override
	public void rollback(BindingVector env) {
		sibling.propagateToPreceding(env);

		preceding.rollback(env);
	}

	@Override
	public Iterator<BindingVector> getSubSet(BindingVector env, boolean isAdd) {
		return null;
	}

	@Override
	public RETENode clone(Map<RETENode, RETENode> netCopy, RETERuleContext context) {
		return null;
	}
}
