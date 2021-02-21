/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulation;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import scheduler.*;

import java.util.Random;

import org.cloudbus.cloudsim.Log;

/**
 * @author osman
 */
public class Test {

    //Scenario 0: high number of cloudlets, high heterogeneity
    //Scenario 1: high number of cloudlets, low heterogeneity
    //Scenario 2: low number of cloudlets, high heterogeneity
    //Scenario 3: low number of cloudlets, low heterogeneity
    //# of cloudlets: 100, 1000
    //low heterogeneity: VM_MIPS_POWERS[i] = rng.nextInt(101) + 900; // Rand [900, 1000]
    //high heterogeneity: VM_MIPS_POWERS[i] = rng.nextInt(901) + 100; // Rand [100, 1000]
    private static int SEED = 0;
    private static int NUM_TRY = 25;

    private static int cloudletSchedulerType = 0; //0: space shared, 1: time shared
    private static int numOfCloudlets;
    private static int highHeterogeneity;
    private static int numOfVMs = 10;
    private static int brokerType; //0: Mapping broker, 1: SJF Broker, 2: FCFS Broker (Standard DatacenterBroker)
    private static boolean silent = true;
    private static int fitnessType = 0; // 0:makespan, 1: resource utilization

    static int MAX_FES;

    public static void main(String[] args) {

        //List<Cloudlet> cloudletList = Util.readSWFWorkload("HPC2N-2002-1.1-cln");

        //scenario 0
        System.out.println("\n********************** SCENARIO 0 **************************");
        numOfCloudlets = 1000;
        highHeterogeneity = 1;
        MAX_FES = numOfCloudlets * 1000;
        doAllExperiments();

        //scenario 1
        System.out.println("\n********************** SCENARIO 1 **************************");
        numOfCloudlets = 1000;
        highHeterogeneity = 0;
        MAX_FES = numOfCloudlets * 1000;
        doAllExperiments();

        //scenario 2
        System.out.println("\n********************** SCENARIO 2 **************************");
        numOfCloudlets = 100;
        highHeterogeneity = 1;
        MAX_FES = numOfCloudlets * 1000;
        doAllExperiments();

        //scenario 3
        System.out.println("\n********************** SCENARIO 3 **************************");
        numOfCloudlets = 100;
        highHeterogeneity = 0;
        doAllExperiments();

        /*long startTime, endTime;

        Random rng = new Random(SEED);
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
        rng = new Random(SEED);
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
        rng = new Random(SEED);
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
        rng = new Random(SEED);
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
        rng = new Random(SEED);
        brokerType = 1;
        sim = new Simulation(cloudletSchedulerType, numOfCloudlets, numOfVMs, brokerType, fitnessType, rng, silent);
        System.out.println("SJF fitness: " + sim.runSimulation(null));

        //FCFS
        rng = new Random(SEED);
        brokerType = 2;
        sim = new Simulation(cloudletSchedulerType, numOfCloudlets, numOfVMs, brokerType, fitnessType, rng, silent);
        System.out.println("FCFS fitness: " + sim.runSimulation(null));

        System.out.println();

        //Min Min
        rng = new Random(SEED);
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
        rng = new Random(SEED);
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
        */
    }

    private static void doAllExperiments() {
        FCFSExp();
        SJFExp();
        MinMinExp();
        MaxMinExp();
        ABCExp();
        CMAESExp();
        PSOExp();
    }

    public static void CMAESExp() {
        double[] results = new double[NUM_TRY];
        for (int i = 0; i < NUM_TRY; i++) {
            Random rng = new Random(SEED + i);
            brokerType = 0;
            Simulation sim = new Simulation(cloudletSchedulerType, numOfCloudlets, numOfVMs, brokerType, fitnessType, rng, silent, highHeterogeneity);
            CMAES_Scheduler cmaes_scheduler = new CMAES_Scheduler(sim);
            int[] mapping = cmaes_scheduler.schedule(MAX_FES);
            double makespan = sim.runSimulation(mapping);
            results[i] = makespan;
        }
        calculateStatistics("CMAES", results);
    }

