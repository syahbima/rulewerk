package org.semanticweb.vlog4j.core.model;

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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.internal.util.collections.Sets;
import org.semanticweb.vlog4j.core.model.api.Conjunction;
import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.api.Literal;
import org.semanticweb.vlog4j.core.model.api.NegativeLiteral;
import org.semanticweb.vlog4j.core.model.api.PositiveLiteral;
import org.semanticweb.vlog4j.core.model.api.TermType;
import org.semanticweb.vlog4j.core.model.api.Variable;
import org.semanticweb.vlog4j.core.model.implementation.ConjunctionImpl;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;

public class ConjunctionImplTest {

	@Test
	public void testGettersPositiveLiterals() {
		final Variable x = Expressions.makeVariable("X");
		final Variable y = Expressions.makeVariable("Y");
		final Constant c = Expressions.makeConstant("c");
		final Constant d = Expressions.makeConstant("d");
		final PositiveLiteral positiveLiteral1 = Expressions.makePositiveLiteral("p", x, c);
		final PositiveLiteral positiveLiteral2 = Expressions.makePositiveLiteral("p", y, x);
		final PositiveLiteral positiveLiteral3 = Expressions.makePositiveLiteral("q", x, d);
		final List<PositiveLiteral> positiveLiteralList = Arrays.asList(positiveLiteral1, positiveLiteral2,
				positiveLiteral3);

		final List<Literal> literalList = Arrays.asList(positiveLiteral1, positiveLiteral2, positiveLiteral3);

		final Conjunction<PositiveLiteral> conjunctionPositiveLiterals = new ConjunctionImpl<>(positiveLiteralList);
		final Conjunction<Literal> conjunctionLiterals = new ConjunctionImpl<>(literalList);

		assertEquals(positiveLiteralList, conjunctionPositiveLiterals.getLiterals());
		assertEquals(literalList, conjunctionPositiveLiterals.getLiterals());
		assertEquals(conjunctionLiterals, conjunctionPositiveLiterals);

		assertEquals(positiveLiteralList, conjunctionPositiveLiterals.getLiterals());
		assertEquals(Sets.newSet(x, y), conjunctionPositiveLiterals.getVariables());
		assertEquals(Sets.newSet(x, y), conjunctionPositiveLiterals.getTerms(TermType.VARIABLE));
		assertEquals(Sets.newSet(), conjunctionPositiveLiterals.getTerms(TermType.BLANK));
		assertEquals(Sets.newSet(c, d), conjunctionPositiveLiterals.getTerms(TermType.CONSTANT));
	}

	@Test
	public void testGettersLiterals() {
		final Variable x = Expressions.makeVariable("X");
		final Variable y = Expressions.makeVariable("Y");
		final Constant c = Expressions.makeConstant("c");
		final Constant d = Expressions.makeConstant("d");
		final Literal positiveLiteral1 = Expressions.makePositiveLiteral("p", x, c);
		final NegativeLiteral negativeLiteral2 = Expressions.makeNegativeLiteral("p", y, x);
		final Literal positiveLiteral3 = Expressions.makePositiveLiteral("q", x, d);
		final Literal negativeLiteral4 = Expressions.makePositiveLiteral("q", y, d);
		final List<Literal> literalList = Arrays.asList(positiveLiteral1, negativeLiteral2, positiveLiteral3,
				negativeLiteral4);

		final Conjunction<Literal> conjunction = new ConjunctionImpl<>(literalList);

		assertEquals(literalList, conjunction.getLiterals());
		assertEquals(Sets.newSet(x, y), conjunction.getVariables());
		assertEquals(Sets.newSet(x, y), conjunction.getTerms(TermType.VARIABLE));
		assertEquals(Sets.newSet(), conjunction.getTerms(TermType.BLANK));
		assertEquals(Sets.newSet(c, d), conjunction.getTerms(TermType.CONSTANT));
	}

