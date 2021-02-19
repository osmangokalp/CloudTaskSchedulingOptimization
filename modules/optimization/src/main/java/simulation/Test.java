/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulation;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.util.WorkloadFileReader;
import scheduler.*;
import simulation.Simulation;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Log;

/**
 * @author osman
 */
public class Test {

    private static int seed = 0;

    private static int cloudletSchedulerType = 0; //0: space shared, 1: time shared
    private static int numOfCloudlets = 400;
    private static int numOfVMs = 10;
    private static int brokerType = 0; //0: Mapping broker, 1: SJF Broker, 2: FCFS Broker (Standard DatacenterBroker)
    private static boolean silent = true;
    private static int fitnessType = 0; // 0:makespan, 1: resource utilization

    static int MAX_FES = numOfCloudlets * 10000;

    public static void main(String[] args) {

        //List<Cloudlet> cloudletList = Util.readSWFWorkload("HPC2N-2002-1.1-cln");

        long startTime, endTime;

        Random rng = new Random(seed);
        int[] mapping = new int[numOfCloudlets];
        Log.print("Random mapping: ");
        for (int i = 0; i < numOfCloudlets; i++) {
            mapping[i] = rng.nextInt(numOfVMs); // random mapping
            System.out.print(mapping[i] + ", ");
        }
        Log.printLine();

        Simulation sim = new Simulation(cloudletSchedulerType, numOfCloudlets, numOfVMs, brokerType, fitnessType, rng, silent);

        double predictedMakespan = sim.predictFitnessValue(mapping);
        System.out.println("Random mapping predicted fitness: " + predictedMakespan);

        double actualMakespan = sim.runSimulation(mapping);
        System.out.println("Random mapping actual fitness: " + actualMakespan);

        System.out.println("Random mapping predicted makespan: " + sim.calculatePredictedMakespan(mapping));
        System.out.println("Random mapping predicted resource utilization: " + sim.calculatePredictedResourceUtilization(mapping));

        System.out.println("");

        //CMAES-----------------------------------------------
        rng = new Random(seed);
        CMAES_Scheduler cmaesScheduler = new CMAES_Scheduler(sim);
        startTime = System.nanoTime();
        mapping = cmaesScheduler.schedule(MAX_FES);
        endTime = System.nanoTime();
        System.out.println("Time elapsed (s): " + (endTime - startTime) / 1000000000.0);

        System.out.print("CMAES Mapping: ");
        for (int i = 0; i < numOfCloudlets; i++) {
            System.out.print(mapping[i] + ", ");
        }
        System.out.println();

        predictedMakespan = sim.predictFitnessValue(mapping);
        System.out.println("ABC predicted fitness: " + predictedMakespan);

        actualMakespan = sim.runSimulation(mapping);
        System.out.println("ABC actual fitness: " + actualMakespan);

        System.out.println("ABC predicted makespan: " + sim.calculatePredictedMakespan(mapping));
        System.out.println("ABC predicted resource utilization: " + sim.calculatePredictedResourceUtilization(mapping));

        System.out.println("");

        //ABC-----------------------------------------------
        rng = new Random(seed);
        ABC_Scheduler abcScheduler = new ABC_Scheduler(sim);
        startTime = System.nanoTime();
        mapping = abcScheduler.schedule(MAX_FES);
        endTime = System.nanoTime();
        System.out.println("Time elapsed (s): " + (endTime - startTime) / 1000000000.0);

        System.out.print("ABC Mapping: ");
        for (int i = 0; i < numOfCloudlets; i++) {
            System.out.print(mapping[i] + ", ");
        }
        System.out.println();

        predictedMakespan = sim.predictFitnessValue(mapping);
        System.out.println("ABC predicted fitness: " + predictedMakespan);

        actualMakespan = sim.runSimulation(mapping);
        System.out.println("ABC actual fitness: " + actualMakespan);

        System.out.println("ABC predicted makespan: " + sim.calculatePredictedMakespan(mapping));
        System.out.println("ABC predicted resource utilization: " + sim.calculatePredictedResourceUtilization(mapping));

        System.out.println("");

        //PSO-----------------------------------------------
        rng = new Random(seed);
        PSO_Scheduler psoScheduler = new PSO_Scheduler(sim);
        startTime = System.nanoTime();
        mapping = psoScheduler.schedule(MAX_FES);
        endTime = System.nanoTime();
        System.out.println("Time elapsed (s): " + (endTime - startTime) / 1000000000.0);

        System.out.print("PSO Mapping: ");
        for (int i = 0; i < numOfCloudlets; i++) {
            System.out.print(mapping[i] + ", ");
        }
        System.out.println();

        predictedMakespan = sim.predictFitnessValue(mapping);
        System.out.println("PSO predicted fitness: " + predictedMakespan);

        actualMakespan = sim.runSimulation(mapping);
        System.out.println("PSO actual fitness: " + actualMakespan);

        System.out.println("PSO predicted makespan: " + sim.calculatePredictedMakespan(mapping));
        System.out.println("PSO predicted resource utilization: " + sim.calculatePredictedResourceUtilization(mapping));

        System.out.println("");

        //SJF
        rng = new Random(seed);
        brokerType = 1;
        sim = new Simulation(cloudletSchedulerType, numOfCloudlets, numOfVMs, brokerType, fitnessType, rng, silent);
        System.out.println("SJF fitness: " + sim.runSimulation(null));

        //FCFS
        rng = new Random(seed);
        brokerType = 2;
        sim = new Simulation(cloudletSchedulerType, numOfCloudlets, numOfVMs, brokerType, fitnessType, rng, silent);
        System.out.println("FCFS fitness: " + sim.runSimulation(null));

        System.out.println();

        //Min Min
        rng = new Random(seed);
        brokerType = 0;
        sim = new Simulation(cloudletSchedulerType, numOfCloudlets, numOfVMs, brokerType, fitnessType, rng, silent);
        MinMinScheduler minmins = new MinMinScheduler(sim);
        mapping = minmins.schedule(0);
        predictedMakespan = sim.predictFitnessValue(mapping);
        System.out.println("MinMin predicted fitness: " + predictedMakespan);
        actualMakespan = sim.runSimulation(mapping);
        System.out.println("MinMin actual fitness: " + actualMakespan);

        System.out.println("MinMin predicted makespan: " + sim.calculatePredictedMakespan(mapping));
        System.out.println("MinMin predicted resource utilization: " + sim.calculatePredictedResourceUtilization(mapping));

        System.out.println();

        //Max Min
        rng = new Random(seed);
        brokerType = 0;
        sim = new Simulation(cloudletSchedulerType, numOfCloudlets, numOfVMs, brokerType, fitnessType, rng, silent);
        MaxMinScheduler maxmins = new MaxMinScheduler(sim);
        mapping = maxmins.schedule(0);
        predictedMakespan = sim.predictFitnessValue(mapping);
        System.out.println("MaxMin predicted fitness: " + predictedMakespan);
        actualMakespan = sim.runSimulation(mapping);
        System.out.println("MaxMin actual fitness: " + actualMakespan);

        System.out.println("MaxMin predicted makespan: " + sim.calculatePredictedMakespan(mapping));
        System.out.println("MaxMin predicted resource utilization: " + sim.calculatePredictedResourceUtilization(mapping));

    }
}
