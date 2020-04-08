package org.apache.jena.reasoner.rulesys.test.tr;

import java.io.FileInputStream;
import java.io.IOException;
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
import org.apache.jena.util.PrintUtil;
import org.apache.jena.vocabulary.ReasonerVocabulary;

/**
 * Tests for transactional rules.
 * 
 * 
 * @author wvw
 *
 */

public class TestTransactionRules {

	public static void main(String[] args) throws Exception {
		TestTransactionRules test = new TestTransactionRules();

		test.test();
	}

	private TaskDiscard discard = new TaskDiscard();
	private WithinPeriodAfter withinPeriodAfter = new WithinPeriodAfter();

	public TestTransactionRules() {
		init();
	}

	public void test() throws IOException {
		// - test case 1

		// initially transition is unsuccessful
		// but, becomes successful after update
		// after stage 2, builtin task:discard should be called

		System.out.println("- TEST CASE 1");

		withinPeriodAfter.setTestCase(WithinPeriodAfter.TEST_CASE1);

		testStage1();
		testStage2();

		System.out.println("\n\n");

		// - test case 2

		// initially transition is successful
		// but, becomes unsuccessful after update
		// after stage 1, builtin task:discard should be called
		// after stage 2, builtin task:discard should be rolled back

		System.out.println("- TEST CASE 2");

		withinPeriodAfter.setTestCase(WithinPeriodAfter.TEST_CASE2);

		testStage1();
		testStage2();
	}

	private void testStage1() throws IOException {
		String path = "testing/reasoners/transaction/";

		// - read data

		String dataPath = path + "tmp_discard.rdf";

		Model m = ModelFactory.createDefaultModel();
		m.read(new FileInputStream(dataPath), "", "N3");

		// - setup reasoner

		String rulesPath = path + "tmp_discard.rules";
		printRules(rulesPath);

		Resource config = m.createResource();
		config.addProperty(ReasonerVocabulary.PROPruleMode, "forwardRETE");
		config.addProperty(ReasonerVocabulary.PROPruleSet, rulesPath);

		Reasoner reasoner = GenericRuleReasonerFactory.theInstance().create(config);
		InfModel inf = ModelFactory.createInfModel(reasoner, m);

		// - print derivations (force inferencing)

		StmtIterator stmtIt = inf.getDeductionsModel().listStatements();
		boolean found = stmtIt.hasNext();

		while (stmtIt.hasNext()) {
			System.out.println("derivation:");
			System.out.println(stmtIt.next() + "\n");
		}

		if (!found)
			System.out.println("derivations: none\n");
	}

	private void testStage2() {
		withinPeriodAfter.updated();
	}

	private void init() {
		PrintUtil.registerPrefix("cig", "http://niche.cs.dal.ca/ns/cig-into#");

		// typically, care should be taken that one transition builtin object is created
		// per rule (since they often keep rule-specific state)

		// here, we use one for all rules for testing purposes

		BuiltinRegistry.theRegistry.register(TaskDiscard.name, discard);
		BuiltinRegistry.theRegistry.register(WithinPeriodAfter.name, withinPeriodAfter);
	}

	private void printRules(String ruleFile) {
		List<Rule> rules = Rule.parseRules(Util.loadRuleParserFromResourceFile(ruleFile));
		System.out.println("rules:");
		rules.stream().forEach(r -> System.out.println(r));
		System.out.println();
	}
}
