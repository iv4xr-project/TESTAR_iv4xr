
package eu.testar.iv4xr.emotions;

import java.time.LocalDateTime;
import java.util.Vector;

import eu.testar.iv4xr.LabRecruitsAgentTESTAR;
import helperclasses.datastructures.Vec3;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;

public class EmotionalCritic {

	// The value that defines the value interval for the PAD dimensions: [-limit, limit]
	private double limit = 5;

	// The initialization of the PAD dimensions
	private double pleasure = 0.0;
	private double dominance = 0.0;
	private double arousal = 0.0;

	// The tick counters for each dimension, recording how many ticks passed since the dimension was last altered
	private int Pleasure_ticker = 0;
	private int Dominance_ticker = 0;
	private int Arousal_ticker = 0;

	// The maximum 'ticks without alteration' for each dimension
	private int Pleasure_treshold = 10;
	private int Dominance_treshold = 10;
	private int Arousal_treshold = 10;

	// The number of known entities
	public int known_entities = 0;

	public Vector<Vector<Double>> PDA_vector = new Vector<Vector<Double>>();

	public Vector<LocalDateTime> time_stamps = new Vector<LocalDateTime>();

	public Vector<Vec3> position_stamps = new Vector<Vec3>();

	public LabRecruitsAgentTESTAR agent;

	private GoalStructure last_assessed_goal = null;

	private int goal_success = 0;

	public EmotionalCritic(LabRecruitsAgentTESTAR agent) {
		this.agent = agent;
	}

	public void update() {
		this.Pleasure_ticker++; 
		this.Dominance_ticker++;
		this.Arousal_ticker++;

		var last_goal = this.agent.getCurrentGoal();
		if(last_goal == null) {
			System.out.println("No goal!");
		}
		else {
			System.out.println("Status:" + last_goal.getStatus());
		}

		//Assessing changes in Pleasure
		if ((last_goal == this.last_assessed_goal || this.last_assessed_goal == null)) {
			if (this.Pleasure_ticker > this.Pleasure_treshold) {
				this.pleasure -= 0.4;
				this.Pleasure_ticker = 0;
			}
		}
		else {
			this.Pleasure_ticker = 0;

			if(this.last_assessed_goal.getStatus().success()){

				this.pleasure += 1;		
			}
			else if (this.last_assessed_goal.getStatus().failed()) {

				this.pleasure -= 1;		
			}
		}

		//Assessing changes in Arousal
		if (this.Arousal_ticker > this.Arousal_treshold) {
			this.arousal -= 0.4;
			this.Arousal_ticker = 0;
		}

		var prev_known_entities = this.known_entities;

		this.known_entities = agent.getState().worldmodel.elements.size();

		System.out.println(this.known_entities);

		if (this.known_entities > prev_known_entities) {
			this.arousal += 1;
			this.Arousal_ticker = 0;

		}

		//Assessing changes in Dominance
		//This is yet to be implemented
		enforceLimit();

		Vector<Double> PDA_vals = new Vector<Double>(3);

		PDA_vals.add(this.pleasure);
		PDA_vals.add(this.dominance);
		PDA_vals.add(this.arousal);

		this.PDA_vector.add(PDA_vals);

		this.time_stamps.add(LocalDateTime.now());

		this.position_stamps.add(this.agent.getState().worldmodel.position);

		System.out.println("Pleasure: " + this.pleasure);
		System.out.println("Dominance: " + this.dominance);
		System.out.println("Arousal: " + this.arousal);
		System.out.println("Pleasure_ticker: " + this.Pleasure_ticker);
		System.out.println("Dominance_ticker: " + this.Dominance_ticker);
		System.out.println("Arousal_ticker: " + this.Arousal_ticker);

		this.last_assessed_goal = last_goal;
	}

	private void enforceLimit() {
		if (this.pleasure > this.limit) {
			this.pleasure = this.limit;
		}
		else if(this.pleasure < -this.limit) {
			this.pleasure = -this.limit;
		}

		if (this.dominance > this.limit) {
			this.dominance = this.limit;
		}
		else if(this.dominance < -this.limit) {
			this.dominance = -this.limit;
		}

		if (this.arousal > this.limit) {
			this.arousal = this.limit;
		}
		else if(this.arousal < -this.limit) {
			this.arousal = -this.limit;
		}
	}

	public double getPleasure() {
		return this.pleasure;
	}

	public double getDominance() {
		return this.dominance;
	}

	public double getArousal() {
		return this.arousal;
	}

}
