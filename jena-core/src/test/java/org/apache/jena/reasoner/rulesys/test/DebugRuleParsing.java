package org.apache.jena.reasoner.rulesys.test;

import java.util.List;

import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.reasoner.rulesys.Util;
import org.apache.jena.util.PrintUtil;

/**
 * Using for debugging rule parsing (with transaction rule support).
 */

public class DebugRuleParsing {

	/** The name of the rule set to load */
	public static final String ruleFile = "etc/tr.rules";

	public static void main(String[] args) {
		PrintUtil.registerPrefix("cig", "http://niche.cs.dal.ca/ns/cig/");
		List<Rule> rules = Rule.parseRules(Util.loadRuleParserFromResourceFile(ruleFile));

		rules.stream().forEach(r -> System.out.println(r));
	}
}
