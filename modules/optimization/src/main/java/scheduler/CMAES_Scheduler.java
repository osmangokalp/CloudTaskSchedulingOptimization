package scheduler;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer;
import org.apache.commons.math3.random.MersenneTwister;
import simulation.Simulation;
import simulation.Util;

import java.util.Arrays;

public class CMAES_Scheduler extends TaskScheduler{
    public CMAES_Scheduler(Simulation sim) {
        super(sim);
    }

    @Override
    public int[] schedule(int MAX_FES) {

        int NP = 20;
        int dimension = sim.getNumOfCloudlets();
        double upperBound = sim.getNumOfVMs();
        double lowerBound = 0;

        double lowerBounds[] = new double[dimension];
        Arrays.fill(lowerBounds, lowerBound);

        double upperBounds[] = new double[dimension];
        Arrays.fill(upperBounds, upperBound);

        double sigmas[] = new double[dimension];
        Arrays.fill(sigmas, sim.getRng().nextDouble() * (upperBound - lowerBound));

        double initialValues[] = new double[dimension];
        for (int i = 0; i < dimension; i++) {
            initialValues[i] = sim.getRng().nextDouble() * (upperBound - lowerBound) + lowerBound;
        }

        CMAESOptimizer es = new CMAESOptimizer(MAX_FES, 0, true, 1000, 1,
                new MersenneTwister(sim.getRng().nextInt()), true, new SimpleValueChecker(0, 0));

        PointValuePair result = es.optimize(
                new MaxEval(MAX_FES),
                new CMAESOptimizer.PopulationSize(NP),
                new CMAESOptimizer.Sigma(sigmas),
                new InitialGuess(initialValues),
                new SimpleBounds(lowerBounds, upperBounds),
                new ObjectiveFunction(new TaskSchedulerObjectiveFunction(sim)),
                GoalType.MINIMIZE
        );

        double[] resultingValues = result.getPoint();
        double resultingF = sim.predictFitnessValue(Util.discretizeSol(resultingValues));

        int[] mapping = Util.discretizeSol(resultingValues);

        return mapping;

    }
}

  class TaskSchedulerObjectiveFunction implements MultivariateFunction {
    private Simulation sim;

    public TaskSchedulerObjectiveFunction(Simulation sim) {
        this.sim = sim;
    }

    @Override
    public double value(double[] x) {
        return sim.predictFitnessValue(Util.discretizeSol(x));
    }

}
