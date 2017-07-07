package ge.core.operator.crossover;

import ge.core.problem.Solution;
import ge.core.problem.Solutions;
import ge.core.problem.Variable;
 /**
  * El cruce sí necesita plantillas porque se accede continuamente a las variables
  * @author jlrisco
  * @param <T>
  */

public abstract class CrossoverOperator<T extends Variable<?>> {
    abstract public Solutions<T> execute(Solution<T> parent1, Solution<T> parent2);
}
