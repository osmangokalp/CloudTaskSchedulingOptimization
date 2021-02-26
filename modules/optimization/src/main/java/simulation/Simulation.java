/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulation;

import broker.MappingBroker;
import broker.SJF_DatacenterBroker;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeSharedOverSubscription;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 *
 * @author osman
 */
public class Simulation {

    private int cloudletSchedulerType; //0: space shared, 1: time shared
    private int brokerType; //0: Mapping broker, 1: SJF Broker
    private int numOfCloudlets;
    private int numOfVMs;
    private Random rng;
    private boolean silent;
    private int fitnessType; // 0:makespan, 1: resource utilization

    private final double[] VM_MIPS_POWERS;
    private final int[] CLOUDLET_LENGTHS;
    private final double[][] ETC_MATRIX;

    public Simulation(int cloudletSchedulerType, int numOfCloudlets, int numOfVMs, int brokerType, int fitnessType, Random rng, boolean silent, int highHeterogeneity) {
        this.cloudletSchedulerType = cloudletSchedulerType;
        this.numOfCloudlets = numOfCloudlets;
        this.numOfVMs = numOfVMs;
        this.brokerType = brokerType;
        this.fitnessType = fitnessType;
        this.rng = rng;
        this.silent = silent;

        VM_MIPS_POWERS = new double[numOfVMs];
        for (int i = 0; i < numOfVMs; i++) {
            if (highHeterogeneity == 1) {
                VM_MIPS_POWERS[i] = rng.nextInt(901) + 100; // Rand [100, 1000]
            } else {
                VM_MIPS_POWERS[i] = rng.nextInt(101) + 500; // Rand [500, 600]
            }
        }

        CLOUDLET_LENGTHS = new int[numOfCloudlets];
        for (int i = 0; i < this.numOfCloudlets; i++) {
            CLOUDLET_LENGTHS[i] = 1000 + rng.nextInt(1001); //Rand [1000, 2000]
        }

        ETC_MATRIX = new double[numOfCloudlets][numOfVMs];
        for (int i = 0; i < numOfCloudlets; i++) {
            for (int j = 0; j < numOfVMs; j++) {
                ETC_MATRIX[i][j] = (double) CLOUDLET_LENGTHS[i] / VM_MIPS_POWERS[j];
            }
        }
    }

    /**
     * The cloudlet list.
     */
    private List<Cloudlet> cloudletList;

    /**
     * The vmlist.
     */
    private List<Vm> vmlist;

    private List<Vm> createVM(int userId, int numOfVMs) {

        //Creates a container to store VMs. This list is passed to the broker later
        LinkedList<Vm> list = new LinkedList<Vm>();

        //VM Parameters
        long size = 1024; //image size (MB) - 10 GB
        int ram = 1024; //vm memory (MB)
        double mips;
        long bw = 1024;
        int pesNumber = 1; //number of cpus
        String vmm = "Xen"; //VMM name

        //create VMs
        Vm[] vm = new Vm[numOfVMs];

        for (int i = 0; i < numOfVMs; i++) {

            CloudletScheduler cs = null;
            switch (cloudletSchedulerType) {
                case 0:
                    cs = new CloudletSchedulerSpaceShared();
                    break;
                case 1:
                    cs = new CloudletSchedulerTimeShared();
                    break;
                default:
                    break;
            }

            mips = VM_MIPS_POWERS[i];
            Log.printLine("VM " + i + " MIPS: " + mips);
            vm[i] = new Vm(i, userId, mips, pesNumber, ram, bw, size, vmm, cs);

            list.add(vm[i]);
        }

        return list;
    }

