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

/**
 * Interface for all non-terminal nodes in the network.
 */
public interface RETESourceNode extends RETENode {

	/** For testing only **/
	public String getId();

	/**
	 * Set the continuation node for this node.
	 */
	public void setContinuation(RETESinkNode continuation);

	/**
	 * Get the continuation node for this node.
	 */
	public RETESinkNode getContinuation();

	/**
	 * Returns whether this is an alpha node.
	 */
	public boolean isAlphaNode();

	/**
	 * In case a particular match failed in a transactional rule, rollback any
	 * state-changing operations that were performed as part of this match
	 * 
	 * @param env
	 */
	public void rollback(BindingVector env);
}
