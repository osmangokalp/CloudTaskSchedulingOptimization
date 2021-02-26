/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scheduler;

import simulation.Util;
import simulation.Simulation;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author osman From:https://sci2s.ugr.es/EAMHCO (Several advanced Particle
 * Swarm Optimization algorithms)
 */
public class PSO_Scheduler extends TaskScheduler {
    
    private int SWARM_SIZE = 40;
    private int TYPE_OF_ALGORITHM = 0;
    private int NEIGHBOR = SWARM_SIZE / 2; //[0, SWARM_SIZE]

    public PSO_Scheduler(Simulation sim) {
        super(sim);
        rng = sim.getRng();
    }

    @Override
    public int[] schedule(int MAX_FES) {
        max_evaluations = MAX_FES;
        
        check_parameters();
        
        init();

        run_pso();

        int[] mapping = Util.discretizeSol(best_particle.best_position);

        return mapping;
    }

    /**
     * Swarm of particles
     */
    protected ArrayList<PSO_Particle> swarm;
    /**
     * Number of evaluations spent
     */
    protected int eval;
    /**
     * Maximum evaluations
     */
    protected int max_evaluations;
    /**
     * Problem's dimension
     */
    protected int dim;

    /**
     * Swarm size
     */
    protected int size;
    /**
     * Best particle
     */
    protected PSO_Particle best_particle;
    /**
     * Random generator
     */
    protected Random rng;
    /**
     * Type of neighborhood
     */
    protected int type = 0;
    /**
     * Size of neighborhood
     */
    protected int neighbor;
    /**
     * Test function
     */
    /**
     * Seed of random generator
     */
    protected long seed;

    /**
     * Calculates the distance between two particles
     */
    private double distance(PSO_Particle a, PSO_Particle b) {
        double dist = 0;
        for (int i = 0; i < dim; i++) {
            dist += (Math.pow(a.position[i] - b.position[i], 2));
        }

        return Math.sqrt(dist);
    }

    private boolean check_parameters() {
        
        //Dimension
        dim = sim.getNumOfCloudlets();

        size = SWARM_SIZE;
        
        swarm = new ArrayList<PSO_Particle>(size);
        
        //type of algorithm
        // 0 -> global
        // 1 -> social
        // 2 -> geographical
        type = TYPE_OF_ALGORITHM;
        
        if (type > 0) {
            neighbor = NEIGHBOR;
        }

        return true;
    }

    /**
     * Initializes the swarm
     */
    private void init() {
        eval = 0;

        for (int i = 0; i < size; i++) {
            swarm.add(new PSO_Particle(dim, sim.getNumOfVMs(), 0));
            swarm.get(i).init(rng);
        }
    }

    /**
     * Return the reference particle for a given particle according the type of
     * the neighborhood
     *
     * @param pos Index of the particle
     * @return The reference particle
     */
    private PSO_Particle reference_particle(int pos) {
        switch (type) {
            case 1: // local social
                int ini,
                 end;
                ini = pos - neighbor / 2;
                end = pos + neighbor / 2;
                if (neighbor % 2 == 1) {
                    ini--;
                }
                ini = (ini + swarm.size()) % swarm.size();
                end = end % swarm.size();

                PSO_Particle lbest = swarm.get(ini);
                for (ini = (ini + 1) % swarm.size(); ini != end; ini = (ini + 1) % swarm.size()) {
                    if (swarm.get(ini).fitness < lbest.fitness) {
                        lbest = swarm.get(ini);
                    }
                }
                return lbest;

            case 2: // local geographic
                ArrayList<PSO_Particle> nearest = new ArrayList<PSO_Particle>(neighbor);
                ArrayList<Double> dist = new ArrayList<Double>(neighbor);
                for (int i = 0; i < neighbor; i++) {
                    dist.add(i, Double.MAX_VALUE);
                    nearest.add(i, swarm.get(i));
                }
                //find the neighbor particles nearest to actual
                for (int i = 0; i < swarm.size(); i++) {
                    if (i != pos) {
                        double dist_aux = distance(swarm.get(pos), swarm.get(i));
                        for (int j = 0; j < neighbor; j++) {
                            if (dist_aux < dist.get(j)) {
                                for (int k = neighbor - 1; k > j; k--) {
                                    dist.set(k, dist.get(k - 1));
                                    nearest.set(k, nearest.get(k - 1));
                                }
                                dist.set(j, dist_aux);
                                nearest.set(j, swarm.get(i));
                                break;
                            }
                        }
                    }
                }

                //choose the best of the nearest
                int index = 0;
                double best = nearest.get(0).fitness;
                for (int i = 1; i < neighbor; i++) {
                    if (nearest.get(i).fitness < best) {
                        best = nearest.get(i).fitness;
                        index = i;
                    }
                }

                return nearest.get(index);

            default: // global
                return best_particle;
        }
    }

    /**
     * PSO Main function
     */
    public void run_pso() {
        /* Evaluates the initial population and find the first best*/
        best_particle = new PSO_Particle();
        swarm.get(0).fitness = sim.predictFitnessValue(Util.discretizeSol(swarm.get(0).position));
        eval++;
        swarm.get(0).best_fitness = swarm.get(0).fitness;
        swarm.get(0).best_position = swarm.get(0).position.clone();
        best_particle.best_fitness = swarm.get(0).best_fitness;
        best_particle.best_position = swarm.get(0).best_position.clone();

        for (int i = 1; i < swarm.size() && eval < max_evaluations; i++) {
            swarm.get(i).fitness = sim.predictFitnessValue(Util.discretizeSol(swarm.get(i).position));
            eval++;
            swarm.get(i).best_fitness = swarm.get(i).fitness;
            swarm.get(i).best_position = swarm.get(i).position.clone();
            if (swarm.get(i).best_fitness < best_particle.best_fitness) {
                best_particle = swarm.get(i);
            }
        }

        PSO_Particle.update_intertia(eval, max_evaluations);

        /* PSO main boucle*/
        while (eval < max_evaluations) {
            /* Updates particles' velocity */
            for (int i = 0; i < swarm.size(); i++) {
                swarm.get(i).update_velocity(reference_particle(i), rng);
            }

            /* Updates particles' position */
            for (int i = 0; i < swarm.size() && eval < max_evaluations; i++) {
                swarm.get(i).update_position();
                /* Evaluates new positions*/
                swarm.get(i).fitness = sim.predictFitnessValue(Util.discretizeSol(swarm.get(i).position));
                eval++;

                /* Check for own best */
                if (swarm.get(i).fitness < swarm.get(i).best_fitness) {
                    swarm.get(i).best_fitness = swarm.get(i).fitness;
                    swarm.get(i).best_position = swarm.get(i).position.clone();
                }

                /* Check for best particle*/
                if (swarm.get(i).fitness < best_particle.best_fitness) {
                    best_particle = swarm.get(i);
                }
            }

            /* Inertia update */
            PSO_Particle.update_intertia(eval, max_evaluations);
        }
    }

}
