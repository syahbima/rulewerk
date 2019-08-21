package org.semanticweb.vlog4j.core.reasoner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.semanticweb.vlog4j.core.model.api.DataSource;
import org.semanticweb.vlog4j.core.model.api.DataSourceDeclaration;
import org.semanticweb.vlog4j.core.model.api.Fact;
import org.semanticweb.vlog4j.core.model.api.Literal;
import org.semanticweb.vlog4j.core.model.api.PositiveLiteral;
import org.semanticweb.vlog4j.core.model.api.Predicate;
import org.semanticweb.vlog4j.core.model.api.PrefixDeclarations;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.api.Statement;
import org.semanticweb.vlog4j.core.model.api.StatementVisitor;

/*-
 * #%L
 * VLog4j Core Components
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

/**
 * A knowledge base with rules, facts, and declartions for loading data from
 * further sources. This is a "syntactic" object in that it represents some
 * information that is not relevant for the semantics of reasoning, but that is
 * needed to ensure faithful re-serialisation of knowledge bases loaded from
 * files (e.g., preserving order).
 * 
 * @author Markus Kroetzsch
 *
 */
public class KnowledgeBase {
	
	private final Set<KnowledgeBaseListener> listeners = new HashSet<>();
	
	/**
	 * Auxiliary class to process {@link Statement}s when added to the knowledge
	 * base. Returns true if a statement was added successfully.
	 * 
	 * @author Markus Kroetzsch
	 *
	 */
	class AddStatementVisitor implements StatementVisitor<Boolean> {
		@Override
		public Boolean visit(Fact statement) {
			addFact(statement);
			return true;
		}

		@Override
		public Boolean visit(Rule statement) {
			return true;
		}

		@Override
		public Boolean visit(DataSourceDeclaration statement) {
			dataSourceDeclarations.add(statement);
			return true;
		}
	}

	final AddStatementVisitor addStatementVisitor = new AddStatementVisitor();

	class ExtractStatementsVisitor<T> implements StatementVisitor<Void> {

		final ArrayList<T> extracted = new ArrayList<>();
		final Class<T> ownType;

		ExtractStatementsVisitor(Class<T> type) {
			ownType = type;
		}

