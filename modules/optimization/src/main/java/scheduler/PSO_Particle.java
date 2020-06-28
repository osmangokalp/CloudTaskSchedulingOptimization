/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scheduler;

import java.util.Random;

/**
 *
 * @author osman
 */
public class PSO_Particle {
	/** Cognitive knowledge factor */
	private static double cognitive = 2;
	/** Social knowledge factor */
	private static double social = 2;
	/** Inertia factor initial value */
	private static double inertia = 0.9;
	
	/** Problem's dimension */
	protected int dimension;
	/** Actual position */
	protected double[] position;
	/** best position factor */
	protected double[] best_position;
	/** Velocity vector */
	protected double[] velocity;
	/** Problem bounds */
	protected double max_bounds;
	protected double min_bounds;
	/** Maximum velocity on a component */
	protected double max_velocity;
	/** Fitness of actual position */
	protected double fitness;
	/** Fitness of best position */
	protected double best_fitness;

	/** Default constructor empty */
	public PSO_Particle(){}
	
	/** Particle constructor
	 * @param dim Problem's dimension
	 * @param max High bound for each dimension
	 * @param min Low bound for each dimension
	 */
	public PSO_Particle(int dim, double max, double min){
		dimension = dim;
		position = new double[dimension];
		best_position = new double[dimension];
		velocity = new double[dimension];
		max_bounds = max;
		min_bounds = min;
		max_velocity = 0.2*Math.abs(max_bounds - min_bounds);
	}

	/** Initializes a particle position and velocity
	 * @param rand Random generator.
	 */
	public void init(Random rand){
		for(int i = 0; i < dimension; i++){
			position[i] = rand.nextDouble()*(max_bounds - min_bounds) + min_bounds;
			best_position[i] = position[i];
			velocity[i] = rand.nextDouble()*(max_velocity * 2) - max_velocity;
		}
	}

	/** Updates particle's velocity
	 * @param global_best reference particle.
	 * @param rand Random generator.
	 */
	public void update_velocity(PSO_Particle global_best, Random rand){
		for(int i = 0; i < dimension; i++){
			velocity[i] = (velocity[i] * inertia) + (cognitive * rand.nextDouble() * (best_position[i] - position[i]))
							+ (social * rand.nextDouble() * (global_best.best_position[i] - position[i]));
			if(velocity[i] < -max_velocity)
				velocity[i] = -max_velocity;
			else if(velocity[i] > max_velocity)
				velocity[i] = max_velocity;
		}
	}

	/** Updates particle's position
	 */
	public void update_position(){
		for( int i = 0; i < dimension; i++){
			position[i] = position[i] + velocity[i];
			if(position[i] > max_bounds){
				position[i] = max_bounds;
				velocity[i] = 0;
			}
			else if(position[i] < min_bounds)
			{
				position[i] = min_bounds;
				velocity[i] = 0;
			}
		}
	}

	/** Updates inertia value for all particles
	 * @param eval number of evaluations
	 * @param max_eval maximum evaluation allowed
	 */
	static void update_intertia(int eval, int max_eval){
		inertia = 0.9 - 0.5*((eval*1.0)/max_eval);
	}

	/** Prints information about the particle
	 * @param n number of particle
	 */
	public void print(int n){
		System.out.println("particle: "+n);
		for(int i = 0; i < position.length; i++){
			System.out.println(position[i]+","+velocity[i]+","+best_position[i]);
		}
		System.out.println(best_fitness + "," + fitness+","+inertia);
	}
};

