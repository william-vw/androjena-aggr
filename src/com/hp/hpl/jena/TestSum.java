package com.hp.hpl.jena;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.util.PrintUtil;

public class TestSum {

	public static class AssetManager {

		public InputStream open(String path) throws IOException {
			return new FileInputStream(Paths.get(path).toFile());
		}
	}

	public static void main(String[] args) throws Exception {
		String dataPath = "test/sum.ttl";
		String rulePath = "test/sum.jena";

		AssetManager assetMan = new AssetManager();

		// - load data

		Model m = ModelFactory.createDefaultModel();
		m.read(assetMan.open(dataPath), null, "TURTLE");

		// - load & parse rules

		Map<String, String> prefixNs = new HashMap<>();
		prefixNs.put("", "http://wvw.ca/ns/sum.owl#");

		PrintUtil.registerPrefixMap(prefixNs);

		List<Rule> rules = new ArrayList<>();

		BufferedReader br = new BufferedReader(new InputStreamReader(assetMan.open(rulePath)));
		rules.addAll(Rule.parseRules(Rule.rulesParserFromReader(br)));
		// - create inf model

		GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
		reasoner.setMode(GenericRuleReasoner.FORWARD);

		InfModel infModel = ModelFactory.createInfModel(reasoner, m);
		
		System.out.println("- deductions model:");
		infModel.getDeductionsModel().write(System.out, "TURTLE");

		System.out.println("\n- warnings:");
		// - check inferences
		StmtIterator it = infModel.listStatements(null, infModel.createProperty("http://wvw.ca/ns/sum.owl#warning"),
				(RDFNode) null);
		while (it.hasNext()) {
			Statement stmt = it.next();
			System.out.println("warning: " + stmt.getObject());
		}
	}
}
