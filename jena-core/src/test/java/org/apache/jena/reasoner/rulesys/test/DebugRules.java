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

package org.apache.jena.reasoner.rulesys.test;

import java.util.List;

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.BuiltinRegistry;
import org.apache.jena.reasoner.rulesys.GenericRuleReasonerFactory;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.reasoner.rulesys.Util;
import org.apache.jena.reasoner.rulesys.Within;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.PrintUtil;
import org.apache.jena.vocabulary.ReasonerVocabulary;

/**
 * * Using during debuging of the rule systems. Runs a named set of rules (can
 * contain axioms and rules) and lists all the resulting entailments.
 */
public class DebugRules {

	/** The name of the ruleset to load */
	public static final String ruleFile = "tr/tr.rules";

	/** The name of the dataset to load */
	public static final String dataFile = "tr/data2.rdf";

	/** Constructor */
	public DebugRules() {
	}

	/** Run a single test */
//	public void run() {
//		BasicForwardRuleReasoner reasoner = new BasicForwardRuleReasoner(ruleset);
//		InfGraph result = reasoner.bind(Factory.createGraphMem());
//		System.out.println("Derivations:");
//		for (Iterator<Triple> i = result.find(null, null, null); i.hasNext();) {
//			System.out.println(PrintUtil.print(i.next()));
//		}
//	}

	public void run() {
		PrintUtil.registerPrefix("cig", "http://niche.cs.dal.ca/ns/cig/");
		BuiltinRegistry.theRegistry.register("within", new Within());

		List<Rule> ruleset = Rule.parseRules(Util.loadRuleParserFromResourceFile(ruleFile));
		System.out.println("Rules:");
		ruleset.stream().forEach(r -> System.out.println(r));

		Model m = ModelFactory.createDefaultModel();
		Resource config = m.createResource();
		config.addProperty(ReasonerVocabulary.PROPruleMode, "forwardRETE");
		config.addProperty(ReasonerVocabulary.PROPruleSet, ruleFile);
		Reasoner reasoner = GenericRuleReasonerFactory.theInstance().create(config);

		Model data = FileManager.get().loadModel(dataFile, "TURTLE");
		InfModel infModel = ModelFactory.createInfModel(reasoner, data);

		System.out.println("\nDerivations:");
		StmtIterator stmtIt = infModel.getDeductionsModel().listStatements();
		while (stmtIt.hasNext())
			System.out.println(stmtIt.next());
	}

	public static void main(String[] args) {
		try {
			new DebugRules().run();

		} catch (Exception e) {
			System.out.println("Problem: " + e);
		}
	}

}
