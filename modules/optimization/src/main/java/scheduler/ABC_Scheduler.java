/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scheduler;

import simulation.Util;
import simulation.Simulation;
import java.util.Random;

/**
 *
 * @author osman
 */
public class ABC_Scheduler extends TaskScheduler {
    
    private int MAX_FES;

    /* Control Parameters of ABC algorithm*/
    int NP = 40;
    /* The number of colony size (employed bees+onlooker bees)*/
    int FoodNumber = NP / 2;
    /*The number of food sources equals the half of the colony size*/
    int limit = 100;
    /*A food source which could not be improved through "limit" trials is abandoned by its employed bee*/
    int FEs = 0;
    /*The number of cycles for foraging {a stopping criteria}*/

 /* Problem specific variables*/
    int D;
    /*The number of parameters of the problem to be optimized*/
    double lb;
    /*lower bound of the parameters. */
    double ub;
    /*upper bound of the parameters. lb and ub can be defined as arrays for the problems of which parameters have different bounds*/

    int dizi1[] = new int[10];
    double Foods[][];
    /*Foods is the population of food sources. Each row of Foods matrix is a vector holding D parameters to be optimized. The number of rows of Foods matrix equals to the FoodNumber*/
    double f[];
    /*f is a vector holding objective function values associated with food sources */
    double fitness[];
    /*fitness is a vector holding fitness (quality) values associated with food sources*/
    double trial[];
    /*trial is a vector holding trial numbers through which solutions can not be improved*/
    double prob[];
    /*prob is a vector holding probabilities of food sources (solutions) to be chosen*/
    double solution[];
    /*New solution (neighbour) produced by v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) j is a randomly chosen parameter and k is a randomlu chosen solution different from i*/

    double ObjValSol;
    /*Objective function value of new solution*/
    double FitnessSol;
    /*Fitness value of new solution*/
    int neighbour, param2change;
    /*param2change corrresponds to j, neighbour corresponds to k in equation v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij})*/

    double GlobalMin;
    /*Optimum solution obtained by ABC algorithm*/
    double GlobalParams[];
    /*Parameters of the optimum solution*/
    double r;

    Random rng; 
    
    public ABC_Scheduler(Simulation sim) {
        super(sim);
        D = sim.getNumOfCloudlets();
        lb = 0;
        ub = sim.getNumOfVMs();
        limit = D * FoodNumber;

        Foods = new double[FoodNumber][D];
        f = new double[FoodNumber];
        fitness = new double[FoodNumber];
        trial = new double[FoodNumber];
        prob = new double[FoodNumber];
        solution = new double[D];
        GlobalParams = new double[D];
        rng = sim.getRng();
    }

    /*Fitness function*/
    double CalculateFitness(double fun) {
        double result = 0;
        if (fun >= 0) {
            result = 1 / (fun + 1);
        } else {

            result = 1 + Math.abs(fun);
        }
        return result;
    }

    /*The best food source is memorized*/
    void MemorizeBestSource() {
        int i, j;

        for (i = 0; i < FoodNumber; i++) {
            if (f[i] < GlobalMin) {
                GlobalMin = f[i];
                for (j = 0; j < D; j++) {
                    GlobalParams[j] = Foods[i][j];
                }
            }
        }
    }

    /*Variables are initialized in the range [lb,ub]. If each parameter has different range, use arrays lb[j], ub[j] instead of lb and ub */
 /* Counters of food sources are also initialized in this function*/
    void init(int index) {
        int j;
        for (j = 0; j < D; j++) {
            r = ((double) rng.nextDouble() * 32767 / ((double) 32767 + (double) (1)));
            Foods[index][j] = r * (ub - lb) + lb;
            solution[j] = Foods[index][j];
        }
        f[index] = calculateFunction(solution);
        fitness[index] = CalculateFitness(f[index]);
        trial[index] = 0;
    }


    /*All food sources are initialized */
    void initial() {
        int i;
        for (i = 0; i < FoodNumber; i++) {
            init(i);
        }
        GlobalMin = f[0];
        for (i = 0; i < D; i++) {
            GlobalParams[i] = Foods[0][i];
        }

    }

    void SendEmployedBees() {
        if (FEs >= MAX_FES) {
            return;
        }
        int i, j;
        /*Employed Bee Phase*/
        for (i = 0; i < FoodNumber; i++) {
            /*The parameter to be changed is determined randomly*/
            r = ((double) rng.nextDouble() * 32767 / ((double) (32767) + (double) (1)));
            param2change = (int) (r * D);

            /*A randomly chosen solution is used in producing a mutant solution of the solution i*/
            r = ((double) rng.nextDouble() * 32767 / ((double) (32767) + (double) (1)));
            neighbour = (int) (r * FoodNumber);

            /*Randomly selected solution must be different from the solution i*/
            // while(neighbour==i)
            // {
            // r = (   (double)rng.nextDouble()*32767 / ((double)(32767)+(double)(1)) );
            // neighbour=(int)(r*FoodNumber);
            // }
            for (j = 0; j < D; j++) {
                solution[j] = Foods[i][j];
            }

            /*v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) */
            r = ((double) rng.nextDouble() * 32767 / ((double) (32767) + (double) (1)));
            solution[param2change] = Foods[i][param2change] + (Foods[i][param2change] - Foods[neighbour][param2change]) * (r - 0.5) * 2;

            /*if generated parameter value is out of boundaries, it is shifted onto the boundaries*/
            if (solution[param2change] < lb) {
                solution[param2change] = lb;
            }
            if (solution[param2change] > ub) {
                solution[param2change] = ub;
            }
            ObjValSol = calculateFunction(solution);
            if (FEs >= MAX_FES) {
                break;
            }
            FitnessSol = CalculateFitness(ObjValSol);

            /*a greedy selection is applied between the current solution i and its mutant*/
            if (FitnessSol > fitness[i]) {

                /*If the mutant solution is better than the current solution i, replace the solution with the mutant and reset the trial counter of solution i*/
                trial[i] = 0;
                for (j = 0; j < D; j++) {
                    Foods[i][j] = solution[j];
                }
                f[i] = ObjValSol;
                fitness[i] = FitnessSol;
            } else {
                /*if the solution i can not be improved, increase its trial counter*/
                trial[i] = trial[i] + 1;
            }

        }

        /*end of employed bee phase*/
    }

