package org.semanticweb.vlog4j.core.reasoner.implementation;

import static org.junit.Assert.assertEquals;

/*-
 * #%L
 * VLog4j Core Components
 * %%
 * Copyright (C) 2018 VLog4j Developers
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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.vlog4j.core.exceptions.EdbIdbSeparationException;
import org.semanticweb.vlog4j.core.exceptions.IncompatiblePredicateArityException;
import org.semanticweb.vlog4j.core.exceptions.ReasonerStateException;
import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.api.PositiveLiteral;
import org.semanticweb.vlog4j.core.model.api.Predicate;
import org.semanticweb.vlog4j.core.model.api.Term;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import org.semanticweb.vlog4j.core.reasoner.DataSource;
import org.semanticweb.vlog4j.core.reasoner.KnowledgeBase;
import org.semanticweb.vlog4j.core.reasoner.exceptions.EdbIdbSeparationException;
import org.semanticweb.vlog4j.core.reasoner.exceptions.IncompatiblePredicateArityException;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;


import karmaresearch.vlog.EDBConfigurationException;

public class AddDataSourceTest {

	private static final String CSV_FILE_PATH = FileDataSourceTestUtils.INPUT_FOLDER + "unaryFacts.csv";

	@Test
	public void testAddDataSourceExistentDataForDifferentPredicates() throws ReasonerStateException,
			EdbIdbSeparationException, EDBConfigurationException, IOException, IncompatiblePredicateArityException {
		final Predicate predicateParity1 = Expressions.makePredicate("p", 1);
		final Constant constantA = Expressions.makeConstant("a");
		final PositiveLiteral factPredicatePArity2 = Expressions.makePositiveLiteral("p", constantA, constantA);
		final PositiveLiteral factPredicateQArity1 = Expressions.makePositiveLiteral("q", constantA);
		final Predicate predicateLArity1 = Expressions.makePredicate("l", 1);
		final DataSource dataSource = new CsvFileDataSource(new File(CSV_FILE_PATH));

		final VLogKnowledgeBase kb = new VLogKnowledgeBase();
		kb.addFacts(factPredicatePArity2, factPredicateQArity1);
		kb.addFactsFromDataSource(predicateLArity1, dataSource);
		kb.addFactsFromDataSource(predicateParity1, dataSource);

		try (final VLogReasoner reasoner = new VLogReasoner(kb)) {
			reasoner.load();
			reasoner.reason();
			final QueryResultIterator queryResultIteratorL1 = reasoner.answerQuery(
					Expressions.makePositiveLiteral(predicateLArity1, Expressions.makeVariable("x")), false);
			final Set<List<Term>> queryResultsL1 = QueryResultsUtils.collectQueryResults(queryResultIteratorL1);

			final QueryResultIterator queryResultIteratorP1 = reasoner.answerQuery(
					Expressions.makePositiveLiteral(predicateParity1, Expressions.makeVariable("x")), false);
			final Set<List<Term>> queryResultsP1 = QueryResultsUtils.collectQueryResults(queryResultIteratorP1);
			assertEquals(queryResultsL1, queryResultsP1);

		}
	}

	@Test
	public void testAddDataSourceBeforeLoading() throws ReasonerStateException, EdbIdbSeparationException,
			EDBConfigurationException, IOException, IncompatiblePredicateArityException {
		final Predicate predicateP = Expressions.makePredicate("p", 1);
		final Predicate predicateQ = Expressions.makePredicate("q", 1);
		final DataSource dataSource = new CsvFileDataSource(new File(CSV_FILE_PATH));

		final VLogKnowledgeBase kb = new VLogKnowledgeBase();

		try (final VLogReasoner reasoner = new VLogReasoner(kb)) {
			kb.addFactsFromDataSource(predicateP, dataSource);
			kb.addFactsFromDataSource(predicateQ, dataSource);
			reasoner.load();
		}
	}

	// TODO rewrite test
	@Ignore
	@Test(expected = ReasonerStateException.class)
	public void testAddDataSourceAfterLoading() throws ReasonerStateException, EdbIdbSeparationException,
			EDBConfigurationException, IOException, IncompatiblePredicateArityException {
		final Predicate predicateP = Expressions.makePredicate("p", 1);
		final Predicate predicateQ = Expressions.makePredicate("q", 1);
		final DataSource dataSource = new CsvFileDataSource(new File(CSV_FILE_PATH));

		final VLogKnowledgeBase kb = new VLogKnowledgeBase();

		try (final VLogReasoner reasoner = new VLogReasoner(kb)) {
			kb.addFactsFromDataSource(predicateP, dataSource);
			reasoner.load();
			kb.addFactsFromDataSource(predicateQ, dataSource);
		}
	}

	// TODO rewrite test
	@Ignore
	@Test(expected = ReasonerStateException.class)
	public void testAddDataSourceAfterReasoning() throws ReasonerStateException, EdbIdbSeparationException,
			EDBConfigurationException, IOException, IncompatiblePredicateArityException {
		final Predicate predicateP = Expressions.makePredicate("p", 1);
		final Predicate predicateQ = Expressions.makePredicate("q", 1);
		final DataSource dataSource = new CsvFileDataSource(new File(CSV_FILE_PATH));

		final VLogKnowledgeBase kb = new VLogKnowledgeBase();

		try (final VLogReasoner reasoner = new VLogReasoner(kb)) {
			kb.addFactsFromDataSource(predicateP, dataSource);
			reasoner.load();
			reasoner.reason();
			kb.addFactsFromDataSource(predicateQ, dataSource);
		}
	}

	// TODO move to a test class for VLogKnowledgeBase
	@Test(expected = IllegalArgumentException.class)
	public void testAddDataSourceNoMultipleDataSourcesForPredicate() throws ReasonerStateException, IOException {
		final Predicate predicate = Expressions.makePredicate("p", 1);
		final DataSource dataSource = new CsvFileDataSource(new File(CSV_FILE_PATH));

		final VLogKnowledgeBase kb = new VLogKnowledgeBase();
		kb.addFactsFromDataSource(predicate, dataSource);
		kb.addFactsFromDataSource(predicate, dataSource);
	}

	// TODO move to a test class for VLogKnowledgeBase
	@Test(expected = IllegalArgumentException.class)
	public void testAddDataSourceNoFactsForPredicate() throws ReasonerStateException, IOException {
		final Predicate predicate = Expressions.makePredicate("p", 1);
		final DataSource dataSource = new CsvFileDataSource(new File(CSV_FILE_PATH));
		final PositiveLiteral fact = Expressions.makePositiveLiteral(Expressions.makePredicate("p", 1),
				Expressions.makeConstant("a"));

		final VLogKnowledgeBase kb = new VLogKnowledgeBase();
		kb.addFacts(fact);
		kb.addFactsFromDataSource(predicate, dataSource);
	}

	// TODO move to a test class for VLogKnowledgeBase
	@Test(expected = NullPointerException.class)
	public void testAddDataSourcePredicateNotNull() throws ReasonerStateException, IOException {
		final DataSource dataSource = new CsvFileDataSource(new File(CSV_FILE_PATH));

		final VLogKnowledgeBase kb = new VLogKnowledgeBase();
		kb.addFactsFromDataSource(null, dataSource);
	}

	// TODO move to a test class for VLogKnowledgeBase
	@Test(expected = NullPointerException.class)
	public void testAddDataSourceNotNullDataSource() throws ReasonerStateException {
		final Predicate predicate = Expressions.makePredicate("p", 1);
		
		final KnowledgeBase kb = new VLogKnowledgeBase();
		kb.addFactsFromDataSource(predicate, null);
	}

}
