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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fruit.Assert;
import org.fruit.alayer.Action;
import org.fruit.alayer.Widget;

import nl.ou.testar.GraphDB;
import nl.ou.testar.a11y.protocols.Evaluator;
import nl.ou.testar.a11y.reporting.EvaluationResults;

/**
 * An abstract WCAG guideline
 * Subclasses implement specific guideline behavior.
 * @author Davy Kager
 *
 */
public abstract class AbstractGuideline extends ItemBase implements Evaluator {
	
	private static final long serialVersionUID = -2941524827644792263L;
	/**
	 * The list of all the success criteria in this guideline
	 */
	protected final List<SuccessCriterion> criteria = new ArrayList<>();
	
	/**
	 * Constructs a new guideline
	 * @param nr The number of the guideline.
	 * @param name The name of the guideline.
	 * @param parent The principle (parent) this guideline belongs to.
	 */
	protected AbstractGuideline(int nr, String name, AbstractPrinciple parent) {
		super(nr, name, Assert.notNull(parent));
	}
	
	/**
	 * Gets all success criteria in this guideline
	 * @return The list of success criteria.
	 */
	public List<SuccessCriterion> getSuccessCriteria() {
		return Collections.unmodifiableList(criteria);
	}
	
	/**
	 * Gets a success criterion by its name (case-insensitive)
	 * @param name The name.
	 * @return The success criterion, or null if not found.
	 */
	protected SuccessCriterion getSuccessCriterionByName(String name) {
		Assert.notNull(name);
		for (SuccessCriterion criterion : criteria)
			if (criterion.getName().equalsIgnoreCase(name))
				return criterion;
		return null;
	}
	
	/**
	 * Evaluates the accessibility of the given state
	 * This will include zero or more evaluation results for each success criterion in this guideline.
	 * This method executes oracles in state analysis.
	 * @param widgets The widgets to consider.
	 * @return The results of the evaluation.
	 */
	@Override
	public EvaluationResults evaluate(List<Widget> widgets) {
		return new EvaluationResults();
	}
	
	/**
	 * Derives the possible actions from the given state
	 * This will include zero or more actions for each success criterion in this guideline.
	 * The actions are specific to accessibility.
	 * This method derives actions in state analysis.
	 * @param widgets The widgets to consider.
	 * @return The set of actions.
	 */
	@Override
	public Set<Action> deriveActions(List<Widget> widgets) {
		return new HashSet<>();
	}
	
	/**
	 * Evaluates the overall accessibility of the SUT by querying the given graph database
	 * This will include zero or more evaluation results for each success criterion in this guideline.
	 * This method executes oracles in offline analysis.
	 * @param graphDB The graph database.
	 * @return The results of the evaluation.
	 */
	@Override
	public EvaluationResults query(GraphDB graphDB) {
		return new EvaluationResults();
	}
	
	@Override
	public String getImplementationVersion() {
		return "WCAG2ICT-guideline-" + WCAG2ICT.VERSION;
	}
	
	/**
	 * Constructs a new EvaluationResult indicating that the given success criterion was passed
	 * @param criterion The success criterion.
	 * @return A new EvaluationResult.
	 */
	protected WCAG2EvaluationResult evaluationPassed(SuccessCriterion criterion) {
		return new WCAG2EvaluationResult(criterion, WCAG2EvaluationResult.Type.OK,
				"Evaluation passed");
	}
	
}