		ArrayList<T> getExtractedStatements() {
			return extracted;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Void visit(Fact statement) {
			if (ownType.equals(Fact.class)) {
				extracted.add((T) statement);
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Void visit(Rule statement) {
			if (ownType.equals(Rule.class)) {
				extracted.add((T) statement);
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Void visit(DataSourceDeclaration statement) {
			if (ownType.equals(DataSourceDeclaration.class)) {
				extracted.add((T) statement);
			}
			return null;
		}
	}

	/**
	 * The primary storage for the contents of the knowledge base.
	 */
	final LinkedHashSet<Statement> statements = new LinkedHashSet<>();
	
	/**
	 * Known prefixes that can be used to pretty-print the contents of the knowledge
	 * base. We try to preserve user-provided prefixes found in files when loading
	 * data.
	 */
	PrefixDeclarations prefixDeclarations;

	/**
	 * Index structure that organises all facts by their predicate.
	 */
	final Map<Predicate, Set<PositiveLiteral>> factsByPredicate = new HashMap<>();
	
	/**
	 * Index structure that holds all data source declarations of this knowledge
	 * base.
	 */
	final Set<DataSourceDeclaration> dataSourceDeclarations = new HashSet<>();
	
	/**
	 * Registers a listener for changes on the knowledge base
	 * @param listener
	 */
	public void addListener(KnowledgeBaseListener listener) {
		this.listeners.add(listener);
	}
	
	/**
	 * Unregisters given listener from changes on the knowledge base
	 * @param listener
	 */
	public void deleteListener(KnowledgeBaseListener listener) {
		this.listeners.remove(listener);
		
	}

	/**
	 * Adds a single statement to the knowledge base.
	 * @return true, if the knowledge base has changed.
	 * @param statement
	 */
	public boolean addStatement(Statement statement) {
		Validate.notNull(statement, "Statement cannot be Null.");
		if (!this.statements.contains(statement) && statement.accept(this.addStatementVisitor)) {
			this.statements.add(statement);
			
			notifyListenersOnStatementAdded(statement);
			
			return true;
		}
		return false;
	}

	/**
	 * Adds a collection of statements to the knowledge base.
	 * 
	 * @param statements
	 */
	public void addStatements(Collection<? extends Statement> statements) {
		final Set<Statement> addedStatements = new HashSet<>();
		
		for (final Statement statement : statements) {
			if (addStatement(statement)) {
				addedStatements.add(statement);
			}
		}
		
		notifyListenersOnStatementsAdded(addedStatements);
		
	}

	/**
	 * Adds a list of statements to the knowledge base.
	 * 
	 * @param statements
	 */
	public void addStatements(Statement... statements) {
		final Set<Statement> addedStatements = new HashSet<>();
		
		for (final Statement statement : statements) {
			if (addStatement(statement)) {
				addedStatements.add(statement);
			}
		}
		
		notifyListenersOnStatementsAdded(addedStatements);
	}

	private void notifyListenersOnStatementsAdded(final Set<Statement> addedStatements) {
		for (final KnowledgeBaseListener listener : this.listeners) {
			listener.onStatementsAdded(addedStatements);
		}
	}
	
	private void notifyListenersOnStatementAdded(final Statement addedStatements) {
		for (final KnowledgeBaseListener listener : this.listeners) {
			listener.onStatementAdded(addedStatements);
		}
	}

	/**
	 * Get the list of all rules that have been added to the knowledge base. The
	 * list is read-only and cannot be modified to add or delete rules.
	 * 
	 * @return list of {@link Rule}s
	 */
	public List<Rule> getRules() {
		return getStatementsByType(Rule.class);
	}

	/**
	 * Get the list of all facts that have been added to the knowledge base. The
	 * list is read-only and cannot be modified to add or delete facts.
	 * 
	 * @return list of {@link Fact}s
	 */
	public List<Fact> getFacts() {
		return getStatementsByType(Fact.class);
	}

	/**
	 * Get the list of all data source declarations that have been added to the
	 * knowledge base. The list is read-only and cannot be modified to add or delete
	 * facts.
	 * 
	 * @return list of {@link DataSourceDeclaration}s
	 */
	public List<DataSourceDeclaration> getDataSourceDeclarations() {
		return getStatementsByType(DataSourceDeclaration.class);
	}

	<T> List<T> getStatementsByType(Class<T> type) {
		final ExtractStatementsVisitor<T> visitor = new ExtractStatementsVisitor<>(type);
		for (final Statement statement : statements) {
			statement.accept(visitor);
		}
		return Collections.unmodifiableList(visitor.getExtractedStatements());
	}

	/**
	 * Add a single fact to the internal data structures. It is assumed that it has
	 * already been checked that this fact is not present yet.
	 * 
	 * @param fact the fact to add
	 */
	void addFact(Fact fact) {
		final Predicate predicate = fact.getPredicate();
		this.factsByPredicate.putIfAbsent(predicate, new HashSet<>());
		this.factsByPredicate.get(predicate).add(fact);
	}

	@Deprecated
	public boolean hasFacts() {
		// If needed, a more elegant implementation should be used
		return !this.getFacts().isEmpty() || !this.getDataSourceDeclarations().isEmpty();
	}

	@Deprecated
	public Map<Predicate, DataSource> getDataSourceForPredicate() {
		// Only for temporary functionality; the one-source-per-predicate model will be
		// retired and is no longer enforced in the knowledge base
		final Map<Predicate, DataSource> result = new HashMap<>();
		for (final DataSourceDeclaration dsd : getDataSourceDeclarations()) {
			result.put(dsd.getPredicate(), dsd.getDataSource());
		}
		return result;
	}

	@Deprecated
	public Map<Predicate, Set<PositiveLiteral>> getFactsForPredicate() {
		// Check if this is really the best format to access this data
		return this.factsByPredicate;
	}

	@Deprecated
	public Set<Predicate> getEdbPredicates() {
		// TODO use cache
		return collectEdbPredicates();
	}

	@Deprecated
	public Set<Predicate> getIdbPredicates() {
		// TODO use cache
		return collectIdbPredicates();
	}

	Set<Predicate> collectEdbPredicates() {
		// not an efficient or elegant implementation
		final Set<Predicate> edbPredicates = new HashSet<>();
		edbPredicates.addAll(this.getDataSourceForPredicate().keySet());
		edbPredicates.addAll(this.factsByPredicate.keySet());
		return edbPredicates;
	}

	Set<Predicate> collectIdbPredicates() {
		final Set<Predicate> idbPredicates = new HashSet<>();
		for (final Rule rule : this.getRules()) {
			for (final Literal headAtom : rule.getHead()) {
				idbPredicates.add(headAtom.getPredicate());
			}
		}
		return idbPredicates;
	}

	/**
	 * Returns all {@link Statement}s of this knowledge base.
	 * 
	 * The result can be iterated over and will return statements in the original
	 * order.
	 * 
	 * @return a collection of statements
	 */
	public Collection<Statement> getStatements() {
		return this.statements;
	}
	
}
