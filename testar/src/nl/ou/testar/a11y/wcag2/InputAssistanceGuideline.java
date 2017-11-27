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

import nl.ou.testar.a11y.wcag2.SuccessCriterion.Level;

/**
 * A WCAG 2.0 guideline
 * @author Davy Kager
 *
 */
public final class InputAssistanceGuideline extends AbstractGuideline {

	private static final long serialVersionUID = -3766907198803540591L;

	InputAssistanceGuideline(AbstractPrinciple parent) {
		super(3, "Input Assistance", parent);
		criteria.add(new SuccessCriterion(1, "Error Identification",
				this, Level.A, "minimize-error-identified"));
		criteria.add(new SuccessCriterion(2, "Labels or Instructions",
				this, Level.A, "minimize-error-cues"));
		criteria.add(new SuccessCriterion(3, "Error Suggestion",
				this, Level.AA, "minimize-error-suggestions"));
		criteria.add(new SuccessCriterion(4, "Error Prevention (Legal, Financial, Data)",
				this, Level.AA, "minimize-error-reversible"));
	}

}