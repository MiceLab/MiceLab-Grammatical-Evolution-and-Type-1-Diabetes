package ge.core.operator.mutation;

import ge.core.problem.Solution;
import ge.core.problem.Variable;
import ge.core.util.random.RandomGenerator;

//Solutions must be numeric
public class SwapMutation<T extends Variable<?>> extends MutationOperator<T> {
	/**
	 * Constructor
	 * Creates a new IntegerFlipMutation mutation operator instance
	 */
	public SwapMutation(double probability) {
		super(probability);
	} // IntegerFlipMutation

	public Solution<T> execute(Solution<T> solution) {
		if (RandomGenerator.nextDouble() < probability) {
			int indexI = RandomGenerator.nextInt(solution.getVariables().size());
			int indexJ = RandomGenerator.nextInt(solution.getVariables().size());
			if (indexI != indexJ) {
				T varI = solution.getVariables().get(indexI);
				solution.getVariables().set(indexI, solution.getVariables().get(indexJ));
				solution.getVariables().set(indexJ, varI);
			}
		}
		return solution;
	} // execute
} // IntegerFlipMutation

