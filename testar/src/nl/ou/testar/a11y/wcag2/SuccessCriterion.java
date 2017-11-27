/*****************************************************************************************
 *                                                                                       *
 * COPYRIGHT (2017):                                                                     *
 * Universitat Politecnica de Valencia                                                   *
 * Camino de Vera, s/n                                                                   *
 * 46022 Valencia, Spain                                                                 *
 * www.upv.es                                                                            *
 *                                                                                       * 
 * D I S C L A I M E R:                                                                  *
 * This software has been developed by the Universitat Politecnica de Valencia (UPV)     *
 * in the context of the TESTAR Proof of Concept project:                                *
 *               "UPV, Programa de Prueba de Concepto 2014, SP20141402"                  *
 * This sample is distributed FREE of charge under the TESTAR license, as an open        *
 * source project under the BSD3 licence (http://opensource.org/licenses/BSD-3-Clause)   *                                                                                        * 
 *                                                                                       *
 *****************************************************************************************/

package nl.ou.testar.a11y.wcag2;

import org.fruit.Assert;
import org.fruit.alayer.Verdict;

/**
 * A WCAG success criterion
 * @author Davy Kager
 *
 */
public final class SuccessCriterion extends ItemBase {
	
	private static final long serialVersionUID = 2042413918878837966L;

	/**
	 * WCAG success criterion conformance levels
	 */
	public enum Level {
		/**
		 * Level A: minimum conformance / highest priority guidelines.
		 */
		A,
		
		/**
		 * Level AA: medium priority guidelines.
		 */
		AA,
		
		/**
		 * Level AAA: maximum conformance / lowest priority guidelines.
		 */
		AAA;
	}
	
	/**
	 * The base for building anchor URLs of success criteria
	 */
	public static final String URL_BASE =
			"https://www.w3.org/WAI/GL/2013/WD-wcag2ict-20130905/accordion#";
	
	private static final int NLEVELS = 3;
	private static final double SEVERITY_STEP = (Verdict.SEVERITY_MAX - Verdict.SEVERITY_MIN) / NLEVELS;
	
	private final Level level;
	private final String urlSuffix;
	
	/**
	 * Constructs a new success criterion
	 * @param nr The number of the success criterion.
	 * @param name The name (short description) of the success criterion.
	 * @param parent The guideline (parent) this success criterion belongs to.
	 * @param level The level of the success criterion.
	 * @param urlSuffix The anchor URL suffix on the W3C website.
	 */
	SuccessCriterion(int nr, String name, AbstractGuideline parent, Level level, String urlSuffix) {
		super(nr, name, Assert.notNull(parent));
		Assert.notNull(level);
		Assert.hasText(urlSuffix);
		this.level = level;
		this.urlSuffix = urlSuffix;
	}
	
	/**
	 * Gets the conformance level of this success criterion
	 * @return The conformance level.
	 */
	public Level getLevel() {
		return level;
	}
	
	/**
	 * Gets the anchor URL suffix of this success criterion
	 * @return The URL suffix.
	 */
	public String getURLSuffix() {
		return urlSuffix;
	}
	
	/**
	 * Gets the severity of violating this success criterion as used in verdicts
	 * The severity depends on the conformance level of the success criterion.
	 * A low level (A) corresponds to a high severity
	 * and a high level (AAA) to a low severity.
	 * @return The severity.
	 */
	public double getVerdictSeverity() {
		return Verdict.SEVERITY_MAX - (level.ordinal() * SEVERITY_STEP);
	}
	
}