package org.semanticweb.rulewerk.parser.datasources;

/*-
 * #%L
 * Rulewerk Parser
 * %%
 * Copyright (C) 2018 - 2020 Rulewerk Developers
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

import java.net.URL;
import java.util.List;

import org.semanticweb.rulewerk.core.model.api.DataSource;
import org.semanticweb.rulewerk.core.reasoner.implementation.SparqlQueryResultDataSource;
import org.semanticweb.rulewerk.parser.DataSourceDeclarationHandler;
import org.semanticweb.rulewerk.parser.DirectiveArgument;
import org.semanticweb.rulewerk.parser.DirectiveHandler;
import org.semanticweb.rulewerk.parser.ParsingException;
import org.semanticweb.rulewerk.parser.javacc.SubParserFactory;

/**
 * Handler for parsing {@link SparqlQueryResultDataSource} declarations
 *
 * @author Maximilian Marx
 */
public class SparqlQueryResultDataSourceDeclarationHandler implements DataSourceDeclarationHandler {
	@Override
	public DataSource handleDirective(List<DirectiveArgument> arguments, final SubParserFactory subParserFactory)
			throws ParsingException {
		DirectiveHandler.validateNumberOfArguments(arguments, 3);
		URL endpoint = DirectiveHandler.validateUrlArgument(arguments.get(0), "SPARQL endpoint");
		String variables = DirectiveHandler.validateStringArgument(arguments.get(1), "variables list");
		String query = DirectiveHandler.validateStringArgument(arguments.get(2), "query fragment");

		return new SparqlQueryResultDataSource(endpoint, variables, query);
	}
}