    private List<Cloudlet> createCloudlet(int userId, int numOfCloudlets) {
        // Creates a container to store Cloudlets
        LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();

        //cloudlet parameters
        long length;
        long fileSize = 300;
        long outputSize = 300;
        int pesNumber = 1;
        UtilizationModel utilizationModel = new UtilizationModelFull();

        Cloudlet[] cloudlet = new Cloudlet[numOfCloudlets];

        for (int i = 0; i < numOfCloudlets; i++) {
            length = CLOUDLET_LENGTHS[i];
            Log.printLine("Cloudlet " + i + " length: " + length);
            cloudlet[i] = new Cloudlet(i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            // setting the owner of these Cloudlets
            cloudlet[i].setUserId(userId);
            list.add(cloudlet[i]);
        }

        return list;
    }

    private Datacenter createDatacenter(String name) {

        // Here are the steps needed to create a PowerDatacenter:
        // 1. We need to create a list to store one or more
        //    Machines
        List<Host> hostList = new ArrayList<Host>();

        // 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
        //    create a list to store these PEs before creating
        //    a Machine.
        List<Pe> peList1 = new ArrayList<Pe>();

        int mips = 1000;

        // 3. Create PEs and add these into the list.
        for (int i = 0; i < numOfVMs; i++) {
            peList1.add(new Pe(i, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
        }

        //4. Create Hosts with its id and list of PEs and add them to the list of machines
        int hostId = 0;
        int ram = 100 * 1024; //host memory (MB) - 20 GB
        long storage = 100 * 1024; //host storage - 1 TB
        int bw = 100 * 1024; //10 GB/s

        hostList.add(
                new Host(
                        hostId,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerSimple(bw),
                        storage,
                        peList1,
                        new VmSchedulerTimeSharedOverSubscription(peList1)
                )
        ); // This is our first machine

        // 5. Create a DatacenterCharacteristics object that stores the
        //    properties of a data center: architecture, OS, list of
        //    Machines, allocation policy: time- or space-shared, time zone
        //    and its price (G$/Pe time unit).
        String arch = "x86";      // system architecture
        String os = "Linux";          // operating system
        String vmm = "Xen";
        double time_zone = 10.0;         // time zone this resource located
        double cost = 3.0;              // the cost of using processing in this resource
        double costPerMem = 0.05;		// the cost of using memory in this resource
        double costPerStorage = 0.1;	// the cost of using storage in this resource
        double costPerBw = 0.1;			// the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

        // 6. Finally, we need to create a PowerDatacenter object.
        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    private DatacenterBroker createBroker() {

        DatacenterBroker broker = null;
        try {
            switch (brokerType) {
                case 0:
                    broker = new MappingBroker("Broker");
                    break;
                case 1:
                    broker = new SJF_DatacenterBroker("Broker");
                    break;
                case 2:
                    broker = new DatacenterBroker("Broker"); //Default broker: FCFS
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    public double runSimulation(int[] mapping) {
        if (silent) {
            Log.disable();
        }
        Log.printLine("Simulation Starts...");

        double fitness = -1;

        try {
            // First step: Initialize the CloudSim package. It should be called
            // before creating any entities.
            int num_user = 1;   // number of grid users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;  // mean trace events

            // Initialize the CloudSim library
            CloudSim.init(num_user, calendar, trace_flag);

            // Second step: Create Datacenters
            //Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
            @SuppressWarnings("unused")
            Datacenter datacenter0 = createDatacenter("Datacenter_0");
            @SuppressWarnings("unused")
            //Datacenter datacenter1 = createDatacenter("Datacenter_1");

            //Third step: Create Broker
            DatacenterBroker broker = createBroker();
            int brokerId = broker.getId();

            //Fourth step: Create VMs and Cloudlets and send them to broker
            vmlist = createVM(brokerId, numOfVMs); //creating 20 vms
            cloudletList = createCloudlet(brokerId, numOfCloudlets); // creating 40 cloudlets

            broker.submitVmList(vmlist);
            broker.submitCloudletList(cloudletList);

            if (brokerType == 0) {
                ((MappingBroker) broker).setMapping(mapping);
            }

            // Fifth step: Starts the simulation
            CloudSim.startSimulation();

            // Final step: Print results when simulation is over
            List<Cloudlet> newList = broker.getCloudletReceivedList();

            CloudSim.stopSimulation();

            printCloudletList(newList);

            switch (fitnessType) {
                case 0:
                    fitness = calculateActualMakespan(newList);
                    break;
                case 1:
                    fitness = calculateActualResourceUtilization(newList);
                    fitness = 1.0 / fitness; //for maximization
                    break;
                default:
                    fitness = -1; //error value
            }

            Log.printLine("Simulation Ends!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }

        return fitness;
    }

    /**
     * Prints the Cloudlet objects
     *
     * @param list list of Cloudlets
     */
    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
                + "Data center ID" + indent + "VM ID" + indent + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");

                Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId()
                        + indent + indent + indent + dft.format(cloudlet.getActualCPUTime())
                        + indent + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent + indent + dft.format(cloudlet.getFinishTime()));
            }
        }

    }

    public double calculateActualMakespan(List<Cloudlet> list) {
        double makespan = 0;
        double minStartTime = Double.MAX_VALUE;
        double maxFinishTime = 0;

        for (Cloudlet cloudlet : cloudletList) {

            if (cloudlet.getExecStartTime() < minStartTime) {
                minStartTime = cloudlet.getExecStartTime();
            }

            if (cloudlet.getFinishTime() > maxFinishTime) {
                maxFinishTime = cloudlet.getFinishTime();
            }
        }

        makespan = maxFinishTime - minStartTime;
        return makespan;
    }

    public double calculateActualResourceUtilization(List<Cloudlet> list) {
        double[] ST = new double[numOfVMs];
        double[] CT = new double[numOfVMs];
        for (int i = 0; i < numOfVMs; i++) {
            ST[i] = Double.POSITIVE_INFINITY;
            CT[i] = Double.NEGATIVE_INFINITY;
        }

        for (Cloudlet cloudlet : cloudletList) {
            int vmID = cloudlet.getVmId();
            if (ST[vmID] > cloudlet.getExecStartTime()) {
                ST[vmID] = cloudlet.getExecStartTime();
            }

            if(CT[vmID] < cloudlet.getFinishTime()) {
                CT[vmID] = cloudlet.getFinishTime();
            }
        }

        double utilization = 0;

        for (int i = 0; i < numOfVMs; i++) {
            utilization += CT[i] - ST[i];
        }

        return utilization / (calculateActualMakespan(list) * numOfVMs);

    }

    public double[] getVM_MIPS_POWERS() {
        return VM_MIPS_POWERS;
    }

    public int[] getCLOUDLET_LENGTHS() {
        return CLOUDLET_LENGTHS;
    }

    public double[][] getETC_MATRIX() {
        return ETC_MATRIX;
    }

    public int getNumOfCloudlets() {
        return numOfCloudlets;
    }

    public int getNumOfVMs() {
        return numOfVMs;
    }

    public Random getRng() {
        return rng;
    }

    public double calculatePredictedMakespan(int[] mapping) {
        double[] finishTimes = new double[numOfVMs];

        for (int i = 0; i < numOfCloudlets; i++) {
            if (mapping[i] == numOfVMs) {
                mapping[i] = (int) numOfVMs - 1;
            }
            finishTimes[mapping[i]] += ETC_MATRIX[i][mapping[i]];
        }

        double maxFinishTime = 0;
        for (int i = 0; i < numOfVMs; i++) {
            if (finishTimes[i] > maxFinishTime) {
                maxFinishTime = finishTimes[i];
            }
        }

        return maxFinishTime;
    }

    public double calculatePredictedResourceUtilization(int[] mapping) {
        double[] finishTimes = new double[numOfVMs];

        for (int i = 0; i < numOfCloudlets; i++) {
            if (mapping[i] == numOfVMs) {
                mapping[i] = (int) numOfVMs - 1;
            }
            finishTimes[mapping[i]] += ETC_MATRIX[i][mapping[i]];
        }

        double totalFinishTime = 0;
        for (int i = 0; i < numOfVMs; i++) {
            totalFinishTime += finishTimes[i];
        }

        return totalFinishTime / (calculatePredictedMakespan(mapping) * numOfVMs);
    }

    public double predictFitnessValue(int[] mapping) {
        switch (fitnessType) {
            case 0:
                return calculatePredictedMakespan(mapping);
            case 1:
                return 1.0 / calculatePredictedResourceUtilization(mapping); //maximization
            default:
                return -1; //error value
        }
    }
}
