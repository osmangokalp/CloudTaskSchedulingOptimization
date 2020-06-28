/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulation;

import simulation.Simulation;
import java.util.Random;
import org.cloudbus.cloudsim.Log;
import scheduler.ABC_Scheduler;
import scheduler.PSO_Scheduler;

/**
 *
 * @author osman
 */
public class Test {

    private static int seed = 0;

    private static int cloudletSchedulerType = 1; //0: space shared, 1: time shared
    private static int numOfCloudlets = 400;
    private static int numOfVMs = 10;
    private static int brokerType = 0; //0: Mapping broker, 1: SJF Broker, 2: FCFS Broker (Standard DatacenterBroker)
    private static boolean silent = true;

    static int MAX_FES = numOfCloudlets * 1000;

    public static void main(String[] args) {

        Random rng = new Random(seed);
        int[] mapping = new int[numOfCloudlets];
        Log.print("Random mapping: ");
        for (int i = 0; i < numOfCloudlets; i++) {
            mapping[i] = rng.nextInt(numOfVMs); // random mapping
            System.out.print(mapping[i] + ", ");
        }
        Log.printLine();

        Simulation sim = new Simulation(cloudletSchedulerType, numOfCloudlets, numOfVMs, brokerType, rng, silent);

        double predictedMakespan = sim.predictFitnessValue(mapping);
        System.out.println("Random mapping predicted makespan: " + predictedMakespan);

        double actualMakespan = sim.runSimulation(mapping);
        System.out.println("Random mapping actual makespan: " + actualMakespan);

        System.out.println("");

        //ABC-----------------------------------------------
        ABC_Scheduler abcScheduler = new ABC_Scheduler(sim);
        mapping = abcScheduler.schedule(MAX_FES);

        System.out.print("ABC Mapping: ");
        for (int i = 0; i < numOfCloudlets; i++) {
            System.out.print(mapping[i] + ", ");
        }
        System.out.println();

        predictedMakespan = sim.predictFitnessValue(mapping);
        System.out.println("ABC predicted makespan: " + predictedMakespan);

        actualMakespan = sim.runSimulation(mapping);
        System.out.println("ABC actual makespan: " + actualMakespan);

        System.out.println("");

        //PSO-----------------------------------------------
        PSO_Scheduler psoScheduler = new PSO_Scheduler(sim);
        mapping = psoScheduler.schedule(MAX_FES);

        System.out.print("PSO Mapping: ");
        for (int i = 0; i < numOfCloudlets; i++) {
            System.out.print(mapping[i] + ", ");
        }
        System.out.println();

        predictedMakespan = sim.predictFitnessValue(mapping);
        System.out.println("PSO predicted makespan: " + predictedMakespan);

        actualMakespan = sim.runSimulation(mapping);
        System.out.println("PSO actual makespan: " + actualMakespan);

        System.out.println("");

        brokerType = 1;
        Simulation sim2 = new Simulation(cloudletSchedulerType, numOfCloudlets, numOfVMs, brokerType, rng, silent);
        System.out.println("SJF makespan: " + sim2.runSimulation(null));

        brokerType = 2;
        Simulation sim3 = new Simulation(cloudletSchedulerType, numOfCloudlets, numOfVMs, brokerType, rng, silent);
        System.out.println("FCFS makespan: " + sim3.runSimulation(null));

    }
}
