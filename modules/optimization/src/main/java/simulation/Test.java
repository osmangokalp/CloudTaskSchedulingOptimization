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

    //Scenario 0: low number of cloudlets, high heterogeneity
    //Scenario 1: low number of cloudlets, low heterogeneity
    //Scenario 2: medium number of cloudlets, high heterogeneity
    //Scenario 3: medium number of cloudlets, low heterogeneity
    //Scenario 4: high number of cloudlets, high heterogeneity
    //Scenario 5: high number of cloudlets, low heterogeneity
    //# of cloudlets: 100, 1000, 5000
    //low heterogeneity: VM_MIPS_POWERS[i] = rng.nextInt(101) + 500; // Rand [500, 600]
    //high heterogeneity: VM_MIPS_POWERS[i] = rng.nextInt(901) + 100; // Rand [100, 1000]
    private static int SEED = 0;
    private static int NUM_TRY = 10;

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
        numOfCloudlets = 100;
        highHeterogeneity = 1;
        MAX_FES = numOfCloudlets * 1000;
        doAllExperiments();

        //scenario 1
        System.out.println("\n********************** SCENARIO 1 **************************");
        numOfCloudlets = 100;
        highHeterogeneity = 0;
        MAX_FES = numOfCloudlets * 1000;
        doAllExperiments();

        //scenario 2
        System.out.println("\n********************** SCENARIO 2 **************************");
        numOfCloudlets = 1000;
        highHeterogeneity = 1;
        MAX_FES = numOfCloudlets * 1000;
        doAllExperiments();

        //scenario 3
        System.out.println("\n********************** SCENARIO 3 **************************");
        numOfCloudlets = 1000;
        highHeterogeneity = 0;
        doAllExperiments();

        //scenario 4
        System.out.println("\n********************** SCENARIO 4 **************************");
        numOfCloudlets = 5000;
        highHeterogeneity = 1;
        MAX_FES = numOfCloudlets * 1000;
        doAllExperiments();

        //scenario 5
        System.out.println("\n********************** SCENARIO 5 **************************");
        numOfCloudlets = 5000;
        highHeterogeneity = 0;
        doAllExperiments();

    }

    private static void doAllExperiments() {
        FCFSExp();
        SJFExp();
        MinMinExp();
        MaxMinExp();
        ABCExp();
        PSOExp();
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
