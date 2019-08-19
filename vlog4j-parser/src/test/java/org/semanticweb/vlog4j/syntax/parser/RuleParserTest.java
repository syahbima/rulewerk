package org.semanticweb.vlog4j.syntax.parser;

/*-
 * #%L
 * VLog4j Syntax
 * %%
 * Copyright (C) 2018 - 2019 VLog4j Developers
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.junit.Test;
import org.semanticweb.vlog4j.core.model.api.Conjunction;
import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.api.Literal;
import org.semanticweb.vlog4j.core.model.api.PositiveLiteral;
import org.semanticweb.vlog4j.core.model.api.Predicate;
import org.semanticweb.vlog4j.core.model.api.PrefixDeclarations;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.api.Variable;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import org.semanticweb.vlog4j.core.reasoner.implementation.CsvFileDataSource;
import org.semanticweb.vlog4j.core.reasoner.implementation.RdfFileDataSource;
import org.semanticweb.vlog4j.core.reasoner.implementation.SparqlQueryResultDataSource;
import org.semanticweb.vlog4j.parser.ParsingException;
import org.semanticweb.vlog4j.parser.RuleParser;

public class RuleParserTest {

	private final Variable x = Expressions.makeVariable("X");
	private final Variable y = Expressions.makeVariable("Y");
	private final Variable z = Expressions.makeVariable("Z");
	private final Constant c = Expressions.makeConstant("http://example.org/c");
	private final Constant d = Expressions.makeConstant("http://example.org/d");
	private final Constant abc = Expressions.makeConstant("\"abc\"^^<" + PrefixDeclarations.XSD_STRING + ">");
	private final Literal atom1 = Expressions.makePositiveLiteral("http://example.org/p", x, c);
	private final Literal negAtom1 = Expressions.makeNegativeLiteral("http://example.org/p", x, c);
	private final Literal atom2 = Expressions.makePositiveLiteral("http://example.org/p", x, z);
	private final PositiveLiteral atom3 = Expressions.makePositiveLiteral("http://example.org/q", x, y);
	private final PositiveLiteral atom4 = Expressions.makePositiveLiteral("http://example.org/r", x, d);
	private final PositiveLiteral fact = Expressions.makePositiveLiteral("http://example.org/s", c);
	private final PositiveLiteral fact2 = Expressions.makePositiveLiteral("p", abc);
	private final Conjunction<Literal> body1 = Expressions.makeConjunction(atom1, atom2);
	private final Conjunction<Literal> body2 = Expressions.makeConjunction(negAtom1, atom2);
	private final Conjunction<PositiveLiteral> head = Expressions.makePositiveConjunction(atom3, atom4);
	private final Rule rule1 = Expressions.makeRule(head, body1);
	private final Rule rule2 = Expressions.makeRule(head, body2);

	@Test
	public void testExplicitIri() throws ParsingException {
		String input = "<http://example.org/s>(<http://example.org/c>) .";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
		assertEquals(Arrays.asList(fact), ruleParser.getFacts());
	}

	@Test
	public void testPrefixResolution() throws ParsingException {
		String input = "@prefix ex: <http://example.org/> . ex:s(ex:c) .";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
		assertEquals(Arrays.asList(fact), ruleParser.getFacts());
	}

	@Test
	public void testBaseRelativeResolution() throws ParsingException {
		String input = "@base <http://example.org/> . <s>(<c>) .";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
		assertEquals(Arrays.asList(fact), ruleParser.getFacts());
	}

	@Test
	public void testBaseResolution() throws ParsingException {
		String input = "@base <http://example.org/> . s(c) .";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
		assertEquals(Arrays.asList(fact), ruleParser.getFacts());
	}

	@Test
	public void testNoBaseRelativeIri() throws ParsingException {
		String input = "s(c) .";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
		PositiveLiteral atom = Expressions.makePositiveLiteral("s", Expressions.makeConstant("c"));
		assertEquals(Arrays.asList(atom), ruleParser.getFacts());
	}

	@Test(expected = ParsingException.class)
	public void testPrefixConflict() throws ParsingException {
		String input = "@prefix ex: <http://example.org/> . @prefix ex: <http://example.org/2/> . s(c) .";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
	}

	@Test(expected = ParsingException.class)
	public void testBaseConflict() throws ParsingException {
		String input = "@base <http://example.org/> . @base <http://example.org/2/> . s(c) .";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
	}

	@Test(expected = ParsingException.class)
	public void testMissingPrefix() throws ParsingException {
		String input = "ex:s(c) .";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
	}

	@Test(expected = ParsingException.class)
	public void testNoUniversalLiterals() throws ParsingException {
		String input = "p(?X) .";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
	}

	@Test(expected = ParsingException.class)
	public void testNoExistentialLiterals() throws ParsingException {
		String input = "p(!X) .";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
	}

	@Test
	public void testSimpleRule() throws ParsingException {
		String input = "@base <http://example.org/> . " + " q(?X, !Y), r(?X, d) :- p(?X,c), p(?X,?Z) . ";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
		assertEquals(Arrays.asList(rule1), ruleParser.getRules());
	}

	@Test
	public void testNegationRule() throws ParsingException {
		String input = "@base <http://example.org/> . " + " q(?X, !Y), r(?X, d) :- ~p(?X,c), p(?X,?Z) . ";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
		assertEquals(Arrays.asList(rule2), ruleParser.getRules());
	}

	@Test(expected = ParsingException.class)
	public void testUnsafeNegationRule() throws ParsingException {
		String input = "@base <http://example.org/> . " + " q(?X, !Y), r(?X, d) :- ~p(?Y,c), p(?X,?Z) . ";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
	}

	@Test
	public void testWhiteSpace() throws ParsingException {
		String input = "@base \n\n<http://example.org/> . "
				+ " q(?X, !Y)  , r(?X,    d\t ) \n\n:- p(?X,c), p(?X,\n?Z) \n. ";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
		assertEquals(Arrays.asList(rule1), ruleParser.getRules());
	}

	@Test(expected = ParsingException.class)
	public void testNoUnsafeVariables() throws ParsingException {
		String input = "p(?X,?Y) :- q(?X) .";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
	}

	@Test(expected = ParsingException.class)
	public void testNoConflictingQuantificationVariables() throws ParsingException {
		String input = "p(?X,!X) :- q(?X) .";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
	}

	@Test(expected = ParsingException.class)
	public void testNoBodyExistential() throws ParsingException {
		String input = "p(?X) :- q(?X,!Y) .";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
	}

	@Test(expected = ParsingException.class)
	public void testNoDollarVariables() throws ParsingException {
		String input = "p($X) :- q($X) .";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
	}

	@Test
	public void testIntegerLiteral() throws ParsingException {
		String input = "p(42) .";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
		PositiveLiteral integerLiteral = Expressions.makePositiveLiteral("p",
				Expressions.makeConstant("42^^<" + PrefixDeclarations.XSD_INTEGER + ">"));
		assertEquals(Arrays.asList(integerLiteral), ruleParser.getFacts());
	}

	@Test
	public void testAbbreviatedIntegerLiteral() throws ParsingException {
		String input = "@prefix xsd: <" + PrefixDeclarations.XSD + "> . " + "p(\"42\"^^xsd:integer) .";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
		PositiveLiteral integerLiteral = Expressions.makePositiveLiteral("p",
				Expressions.makeConstant("\"42\"^^<" + PrefixDeclarations.XSD_INTEGER + ">"));
		assertEquals(Arrays.asList(integerLiteral), ruleParser.getFacts());
	}

	@Test
	public void testFullIntegerLiteral() throws ParsingException {
		String input = "p(\"42\"^^<" + PrefixDeclarations.XSD_INTEGER + "> ) .";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
		PositiveLiteral integerLiteral = Expressions.makePositiveLiteral("p",
				Expressions.makeConstant("\"42\"^^<" + PrefixDeclarations.XSD_INTEGER + ">"));
		assertEquals(Arrays.asList(integerLiteral), ruleParser.getFacts());
	}

	@Test
	public void testDecimalLiteral() throws ParsingException {
		String input = "p(-5.0) .";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
		PositiveLiteral decimalLiteral = Expressions.makePositiveLiteral("p",
				Expressions.makeConstant("-5.0^^<" + PrefixDeclarations.XSD_DECIMAL + ">"));
		assertEquals(Arrays.asList(decimalLiteral), ruleParser.getFacts());
	}

	@Test
	public void testDoubleLiteral() throws ParsingException {
		String input = "p(4.2E9) .";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
		PositiveLiteral doubleLiteral = Expressions.makePositiveLiteral("p",
				Expressions.makeConstant("4.2E9^^<" + PrefixDeclarations.XSD_DOUBLE + ">"));
		assertEquals(Arrays.asList(doubleLiteral), ruleParser.getFacts());
	}

	@Test
	public void testStringLiteral() throws ParsingException {
		String input = "p(\"abc\") .";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
		assertEquals(Arrays.asList(fact2), ruleParser.getFacts());
	}

	@Test(expected = ParsingException.class)
	public void testIncompleteStringLiteral() throws ParsingException {
		String input = "p(\"abc) .";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
	}

	@Test
	public void testStringLiteralEscapes() throws ParsingException {
		String input = "p(\"_\\\"_\\\\_\\n_\\t_\") ."; // User input: p("_\"_\\_\n_\t_")
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
		PositiveLiteral fact = Expressions.makePositiveLiteral("p",
				Expressions.makeConstant("\"_\"_\\_\n_\t_\"^^<" + PrefixDeclarations.XSD_STRING + ">"));
		assertEquals(Arrays.asList(fact), ruleParser.getFacts());
	}

	@Test
	public void testStringLiteralAllEscapes() throws ParsingException {
		// User input: p("_\n_\t_\r_\b_\f_\'_\"_\\_")
		String input = "p(\"_\\n_\\t_\\r_\\b_\\f_\\'_\\\"_\\\\_\") .";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
		PositiveLiteral fact = Expressions.makePositiveLiteral("p",
				Expressions.makeConstant("\"_\n_\t_\r_\b_\f_\'_\"_\\_\"^^<" + PrefixDeclarations.XSD_STRING + ">"));
		assertEquals(Arrays.asList(fact), ruleParser.getFacts());
	}

	@Test
	public void testStringLiteralMultiLine() throws ParsingException {
		String input = "p('''line 1\n\n" + "line 2\n" + "line 3''') ."; // User input: p("a\"b\\c")
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
		PositiveLiteral fact = Expressions.makePositiveLiteral("p",
				Expressions.makeConstant("\"line 1\n\nline 2\nline 3\"^^<" + PrefixDeclarations.XSD_STRING + ">"));
		assertEquals(Arrays.asList(fact), ruleParser.getFacts());
	}

	@Test(expected = ParsingException.class)
	public void testIncompleteStringLiteralMultiLine() throws ParsingException {
		String input = "p('''abc\ndef'') .";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
	}

	@Test
	public void testFullLiteral() throws ParsingException {
		String input = "p(\"abc\"^^<http://www.w3.org/2001/XMLSchema#string>) .";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
		assertEquals(Arrays.asList(fact2), ruleParser.getFacts());
	}

	@Test
	public void testUnicodeLiteral() throws ParsingException {
		String input = "p(\"\\u0061\\u0062\\u0063\") ."; // "abc"
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
		assertEquals(Arrays.asList(fact2), ruleParser.getFacts());
	}

	@Test
	public void testUnicodeUri() throws ParsingException {
		String input = "@base <http://example.org/> . @prefix ex: <http://example.org/> .  ex:\\u0073(c) .";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
		assertEquals(Arrays.asList(fact), ruleParser.getFacts());
	}

	@Test
	public void testPrefixedLiteral() throws ParsingException {
		String input = "@prefix xsd: <" + PrefixDeclarations.XSD + "> . " + "p(\"abc\"^^xsd:string) .";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
		assertEquals(Arrays.asList(fact2), ruleParser.getFacts());
	}

	@Test
	public void testLangStringLiteral() throws ParsingException {
		String input = "p(\"abc\"@en-gb) .";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
		PositiveLiteral fact = Expressions.makePositiveLiteral("p", Expressions.makeConstant("\"abc\"@en-gb"));
		assertEquals(Arrays.asList(fact), ruleParser.getFacts());
	}

	@Test
	public void testLineComments() throws ParsingException {
		String input = "@prefix ex: <http://example.org/> . % comment \n" + "%@prefix ex: <http:nourl> \n"
				+ " ex:s(ex:c) . % comment \n";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
		assertEquals(Arrays.asList(fact), ruleParser.getFacts());
	}

	@Test
	public void testPositiveLiteral() throws ParsingException {
		String input = "<http://example.org/p>(?X,<http://example.org/c>)";
		RuleParser ruleParser = new RuleParser();
		Literal literal = ruleParser.parsePositiveLiteral(input);
		assertEquals(atom1, literal);
	}

	@Test(expected = ParsingException.class)
	public void testPositiveLiteralError() throws ParsingException {
		String input = "~ <http://example.org/p>(?X,<http://example.org/c>)";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parsePositiveLiteral(input);
	}

	@Test
	public void testLiteral() throws ParsingException {
		String input = "~ <http://example.org/p>(?X,<http://example.org/c>)";
		RuleParser ruleParser = new RuleParser();
		Literal literal = ruleParser.parseLiteral(input);
		assertEquals(negAtom1, literal);
	}

	@Test(expected = ParsingException.class)
	public void tesLiteralError() throws ParsingException {
		String input = "<http://example.org/p>(?X,<http://example.org/c)";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parseLiteral(input);
	}

	@Test
	public void testCsvSource() throws ParsingException, IOException {
		String input = "@source p(2) : load-csv(\"src/main/data/input/example.csv\") .";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
		CsvFileDataSource csvds = new CsvFileDataSource(new File("src/main/data/input/example.csv"));
		Predicate p = Expressions.makePredicate("p", 2);
		assertEquals(1, ruleParser.getDataSources().size());
		assertEquals(p, ruleParser.getDataSources().get(0).getLeft());
		assertEquals(csvds, ruleParser.getDataSources().get(0).getRight());
	}

	@Test
	public void testRdfSource() throws ParsingException, IOException {
		String input = "@source p(3) : load-rdf(\"src/main/data/input/example.nt.gz\") .";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
		RdfFileDataSource rdfds = new RdfFileDataSource(new File("src/main/data/input/example.nt.gz"));
		Predicate p = Expressions.makePredicate("p", 3);
		assertEquals(1, ruleParser.getDataSources().size());
		assertEquals(p, ruleParser.getDataSources().get(0).getLeft());
		assertEquals(rdfds, ruleParser.getDataSources().get(0).getRight());
	}

	@Test(expected = ParsingException.class)
	public void testRdfSourceInvalidArity() throws ParsingException, IOException {
		String input = "@source p(2) : load-rdf(\"src/main/data/input/example.nt.gz\") .";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
	}

	@Test
	public void testSparqlSource() throws ParsingException, MalformedURLException {
		String input = "@source p(2) : sparql(<https://query.wikidata.org/sparql>,\"disease, doid\",\"?disease wdt:P699 ?doid .\") .";
		RuleParser ruleParser = new RuleParser();
		ruleParser.parse(input);
		SparqlQueryResultDataSource sparqlds = new SparqlQueryResultDataSource(
				new URL("https://query.wikidata.org/sparql"), "disease, doid", "?disease wdt:P699 ?doid .");
		Predicate p = Expressions.makePredicate("p", 2);
		assertEquals(1, ruleParser.getDataSources().size());
		assertEquals(p, ruleParser.getDataSources().get(0).getLeft());
		assertEquals(sparqlds, ruleParser.getDataSources().get(0).getRight());
	}

}
