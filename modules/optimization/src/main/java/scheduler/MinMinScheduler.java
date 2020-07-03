package scheduler;

import simulation.Simulation;

import java.util.ArrayList;

public class MinMinScheduler extends TaskScheduler {
    private final double[][] ETC_MATRIX;
    private final int numOfVMs;
    private final int numOfCloudlets;

    private ArrayList<Integer> unmappedTaskIndices = new ArrayList<>();
    private double[] mat; //machine availability time

    public MinMinScheduler(Simulation sim) {
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
            //find min completion time
            double minCT = Double.POSITIVE_INFINITY;
            int minCloudletIndex = -1;
            int minVMIndex = -1;
            for (int cloudletIndex : unmappedTaskIndices) {
                for (int VMIndex = 0; VMIndex < numOfVMs; VMIndex++) {
                    double ct = ETC_MATRIX[cloudletIndex][VMIndex] + mat[VMIndex]; //completion time
                    if (ct < minCT) {
                        minCT = ct;
                        minCloudletIndex = cloudletIndex;
                        minVMIndex = VMIndex;
                    }
                }
            }

            mapping[minCloudletIndex] = minVMIndex; //set the mapping value found
            unmappedTaskIndices.remove(Integer.valueOf(minCloudletIndex)); //remove from unmapped list
            mat[minVMIndex] += ETC_MATRIX[minCloudletIndex][minVMIndex]; //update calc time
        }

        return mapping;
    }
}
