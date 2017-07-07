package ge.core.operator.selection;

import ge.core.problem.Solutions;
import ge.core.problem.Variable;

public abstract class SelectionOperator<T extends Variable<?>> {
    abstract public Solutions<T> execute(Solutions<T> solutions);
}
