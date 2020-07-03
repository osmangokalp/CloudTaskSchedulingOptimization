package broker;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.lists.VmList;
import simulation.Simulation;

import java.util.ArrayList;
import java.util.List;

public class MinMinBroker extends DatacenterBroker {
    private Simulation sim;
    private final int[] CLOUDLET_LENGTHS;
    private final double[][] ETC_MATRIX;
    private final int numOfCloudlets;
    private final int numOfVMs;

    private ArrayList<Integer> cloudletsToBeAssigned = new ArrayList<>();
    private double[] readyTime;

    public MinMinBroker(String name, Simulation sim) throws Exception {
        super(name);
        this.sim = sim;

        CLOUDLET_LENGTHS = sim.getCLOUDLET_LENGTHS();
        ETC_MATRIX = sim.getETC_MATRIX();
        numOfCloudlets = sim.getNumOfCloudlets();
        numOfVMs = sim.getNumOfVMs();

        //initialize readyTime array
        readyTime = new double[numOfVMs];
        for (double d : readyTime) {
            d = 0;
        }

        for (int i = 0; i < CLOUDLET_LENGTHS.length; i++) {
            cloudletsToBeAssigned.add(i);
        }
    }

    @Override
    protected void submitCloudlets() {
        double temp = 0;
        int vmIndex = 0;
        List<Cloudlet> successfullySubmitted = new ArrayList<Cloudlet>();

        while (!cloudletsToBeAssigned.isEmpty()){
            //find min completion time
            double min = Double.POSITIVE_INFINITY;
            int minCloudletIndex = -1;
            int minVMIndex = -1;
            for (int cloudletIndex : cloudletsToBeAssigned) {
                for (int VMIndex = 0; VMIndex < numOfVMs; VMIndex++) {
                    double calcTime = ETC_MATRIX[cloudletIndex][VMIndex] + readyTime[VMIndex];
                    if (calcTime < min) {
                        min = calcTime;
                        minCloudletIndex = cloudletIndex;
                        minVMIndex = VMIndex;
                    }
                }
            }

            Cloudlet cloudlet = getCloudletList().get(minCloudletIndex);
            Vm vm = getVmList().get(minVMIndex);

            if (!Log.isDisabled()) {
                Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Sending cloudlet ",
                        cloudlet.getCloudletId(), " to VM #", vm.getId());
            }

            cloudlet.setVmId(vm.getId());
            sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
            cloudletsSubmitted++;
            vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
            getCloudletSubmittedList().add(cloudlet);
            successfullySubmitted.add(cloudlet);

            cloudletsToBeAssigned.remove(Integer.valueOf(minCloudletIndex)); //remove from unassgined list
            readyTime[minVMIndex] += ETC_MATRIX[minCloudletIndex][minVMIndex]; //update calc time
        }

        // remove submitted cloudlets from waiting list
        getCloudletList().removeAll(successfullySubmitted);
    }
}