	@Test
	public void testGettersNegativeLiterals() {
		final Variable x = Expressions.makeVariable("X");
		final Variable y = Expressions.makeVariable("Y");
		final Constant c = Expressions.makeConstant("c");
		final Constant d = Expressions.makeConstant("d");
		final NegativeLiteral negativeLiteral1 = Expressions.makeNegativeLiteral("p", x, c);
		final NegativeLiteral negativeLiteral2 = Expressions.makeNegativeLiteral("p", y, x);
		final NegativeLiteral negativeLiteral3 = Expressions.makeNegativeLiteral("q", x, d);
		final List<NegativeLiteral> negativeLiteralList = Arrays.asList(negativeLiteral1, negativeLiteral2,
				negativeLiteral3);

		final List<Literal> literalList = Arrays.asList(negativeLiteral1, negativeLiteral2, negativeLiteral3);

		final Conjunction<NegativeLiteral> conjunctionNegativeLiterals = new ConjunctionImpl<>(negativeLiteralList);
		final Conjunction<Literal> conjunctionLiterals = new ConjunctionImpl<>(literalList);

		assertEquals(negativeLiteralList, conjunctionNegativeLiterals.getLiterals());
		assertEquals(literalList, conjunctionNegativeLiterals.getLiterals());
		assertEquals(conjunctionLiterals, conjunctionNegativeLiterals);

		assertEquals(Sets.newSet(x, y), conjunctionNegativeLiterals.getVariables());
		assertEquals(Sets.newSet(x, y), conjunctionNegativeLiterals.getTerms(TermType.VARIABLE));
		assertEquals(Sets.newSet(), conjunctionNegativeLiterals.getTerms(TermType.BLANK));
		assertEquals(Sets.newSet(c, d), conjunctionNegativeLiterals.getTerms(TermType.CONSTANT));
	}

	@Test
	public void testEqualsPositiveLiterals() {
		final Variable x = Expressions.makeVariable("X");
		final Variable y = Expressions.makeVariable("Y");
		final Constant c = Expressions.makeConstant("c");
		final Constant d = Expressions.makeConstant("d");
		final PositiveLiteral positiveLiteral1 = Expressions.makePositiveLiteral("p", x, c);
		final PositiveLiteral positiveLiteral2 = Expressions.makePositiveLiteral("p", y, x);
		final PositiveLiteral positiveLiteral3 = Expressions.makePositiveLiteral("q", x, d);
		final List<PositiveLiteral> positiveLiteralList = Arrays.asList(positiveLiteral1, positiveLiteral2,
				positiveLiteral3);
		final Conjunction<PositiveLiteral> conjunction1 = new ConjunctionImpl<>(positiveLiteralList);
		final Conjunction<PositiveLiteral> conjunction2 = Expressions.makePositiveConjunction(positiveLiteral1,
				positiveLiteral2, positiveLiteral3);
		final Conjunction<Literal> conjunction3 = Expressions.makeConjunction(positiveLiteral1, positiveLiteral2,
				positiveLiteral3);
		final Conjunction<PositiveLiteral> conjunction4 = Expressions.makePositiveConjunction(positiveLiteral1,
				positiveLiteral3, positiveLiteral2);

		assertEquals(conjunction1, conjunction1);
		assertEquals(conjunction2, conjunction1);
		assertEquals(conjunction3, conjunction1);
		assertEquals(conjunction2.hashCode(), conjunction1.hashCode());
		assertEquals(conjunction3.hashCode(), conjunction1.hashCode());
		assertNotEquals(conjunction4, conjunction1);
		assertNotEquals(conjunction4.hashCode(), conjunction1.hashCode());
		assertFalse(conjunction1.equals(null));
		assertFalse(conjunction1.equals(c));
	}