    /* A food source is chosen with the probability which is proportioal to its quality*/
 /*Different schemes can be used to calculate the probability values*/
 /*For example prob(i)=fitness(i)/sum(fitness)*/
 /*or in a way used in the metot below prob(i)=a*fitness(i)/max(fitness)+b*/
 /*probability values are calculated by using fitness values and normalized by dividing maximum fitness value*/
    void CalculateProbabilities() {
        int i;
        double maxfit;
        maxfit = fitness[0];
        for (i = 1; i < FoodNumber; i++) {
            if (fitness[i] > maxfit) {
                maxfit = fitness[i];
            }
        }

        for (i = 0; i < FoodNumber; i++) {
            prob[i] = (0.9 * (fitness[i] / maxfit)) + 0.1;
        }

    }

    void SendOnlookerBees() {
        if (FEs >= MAX_FES) {
            return;
        }

        int i, j, t;
        i = 0;
        t = 0;
        /*onlooker Bee Phase*/
        while (t < FoodNumber) {

            r = ((double) rng.nextDouble() * 32767 / ((double) (32767) + (double) (1)));
            if (r < prob[i]) /*choose a food source depending on its probability to be chosen*/ {
                t++;

                /*The parameter to be changed is determined randomly*/
                r = ((double) rng.nextDouble() * 32767 / ((double) (32767) + (double) (1)));
                param2change = (int) (r * D);

                /*A randomly chosen solution is used in producing a mutant solution of the solution i*/
                r = ((double) rng.nextDouble() * 32767 / ((double) (32767) + (double) (1)));
                neighbour = (int) (r * FoodNumber);

                /*Randomly selected solution must be different from the solution i*/
                while (neighbour == i) {
                    //System.out.println(rng.nextDouble()*32767+"  "+32767);
                    r = ((double) rng.nextDouble() * 32767 / ((double) (32767) + (double) (1)));
                    neighbour = (int) (r * FoodNumber);
                }
                for (j = 0; j < D; j++) {
                    solution[j] = Foods[i][j];
                }

                /*v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) */
                r = ((double) rng.nextDouble() * 32767 / ((double) (32767) + (double) (1)));
                solution[param2change] = Foods[i][param2change] + (Foods[i][param2change] - Foods[neighbour][param2change]) * (r - 0.5) * 2;

                /*if generated parameter value is out of boundaries, it is shifted onto the boundaries*/
                if (solution[param2change] < lb) {
                    solution[param2change] = lb;
                }
                if (solution[param2change] > ub) {
                    solution[param2change] = ub;
                }
                ObjValSol = calculateFunction(solution);
                if (FEs >= MAX_FES) {
                    break;
                }
                FitnessSol = CalculateFitness(ObjValSol);

                /*a greedy selection is applied between the current solution i and its mutant*/
                if (FitnessSol > fitness[i]) {
                    /*If the mutant solution is better than the current solution i, replace the solution with the mutant and reset the trial counter of solution i*/
                    trial[i] = 0;
                    for (j = 0; j < D; j++) {
                        Foods[i][j] = solution[j];
                    }
                    f[i] = ObjValSol;
                    fitness[i] = FitnessSol;
                } else {
                    /*if the solution i can not be improved, increase its trial counter*/
                    trial[i] = trial[i] + 1;
                }
            }
            /*if */
            i++;
            if (i == FoodNumber) {
                i = 0;
            }
        }/*while*/

 /*end of onlooker bee phase     */
    }

    /*determine the food sources whose trial counter exceeds the "limit" value. In Basic ABC, only one scout is allowed to occur in each cycle*/
    void SendScoutBees() {
        int maxtrialindex, i;
        maxtrialindex = 0;
        for (i = 1; i < FoodNumber; i++) {
            if (trial[i] > trial[maxtrialindex]) {
                maxtrialindex = i;
            }
        }
        if (trial[maxtrialindex] >= limit) {
            init(maxtrialindex);
        }
    }

    double calculateFunction(double sol[]) {
        FEs++;
        return sim.predictFitnessValue(Util.discretizeSol(sol));
    }

    @Override
    public int[] schedule(int MAX_FES) {
        this.MAX_FES = MAX_FES;

        initial();
        MemorizeBestSource();

        while (FEs < MAX_FES) {
            SendEmployedBees();
            CalculateProbabilities();
            SendOnlookerBees();
            MemorizeBestSource();
            SendScoutBees();
        }

        int[] mapping = Util.discretizeSol(GlobalParams);

        return mapping;
    }
}
