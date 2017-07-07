package ge.core.algorithm.moge;

import ge.core.problem.Problem;
import ge.core.problem.Solutions;
import ge.core.problem.Variable;

/**
 *
 * @author José L. Risco-Martín
 *
 */
public abstract class Algorithm<V extends Variable<?>> {

  protected Problem<V> problem = null;

  public Algorithm(Problem<V> problem) {
    this.problem = problem;
  }

  public void setProblem(Problem<V> problem) {
    this.problem = problem;
  }

  public abstract void initialize();

  public abstract void step();

  public abstract Solutions<V> execute();
}
