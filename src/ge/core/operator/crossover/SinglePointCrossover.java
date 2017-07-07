package ge.core.operator.crossover;

import ge.core.problem.Solution;
import ge.core.problem.Solutions;
import ge.core.problem.Variable;
import ge.core.util.random.RandomGenerator;

public class SinglePointCrossover<T extends Variable<?>> extends CrossoverOperator<T> {

    public static final double DEFAULT_PROBABILITY = 0.9;
    public static final boolean DEFAULT_FIXED_CROSSOVER_POINT = true;
    protected double probability;
    protected boolean fixedCrossoverPoint;

    public SinglePointCrossover() {
        probability = DEFAULT_PROBABILITY;
        fixedCrossoverPoint = DEFAULT_FIXED_CROSSOVER_POINT;
    } // SinglePointCrossover

    public SinglePointCrossover(boolean fixedCrossoverPoint, double probability) {
        this.probability = probability;
        this.fixedCrossoverPoint = fixedCrossoverPoint;
    }  // SinglePointCrossover

    private int computeCrossoverPoint(int sol1Length, int sol2Length) {
        int point = 0;
        if (sol1Length < sol2Length) {
            point = RandomGenerator.nextInt(0, sol1Length);
        } else {
            point = RandomGenerator.nextInt(0, sol1Length);
        }
        return point;
    }

    /**
     * Creates the new solution. Either with fixed crossver point or not.
     * @param sol1 Chromosome 1
     * @param sol2 Chromosome 2
     **/
    private void makeNewSolution(Solution<T> sol1, Solution<T> sol2) {
        int point1, point2;
        int sol1Length = sol1.getVariables().size();
        int sol2Length = sol2.getVariables().size();

        if (fixedCrossoverPoint) {
            point1 = computeCrossoverPoint(sol1Length, sol2Length);
            T tmp1, tmp2;
            for (int i = 0; i < point1; i++) {
                tmp1 = sol1.getVariables().get(i);
                tmp2 = sol2.getVariables().get(i);
                sol1.getVariables().set(i, tmp2);
                sol2.getVariables().set(i, tmp1);
            }
        } else {
            // TODO: review this algorithm !!!
            point1 = RandomGenerator.nextInt(sol1Length);
           // point2 = RandomGenerator.nextInt(sol2Length);

            Solution<T> tmp1 = sol1.clone();
            Solution<T> tmp2 = sol2.clone();

            int index1 = 0;
            int index2 = 0;

            for (int i = 0; i < point1; i++) {
                sol1.getVariables().set(index1++, tmp1.getVariables().get(i));
            }
            for (int i = point1; i < sol2Length; i++) {
                sol1.getVariables().set(index1++, tmp2.getVariables().get(i));
            }
            for (int i = 0; i < point1; i++) {
                sol2.getVariables().set(index2++, tmp2.getVariables().get(i));
            }

            for (int i = point1; i < sol1Length; i++) {
                sol2.getVariables().set(index2++, tmp1.getVariables().get(i));
            }
        }
    }

    public Solutions<T> doCrossover(double probability, Solution<T> parent1, Solution<T> parent2) {

        Solutions<T> offSpring = new Solutions<T>();

        Solution<T> offSpring0 = parent1.clone();
        Solution<T> offSpring1 = parent2.clone();

        if (RandomGenerator.nextDouble() <= probability) {
            if (RandomGenerator.nextBoolean()) {
                this.makeNewSolution(offSpring0, offSpring1);
            } else {
                this.makeNewSolution(offSpring1, offSpring0); // if
            }
        }
        offSpring.add(offSpring0);
        offSpring.add(offSpring1);
        return offSpring;
    } // doCrossover

    /**
     * Executes the operation
     * @param object An object containing an array of two parents
     * @return An object containing the offSprings
     */
    public Solutions<T> execute(Solution<T> parent1, Solution<T> parent2) {
        return doCrossover(probability, parent1, parent2);
    } // execute
    
    public void setProbability(double probability) {
    	this.probability = probability;
    }
} // SBXCrossover

