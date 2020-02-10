package org.semanticweb.vlog4j.parser;

/*-
 * #%L
 * vlog4j-parser
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

import org.semanticweb.vlog4j.core.model.implementation.Serializer;
import org.semanticweb.vlog4j.parser.datasources.CsvFileDataSourceDeclarationHandler;
import org.semanticweb.vlog4j.parser.datasources.RdfFileDataSourceDeclarationHandler;
import org.semanticweb.vlog4j.parser.datasources.SparqlQueryResultDataSourceDeclarationHandler;
import org.semanticweb.vlog4j.parser.directives.ImportFileDirectiveHandler;
import org.semanticweb.vlog4j.parser.directives.ImportFileRelativeDirectiveHandler;

/**
 * Default parser configuration. Registers default data sources.
 *
 * @author Maximilian Marx
 */
public class DefaultParserConfiguration extends ParserConfiguration {
	public DefaultParserConfiguration() {
		super();
		registerDefaultDataSources();
		registerDefaultDirectives();
	}

	/**
	 * Register built-in data sources (currently CSV, RDF, SPARQL).
	 */
	private void registerDefaultDataSources() {
		registerDataSource(Serializer.CSV_FILE_DATA_SOURCE, new CsvFileDataSourceDeclarationHandler());
		registerDataSource(Serializer.RDF_FILE_DATA_SOURCE, new RdfFileDataSourceDeclarationHandler());
		registerDataSource(Serializer.SPARQL_QUERY_RESULT_DATA_SOURCE,
				new SparqlQueryResultDataSourceDeclarationHandler());
	}

	private void registerDefaultDirectives() {
		registerDirective("import", new ImportFileDirectiveHandler());
		registerDirective("import-relative", new ImportFileRelativeDirectiveHandler());
	}
}