    public static void PSOExp() {
        double[] results = new double[NUM_TRY];
        for (int i = 0; i < NUM_TRY; i++) {
            Random rng = new Random(SEED + i);
            brokerType = 0;
            Simulation sim = new Simulation(cloudletSchedulerType, numOfCloudlets, numOfVMs, brokerType, fitnessType, rng, silent, highHeterogeneity);
            PSO_Scheduler pso_scheduler = new PSO_Scheduler(sim);
            int[] mapping = pso_scheduler.schedule(MAX_FES);
            double makespan = sim.runSimulation(mapping);
            results[i] = makespan;
        }
        calculateStatistics("PSO", results);
    }

    public static void ABCExp() {
        double[] results = new double[NUM_TRY];
        for (int i = 0; i < NUM_TRY; i++) {
            Random rng = new Random(SEED + i);
            brokerType = 0;
            Simulation sim = new Simulation(cloudletSchedulerType, numOfCloudlets, numOfVMs, brokerType, fitnessType, rng, silent, highHeterogeneity);
            ABC_Scheduler abc_scheduler = new ABC_Scheduler(sim);
            int[] mapping = abc_scheduler.schedule(MAX_FES);
            double makespan = sim.runSimulation(mapping);
            results[i] = makespan;
        }
        calculateStatistics("ABC", results);
    }

    public static void MaxMinExp() {
        double[] results = new double[NUM_TRY];
        for (int i = 0; i < NUM_TRY; i++) {
            Random rng = new Random(SEED + i);
            brokerType = 0;
            Simulation sim = new Simulation(cloudletSchedulerType, numOfCloudlets, numOfVMs, brokerType, fitnessType, rng, silent, highHeterogeneity);
            MaxMinScheduler maxmins = new MaxMinScheduler(sim);
            int[] mapping = maxmins.schedule(0);
            double makespan = sim.runSimulation(mapping);
            results[i] = makespan;
        }
        calculateStatistics("MaxMin", results);
    }

    public static void MinMinExp() {
        double[] results = new double[NUM_TRY];
        for (int i = 0; i < NUM_TRY; i++) {
            Random rng = new Random(SEED + i);
            brokerType = 0;
            Simulation sim = new Simulation(cloudletSchedulerType, numOfCloudlets, numOfVMs, brokerType, fitnessType, rng, silent, highHeterogeneity);
            MinMinScheduler minmins = new MinMinScheduler(sim);
            int[] mapping = minmins.schedule(0);
            double makespan = sim.runSimulation(mapping);
            results[i] = makespan;
        }
        calculateStatistics("MinMin", results);
    }

    public static void SJFExp() {
        double[] results = new double[NUM_TRY];
        for (int i = 0; i < NUM_TRY; i++) {
            Random rng = new Random(SEED + i);
            brokerType = 1;
            Simulation sim = new Simulation(cloudletSchedulerType, numOfCloudlets, numOfVMs, brokerType, fitnessType, rng, silent, highHeterogeneity);
            results[i] = sim.runSimulation(null);
        }
        calculateStatistics("SJF", results);
    }

    public static void FCFSExp() {
        double[] results = new double[NUM_TRY];
        for (int i = 0; i < NUM_TRY; i++) {
            Random rng = new Random(SEED + i);
            brokerType = 2;
            Simulation sim = new Simulation(cloudletSchedulerType, numOfCloudlets, numOfVMs, brokerType, fitnessType, rng, silent, highHeterogeneity);
            results[i] = sim.runSimulation(null);
        }
        calculateStatistics("FCFS", results);
    }

    private static void calculateStatistics(String algName, double[] results) {
        System.out.println("\n\n------ " + algName + " -----");
        DescriptiveStatistics ds = new DescriptiveStatistics();
        for (double data : results) {
            ds.addValue(data);
        }
        System.out.println("\tAvg: " + ds.getMean());
        System.out.println("\tMin: " + ds.getMin());
        System.out.println("\tMax: " + ds.getMax());
        System.out.println("\tStd: " + ds.getStandardDeviation());
    }
}
