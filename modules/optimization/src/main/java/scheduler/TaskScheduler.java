/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scheduler;

import simulation.Simulation;

/**
 *
 * @author osman
 */
public abstract class TaskScheduler {
    protected Simulation sim;
    
    public TaskScheduler(Simulation sim) {
        this.sim = sim;
    }
    
    /**
     * Maps given tasks to VMs.
     * @return task-to-VM mapping array.
     */
    public abstract int[] schedule(int MAX_FES);
}
