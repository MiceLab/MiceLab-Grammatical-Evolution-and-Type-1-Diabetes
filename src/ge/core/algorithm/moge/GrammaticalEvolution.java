package ge.core.algorithm.moge;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Logger;

import ge.core.operator.assigner.CrowdingDistance;
import ge.core.operator.assigner.FrontsExtractor;
import ge.core.operator.comparator.ComparatorNSGAII;
import ge.core.operator.comparator.SolutionDominance;
import ge.core.operator.crossover.CrossoverOperator;
import ge.core.operator.crossover.SinglePointCrossover;
import ge.core.operator.mutation.IntegerFlipMutation;
import ge.core.operator.mutation.MutationOperator;
import ge.core.operator.selection.BinaryTournament;
import ge.core.operator.selection.SelectionOperator;
import ge.core.problem.Problem;
import ge.core.problem.Solution;
import ge.core.problem.Solutions;
import ge.core.problem.Variable;

/**
 * Multi-objective Grammatical Evolution Algorithm.
 * Based on NSGA-II
 * @author Ivan Contreras
 *
 */
public class GrammaticalEvolution extends Algorithm<Variable<Integer>> {
  
  private static final Logger logger = Logger.getLogger(NSGAII.class.getName());

  /////////////////////////////////////////////////////////////////////////
  protected int maxGenerations;
  protected int maxPopulationSize;
  /////////////////////////////////////////////////////////////////////////
  protected Comparator<Solution<Variable<Integer>>> dominance;
  protected int currentGeneration;
  protected Solutions<Variable<Integer>> population;
  public Solutions<Variable<Integer>> getPopulation() { return population; }
  protected MutationOperator<Variable<Integer>> mutationOperator;
  protected CrossoverOperator<Variable<Integer>> crossoverOperator;
  protected SelectionOperator<Variable<Integer>> selectionOperator;

  public GrammaticalEvolution(Problem<Variable<Integer>> problem, int maxPopulationSize, int maxGenerations, double probMutation, double probCrossover) {
      super(problem);
      this.maxPopulationSize = maxPopulationSize;
      this.maxGenerations = maxGenerations;
      this.mutationOperator = new IntegerFlipMutation<Variable<Integer>>(problem, probMutation);
      this.crossoverOperator = new SinglePointCrossover<Variable<Integer>>(false,probCrossover);
      this.selectionOperator = new BinaryTournament<Variable<Integer>>();
  }

  public GrammaticalEvolution(Problem<Variable<Integer>> problem, int maxPopulationSize, int maxGenerations) {
    this(problem, maxPopulationSize, maxGenerations, 1.0/problem.getNumberOfVariables(), SinglePointCrossover.DEFAULT_PROBABILITY);
  }

  @Override
  public void initialize() {
      dominance = new SolutionDominance<Variable<Integer>>();
      // Create the initial solutionSet
      population = problem.newRandomSetOfSolutions(maxPopulationSize);
      problem.evaluate(population);
      // Compute crowding distance
      CrowdingDistance<Variable<Integer>> assigner = new CrowdingDistance<Variable<Integer>>(problem.getNumberOfObjectives());
      assigner.execute(population);
      currentGeneration = 0;
  }

  
  public void initialize2(Solutions<Variable<Integer>> solutions) {
      dominance = new SolutionDominance<Variable<Integer>>();
      // Create the initial solutionSet
      population =solutions;
      problem.evaluate(population);
      // Compute crowding distance
      CrowdingDistance<Variable<Integer>> assigner = new CrowdingDistance<Variable<Integer>>(problem.getNumberOfObjectives());
      assigner.execute(population);
      currentGeneration = 0;
  }
  
  public void percentage(){
      int percentage = Math.round((currentGeneration * 100) / maxGenerations);
      logger.info(percentage + "% performed ... "); 
  }
  
