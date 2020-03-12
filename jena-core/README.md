Jena README
============

Welcome to Apache Jena,  a Java framework for writing Semantic Web applications.

This repository is a fork of [Apache Jena](https://github.com/apache/jena.git).
Its goal is to implement a forward-chaining version of [Transaction Logic](https://en.wikipedia.org/wiki/Transaction_logic) into Jena's RETE network. 

### Extensions:

##### `org.apache.jena.reasoner.rulesys.Rule.Parser` 

The rule syntax is extended with an "serial conjunction" logical operator, i.e., '`&`'. Currently, only one type of conjunction is supported per rule, i.e., a rule either contains all '`,`' (classic conjunction) or all '`&`' (serial conjunction) operators. Any rule containing the '`&`' logical operator will be considered a transaction.

##### `org.apache.jena.reasoner.rulesys.Functor`, `Builtin`

These now return whether they constitute a transitional functor / builtin, i.e., which needs to be rolled back when the rule ultimately fails.


##### `org.apache.jena.reasoner.rulesys.impl.RETEFunctorClause`

In case a rule clause constitutes a Functor, a `RETEFunctorClause` will be created (these clauses are compiled in `RETEEngine.RETECompiler`). Normally, these functors would be checked in the `RETETerminal` node; if they were satisifed, the rule would be fired.

In a transaction rule, an alpha node may either be a classic clause filter (`RETEClauseFilter`), i.e., inserting triples matching a rule clause as tokens into the network; or constitute an elementary transition, i.e., performing a reversible task on a previously injected token. Such a transition would be implemented as a `Functor`, and hence be represented using a `RETEFunctorClause`.


##### `org.apache.jena.reasoner.rulesys.impl.RETEJoinQueue`

In a nutshell, at the top of a RETE network in Jena, the two root alpha nodes will each be connected to a `RETEJoinQueue`, which are linked as "siblings" to each other. A join queue is responsible for (**a**) trying to join new tokens with its sibling queue, and (**b**) keeping successfully joined tokens (i.e., it acts as both memory and beta node). In case of a third alpha node, the two initial join queues will both be connected to a third join queue, and the third alpha node will be linked to a fourth join queue, both siblings of each other. These join queues will attempt to join new tokens that were previously joined (i.e., by the prior join queues) with tokens coming directly from the third alpha node. The same structure and process applies to longer RETE networks.

In case the rule is transactional, several things may occur in a join queue:

(**1**) *An incoming token cannot be joined with the sibling join queue.*

Any prior alpha node in the RETE network could represent an elementary transition (i.e., reversible operation); either the join queue is directly linked to such a node, or an alpha node higher up in the network is such a node. 

In the former case, the join queues will simply apply the rollback to its alpha node. In the latter case, we know that all prior alpha nodes contributed to this join (due to the nature of the RETE algorithm) so the rollback will be propagated to all those nodes. Classic clause filters will ignore a rollback, whereas functor clauses will pass on the rollback to their associated `Functor`. In case the functor represents an elementary transition, the functor will take the appropriate steps to rollback the transition.


(**2**) *A deleted token is joined with the sibling join queue.*

In this case, the rollback will proceed in both directions of the RETE network. 

*Prior RETE network*: since the deleted token was successfully joined with the sibling queue, we know that all prior alpha nodes contributed to this join (due to the nature of the RETE algorithm). These alpha nodes possibly elementary transitions, and these should thus be rolled back. Hence, the rollback will be propagated through the prior (i.e., higher-up) RETE network, similar to failed joins with incoming tokens. 

*Following RETE network*: by design, the RETE algorithm will propagate the token deletion through the rest of the RETE network (i.e., lower-down) - this is because all tokens that resulted from a join with this deleted token should be deleted as well. In this process, whenever this deleted token is successfully joined to the sibling join queue, we will issue a rollback to the alpha node connected to the sibling join queue.