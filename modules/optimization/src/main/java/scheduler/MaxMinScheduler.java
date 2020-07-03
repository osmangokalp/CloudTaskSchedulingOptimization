package scheduler;

import simulation.Simulation;

import java.util.ArrayList;

public class MaxMinScheduler extends TaskScheduler {
    private final double[][] ETC_MATRIX;
    private final int numOfVMs;
    private final int numOfCloudlets;

    private ArrayList<Integer> unmappedTaskIndices = new ArrayList<>();
    private double[] mat; //machine availability time

    public MaxMinScheduler(Simulation sim) {
        super(sim);

        ETC_MATRIX = sim.getETC_MATRIX();
        numOfVMs = sim.getNumOfVMs();
        numOfCloudlets = sim.getNumOfCloudlets();

        //initialize readyTime array
        mat = new double[numOfVMs];
        for (int i = 0; i < numOfVMs; i++) {
            mat[i] = 0;
        }

        for (int i = 0; i < numOfCloudlets; i++) {
            unmappedTaskIndices.add(i);
        }
    }

    @Override
    public int[] schedule(int MAX_FES) {
        int[] mapping = new int[numOfCloudlets];

        while (!unmappedTaskIndices.isEmpty()) {
            int minCloudletIndex = -1;
            int minVMIndex = -1;
            int maxMinCloudletIndex = -1;
            int maxMinVMIndex = -1;
            double maxMinCT = -1;
            for (int cloudletIndex : unmappedTaskIndices) {
                double minCT = Double.POSITIVE_INFINITY;
                for (int VMIndex = 0; VMIndex < numOfVMs; VMIndex++) {
                    double ct = ETC_MATRIX[cloudletIndex][VMIndex] + mat[VMIndex]; //completion time
                    if (ct < minCT) {
                        minCT = ct;
                        minCloudletIndex = cloudletIndex;
                        minVMIndex = VMIndex;
                    }
                }
                if (minCT > maxMinCT) {
                    maxMinCT = minCT;
                    maxMinCloudletIndex = minCloudletIndex;
                    maxMinVMIndex = minVMIndex;
                }
            }

            mapping[maxMinCloudletIndex] = maxMinVMIndex; //set the mapping value found
            unmappedTaskIndices.remove(Integer.valueOf(maxMinCloudletIndex)); //remove from unmapped list
            mat[maxMinVMIndex] += ETC_MATRIX[maxMinCloudletIndex][maxMinVMIndex]; //update calc time
        }

        return mapping;
    }
}