  @Override
  public Solutions<Variable<Integer>> execute() {
      int nextPercentageReport = 2;
      while (currentGeneration < maxGenerations) {
          step();
          int percentage = Math.round((currentGeneration * 100) / maxGenerations);
          Double bestObj = population.get(0).getObjectives().get(0);
          if (percentage == nextPercentageReport) {
              logger.info(percentage + "% PERFORMED");
              nextPercentageReport += 2;
          }

      }
      return this.getCurrentSolution();
  }

  public Solutions<Variable<Integer>> getCurrentSolution() {
      population.reduceToNonDominated(dominance);
      return population;
  }

  public void step() {
      currentGeneration++;
      // Create the offSpring solutionSet
      if (population.size() < 2) {
          logger.severe("Generation: " + currentGeneration + ". Population size is less than 2.");
          return;
      }

      
      Solutions<Variable<Integer>> childPop = new Solutions<Variable<Integer>>();
      Solution<Variable<Integer>> parent1, parent2;

      for (int i = 0; i < (maxPopulationSize / 2); i++) {
          //obtain parents
          parent1 = selectionOperator.execute(population).get(0);
          parent2 = selectionOperator.execute(population).get(0);
          Solutions<Variable<Integer>> offSpring = crossoverOperator.execute(parent1, parent2);
          for (Solution<Variable<Integer>> solution : offSpring) {
              mutationOperator.execute(solution);
              childPop.add(solution);
          }
      } // for
      
      problem.evaluate(childPop);

      // Create the solutionSet union of solutionSet and offSpring
      Solutions<Variable<Integer>> mixedPop = new Solutions<Variable<Integer>>();
      mixedPop.addAll(population);
      mixedPop.addAll(childPop);

      // Reducing the union
      population = reduce(mixedPop, maxPopulationSize);
      logger.fine("Generation " + currentGeneration + "/" + maxGenerations + "\n" + population.toString());
  } // step

  public Solutions<Variable<Integer>> reduce(Solutions<Variable<Integer>> pop, int maxSize) {
      FrontsExtractor<Variable<Integer>> extractor = new FrontsExtractor<Variable<Integer>>(dominance);
      ArrayList<Solutions<Variable<Integer>>> fronts = extractor.execute(pop);

      Solutions<Variable<Integer>> reducedPop = new Solutions<Variable<Integer>>();
      CrowdingDistance<Variable<Integer>> assigner = new CrowdingDistance<Variable<Integer>>(problem.getNumberOfObjectives());
      Solutions<Variable<Integer>> front;
      int i = 0;
      while (reducedPop.size() < maxSize && i < fronts.size()) {
          front = fronts.get(i);
          assigner.execute(front);
          reducedPop.addAll(front);
          i++;
      }

      ComparatorNSGAII<Variable<Integer>> comparator = new ComparatorNSGAII<Variable<Integer>>();
      if (reducedPop.size() > maxSize) {
          Collections.sort(reducedPop, comparator);
    //      for(int g=0; g<reducedPop.size();g++)
     //   	  System.out.println(reducedPop.get(i).toString());
          while (reducedPop.size() > maxSize) {
              reducedPop.remove(reducedPop.size() - 1);
          }
      }
      FileWriter fichero = null;
      PrintWriter pw = null;
      try
      {
    	  fichero = new FileWriter("prueba.txt",true);
    	  pw = new PrintWriter(fichero);
    	  pw.println(reducedPop.get(0));
      } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}finally {
      if (null != fichero)
		try {
			fichero.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      }
      return reducedPop;
  }

  public void setMutationOperator(MutationOperator<Variable<Integer>> mutationOperator) {
      this.mutationOperator = mutationOperator;
  }

  public void setCrossoverOperator(CrossoverOperator<Variable<Integer>> crossoverOperator) {
      this.crossoverOperator = crossoverOperator;
  }

  public void setSelectionOperator(SelectionOperator<Variable<Integer>> selectionOperator) {
      this.selectionOperator = selectionOperator;
  }

  public void setMaxGenerations(int maxGenerations) {
      this.maxGenerations = maxGenerations;
  }

  public void setMaxPopulationSize(int maxPopulationSize) {
      this.maxPopulationSize = maxPopulationSize;
  }
}