	@Test
	public void testEqualsNegativeLiterals() {
		final Variable x = Expressions.makeVariable("X");
		final Variable y = Expressions.makeVariable("Y");
		final Constant c = Expressions.makeConstant("c");
		final Constant d = Expressions.makeConstant("d");
		final NegativeLiteral negativeLiteral1 = Expressions.makeNegativeLiteral("p", x, c);
		final NegativeLiteral negativeLiteral2 = Expressions.makeNegativeLiteral("p", y, x);
		final NegativeLiteral negativeLiteral3 = Expressions.makeNegativeLiteral("q", x, d);
		final List<NegativeLiteral> negativeLiteralList = Arrays.asList(negativeLiteral1, negativeLiteral2,
				negativeLiteral3);
		final Conjunction<NegativeLiteral> conjunction1 = new ConjunctionImpl<>(negativeLiteralList);
		final Conjunction<Literal> conjunction2 = Expressions.makeConjunction(negativeLiteral1, negativeLiteral2,
				negativeLiteral3);
		final Conjunction<Literal> conjunction3 = Expressions.makeConjunction(negativeLiteral1, negativeLiteral3,
				negativeLiteral2);

		assertEquals(conjunction1, conjunction1);
		assertEquals(conjunction2, conjunction1);
		assertEquals(conjunction2.hashCode(), conjunction1.hashCode());
		assertNotEquals(conjunction3, conjunction1);
		assertNotEquals(conjunction3.hashCode(), conjunction1.hashCode());
		assertFalse(conjunction1.equals(null));
		assertFalse(conjunction1.equals(c));
	}

	@Test
	public void testEqualsLiterals() {
		final Variable x = Expressions.makeVariable("X");
		final Constant c = Expressions.makeConstant("c");

		final PositiveLiteral positiveLiteral1 = Expressions.makePositiveLiteral("p", x, c);
		final NegativeLiteral negativeLiteral1 = Expressions.makeNegativeLiteral("p", x, c);
		final ConjunctionImpl<Literal> conjunction1 = new ConjunctionImpl<>(
				Arrays.asList(positiveLiteral1, negativeLiteral1));

		final Literal positiveLiteral2 = Expressions.makePositiveLiteral("p", x, c);
		final Literal negativeLiteral2 = Expressions.makeNegativeLiteral("p", x, c);
		final ConjunctionImpl<Literal> conjunction2 = new ConjunctionImpl<>(
				Arrays.asList(positiveLiteral2, negativeLiteral2));

		assertEquals(conjunction1, conjunction1);
		assertEquals(conjunction2, conjunction1);
		assertEquals(conjunction2.hashCode(), conjunction1.hashCode());

	}

	@Test(expected = NullPointerException.class)
	public void literalsNotNull() {
		new ConjunctionImpl<Literal>(null);
	}

	@Test(expected = NullPointerException.class)
	public void positiveLiteralsNotNull() {
		new ConjunctionImpl<PositiveLiteral>(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void positiveLiteralsNoNullElements() {
		final Variable x = Expressions.makeVariable("X");
		final PositiveLiteral positiveLiteral = Expressions.makePositiveLiteral("p", x);
		final List<PositiveLiteral> positiveLiteralList = Arrays.asList(positiveLiteral, null);
		Expressions.makeConjunction(positiveLiteralList);
	}

	@Test(expected = IllegalArgumentException.class)
	public void literalsNoNullElements() {
		final Variable x = Expressions.makeVariable("X");
		final NegativeLiteral negativeLiteral = Expressions.makeNegativeLiteral("p", x);
		final PositiveLiteral positiveLiteral = Expressions.makePositiveLiteral("p", x);
		final List<Literal> literalList = Arrays.asList(negativeLiteral, positiveLiteral, null);
		Expressions.makeConjunction(literalList);
	}

	@Test(expected = NullPointerException.class)
	public void negativeLiteralsNotNull() {
		new ConjunctionImpl<NegativeLiteral>(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void negativeLiteralsNoNullElements() {
		final Variable x = Expressions.makeVariable("X");
		final NegativeLiteral negativeLiteral = Expressions.makeNegativeLiteral("p", x);
		final List<NegativeLiteral> negativeLiteralList = Arrays.asList(negativeLiteral, null);
		Expressions.makeConjunction(negativeLiteralList);
	}

}