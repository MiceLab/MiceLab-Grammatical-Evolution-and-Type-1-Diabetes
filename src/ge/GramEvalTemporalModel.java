package ge;

import data.TablaDatos;
import ge.core.algorithm.moge.AbstractProblemGE;
import ge.core.algorithm.moge.GrammaticalEvolution;
import ge.core.algorithm.moge.Phenotype;
import ge.core.operator.assigner.CrowdingDistance;
import ge.core.operator.assigner.FrontsExtractor;
import ge.core.operator.comparator.SimpleDominance;
import ge.core.operator.comparator.SolutionDominance;
import ge.core.operator.crossover.SinglePointCrossover;
import ge.core.operator.mutation.IntegerFlipMutation;
import ge.core.operator.selection.BinaryTournament;
import ge.core.problem.Solution;
import ge.core.problem.Solutions;
import ge.core.problem.Variable;
import ge.core.util.Maths;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Observer;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.SimpleCompiler;

import charts.ClarkeFrame;
import charts.ScopeV2;
import logger.core.MyLogger;
import util.UtilStats;

/**
 * FUNCIONES MÄS IMPORTANTES 
 * 
 * createDataPlot
 * validateSolutions
 * validateAndReturnEvaluations
 * segundavuelta
 * evaluate
 * main
 * runGE
 * loadProperties
 * 
 * @author micelab1
 *
 */

public class GramEvalTemporalModel extends AbstractProblemGE {

	private static final int LapseTime = 5;
	//DONDE EMPIEZA (para que tengamos historico  24*5min==2horas)
    private static final int HISTORIC_HORIZON = 90/LapseTime;
    
    //Memoria de fitness ya evaluados
    private static Map<String, Double> cache = new HashMap<String, Double>();
    final int sizeMemory=1000;
    //Desde donde empieza a contara la predicción("no nos interesa" lo que pasa en 10 minutos 12*5=1 hora)
    private static final int PREDICTION_HORIZON = 120/LapseTime;
    //Hasta donde realizamos la predicción (2horas)
    // private static final int max_PREDICTION_THERSHOLD = 12;
    private static final Logger logger = Logger.getLogger(GramEvalTemporalModel.class.getName());
	//Numero de objetivos ( 1 solo min&max// 2 Error Grid)
    private static int OBJS=1;
    
	private static GrammaticalEvolution algorithm;
	private static GrammaticalEvolution algorithm2;
	
    //private static SimpleGeneticAlgorithm<Variable<Integer>> algorithm;

   
    protected static long threadId;
    protected static EvaluateDataTable dataTable;
    protected Properties properties;
    protected AbstractPopEvaluator evaluator;

    public GramEvalTemporalModel(Properties properties, long l) throws IOException {
        super(properties.getProperty(data.Common.BNF_PATH_FILE_PROP), OBJS);
        this.properties = properties;
        
        boolean normalizeData = false;
        
        if (properties.getProperty(data.Common.NORMALIZED_DATA_PROP) != null) {
            normalizeData = Boolean.valueOf(properties.getProperty(data.Common.NORMALIZED_DATA_PROP));
        } 
        dataTable = new EvaluateDataTable(this, 
        		properties.getProperty(data.Common.TRAINING_PATH_PROP),
                properties.getProperty(data.Common.VALIDATION_PATH_PROP),
                Double.valueOf(properties.getProperty(data.Common.ERROR_THRESHOLD_PROP)),
                Boolean.valueOf(properties.getProperty(data.Common.VIEW_RESULTS_PROP)),normalizeData,threadId,
                PREDICTION_HORIZON, HISTORIC_HORIZON,LapseTime);
    }


    boolean generateAndCompileEvaluationFile(ArrayList<String> exprList) {
        return true;
    }
    
    
    public StringBuffer createCode2(StringBuffer code, Solutions<Variable<Integer>> solutions) {    
	    code.append("\tpublic double evaluate(int idxExpr, int k) {\n");
	    code.append("\t\tdouble result = 0.0;"
	    		+"\t\tdouble resultGL = 0.0;"
	    		+"\t\tdouble resultC = 0.0;"
	    		+"\t\tdouble resultU = 0.0;"
	    		+"\t\tint before = 0;"
	    		+"\t\tint tiempo = 0;"
	    		+"\t\tdouble aux = 0.0;"
	    		+"\t\tdouble a1,a2=0.0;"
	    		+ "\n\t\tint varId=0; "
	    		+ "\n\t\tint cIni=0, aIni=0, cEnd=0, aEnd=0;\n");
	    code.append("\t\tjava.lang.reflect.Method method;\n");
	    code.append("\t\ttry {\n");
	    code.append("\t\t\tmethod = this.getClass().getMethod(\"f\"+idxExpr,int.class);\n");
	    code.append("\t\tmethod.invoke(this,k);\n\t");
	    code.append("\t\t}catch (Exception e) {}\n");
	    code.append("\t\treturn fit;\n\t}\n");
	    	    
	    for (int i = 0; i < solutions.size(); ++i) {
	    	code.append("\tpublic void f").append(i).append("(int k){\n");
	        code.append("\t\tdouble result = 0.0;\n");
	        String phenotype=generatePhenotype(solutions.get(i)).toString();
//	        if (cache.containsKey(phenotype)){
//                code.append("result="+cache.get(phenotype)+";");
//                System.out.println("CACHE---------------->"+cache.get(phenotype));
//            }   
//            else if (correctSol) 
            	code.append(phenotype);
	    	code.append("\t\tfit=result;\n");
	    	code.append("\t}\n");
	    }
	    code.append("}\n"); //End class
return code;
    }

    
    /**
     * Funcion para evaluar la poblacion (una vez por generacion)
     */
  /*  @Override
    public void evaluate(Solutions<Variable<Integer>> solutions) {
                 
                
        for (int i = 0; i < solutions.size(); ++i) {
                Solution<Variable<Integer>> solution = solutions.get(i);
                double fitness = dataTable.evaluate(threadId, solution, i, INI_TRAIN , min_PREDICTION_THERSHOLD , max_PREDICTION_THERSHOLD,"");          
                if (Double.isNaN(fitness)) {
                    logger.info("I have a NaN number here");
                }
                solution.getObjectives().set(0,fitness);
               if(OBJS==2)
                solution.getObjectives().set(1,dataTable.getFitness2());             
         }
    }*/
    @Override
    public void evaluate(Solutions<Variable<Integer>> solutions) {
        double start=System.currentTimeMillis();
    	ArrayList<String> solStrings = new ArrayList<>();
        double resultAux=0;
        
      /*  for (int i = 0; i < solutions.size(); ++i) {
            Solution<Variable<Integer>> solution = solutions.get(i);           
            Phenotype phenotype = generatePhenotype(solution);
            
            int id=solutions.get(i).getid();
   
            if (cache.containsKey(phenotype.toString())){
                solStrings.add("result="+cache.get(phenotype.toString())+";");
                System.out.println(cache.get(phenotype.toString()));
            }   
            else if (correctSol) {
                solStrings.add(phenotype.toString());
            } else {
                solStrings.add("result=Double.POSITIVE_INFINITY;\n");
            }
        }     */ 

        // And now we evaluate all the solutions with the compiled file:
        evaluator = null;
        EvaluationCode code = new EvaluationCode();
        StringBuffer codeS=code.createCode1(threadId);
        codeS=createCode2(codeS, solutions);
        SimpleCompiler compiler = new SimpleCompiler();
        try {
            	String aux = codeS.toString();
  //          	System.out.println("\n\n\n\n\n\n\n\n\n"+aux);
    			compiler.cook(new StringReader(aux));		
    			Class cl = compiler.getClassLoader().loadClass("algorithm.PopEvaluator"+threadId);
    			evaluator = (AbstractPopEvaluator) cl.newInstance();
    			evaluator.setLimits(HISTORIC_HORIZON ,PREDICTION_HORIZON);
    			evaluator.setTimeTables(dataTable.getTrainingTable());
    		} catch (ClassNotFoundException e) {
    			e.printStackTrace();
    		} catch (CompileException e) {
        		e.printStackTrace();
        	} catch (IOException e) {
        		e.printStackTrace();
        	} catch (InstantiationException e) {
    			e.printStackTrace();
    		} catch (IllegalAccessException e) {
    			e.printStackTrace();
    	}
            
        for (int i = 0; i < solutions.size(); ++i) {
        	
        	 if (cache.containsKey(generatePhenotype(solutions.get(i)).toString())==false){
                double fitness = dataTable.evaluate(evaluator, solutions.get(i), i ,"",0);
                if (Double.isNaN(fitness)) {
                    logger.info("I have a NaN number here");
                }
                solutions.get(i).getObjectives().set(0,fitness);    
             // int id=solutions.get(i).getid();
                if(cache.size()>sizeMemory)
                	cache.clear();
               	cache.put(generatePhenotype(solutions.get(i)).toString(), fitness); 
            //    solution.getObjectives().set(1,fitness);
               if(OBJS==2)
                solutions.get(i).getObjectives().set(1,dataTable.getFitness2());             
        	 }
        	 else{
        		  double fitness=cache.get(generatePhenotype(solutions.get(i)).toString());
                  solutions.get(i).getObjectives().set(0,fitness);    
        	 }
        }

    }

    @Override
    public void evaluate(Solution<Variable<Integer>> solution) {
        logger.severe("The solutions should be already evaluated. You should not see this message.");
    }

    @Override
    public void evaluate(Solution<Variable<Integer>> solution, Phenotype phenotype) {
        logger.severe("The solutions should be already evaluated. You should not see this message.");
    }

    @Override
    public GramEvalTemporalModel clone() {
        GramEvalTemporalModel clone = null;
        try {
            clone = new GramEvalTemporalModel(properties, threadId + 1);
        } catch (IOException ex) {
            logger.severe(ex.getLocalizedMessage());
        }
        return clone;
    }

    /**
     * Carga los parametroS del algoritmo de optimización mediante un fichero de propiedades
     * @param propertiesFilePath
     * @return
     */
    public static Properties loadProperties(String propertiesFilePath) {
        Properties properties = new Properties();
        try {
            properties.load(new BufferedReader(new FileReader(new File(propertiesFilePath))));
            File clsDir = new File(properties.getProperty(data.Common.WORK_DIR_PROP));
            URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Class<URLClassLoader> sysclass = URLClassLoader.class;
            Method method = sysclass.getDeclaredMethod("addURL", new Class[]{URL.class});
            method.setAccessible(true);
            method.invoke(sysloader, new Object[]{clsDir.toURI().toURL()});
        } catch (Exception ex) {
            logger.severe(ex.getLocalizedMessage());
        }
        return properties;
    }
    
    
    /**
     * Inicia el algoritmo de optimización
     * @param properties
     * @param threadId
     * @param obs
     */
    public static void runGE(Properties properties, int threadId, Observer obs, int patient) {
        MyLogger.setup(properties.getProperty(data.Common.LOGGER_BASE_PATH_PROP) + "_" + threadId + ".log", Level.parse(properties.getProperty(data.Common.LOGGER_LEVEL_PROP)));

        GramEvalTemporalModel problem = null;
        try {
            problem = new GramEvalTemporalModel(properties, threadId);
        } catch (IOException ex) {
            logger.severe(ex.getLocalizedMessage());
        }
        
        // Adjust some properties
        double crossOverProb = SinglePointCrossover.DEFAULT_PROBABILITY;
        if (properties.getProperty(data.Common.CROSSOVER_PROB_PROP) != null) {
            crossOverProb = Double.valueOf(properties.getProperty(data.Common.CROSSOVER_PROB_PROP));
        }
        
       double mutationProb = 1.0 / problem.reader.getRules().size();
        if (properties.getProperty(data.Common.MUTATION_PROB_PROP) != null) {
            mutationProb = Double.valueOf(properties.getProperty(data.Common.MUTATION_PROB_PROP));
        }
        algorithm = new GrammaticalEvolution(problem, Integer.valueOf(properties.getProperty(data.Common.NUM_INDIVIDUALS_PROP)), Integer.valueOf(properties.getProperty(data.Common.NUM_GENERATIONS_PROP)), mutationProb, crossOverProb);       
        algorithm.initialize();
        Solutions<Variable<Integer>> solutions = algorithm.execute();     
        OBJS=1;
      //  solutions= segundavuelta(properties, threadId, obs,solutions);
        
        String[] solsForTable = new String[]{String.valueOf(solutions.get(0).getObjective(0)),problem.generatePhenotype(solutions.get(0)).toString()};
        solutionsTableModel.setRowCount(0);
        solutionsTableModel.addRow(solsForTable);
        
        // Now we evaluate the solution in the validation data
        logger.info("Validation of solutions[0] with fitness " + solutions.get(0).getObjective(0));
        problem.evaluate(solutions);
        Solution<Variable<Integer>> solution = solutions.get(0);
       
        try {
        	String a=properties.getProperty(data.Common.VALIDATION_PATH_PROP);
			valuationTables(a, problem,solution,patient);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
    }

    
    /**
     * @param problem 
     * @param solution 
     * @throws IOException 
     * 
     */
    public static void valuationTables( String validationPath, GramEvalTemporalModel problem, Solution<Variable<Integer>> solution,int patient) throws IOException{
//GramEvalTemporalModel.dataTable.fillDataTableV();
        double validationFitness = GramEvalTemporalModel.dataTable.evaluate(problem.evaluator,solution, 0,"validation2",patient);
        logger.info("Training fitness for solutions[0] = " + validationFitness);
		problem.evaluator.setTimeTables(GramEvalTemporalModel.dataTable.getValidationTables());
        validationFitness = GramEvalTemporalModel.dataTable.evaluate(problem.evaluator,solution, 0,"validation",patient); 
        logger.info("Validation fitness for solutions[0] = " + validationFitness);
    }
    
    
    
    /**
     * Una busqueda sobre un conjunto de soluciones ya optimizado
     * @param properties
     * @param threadId
     * @param obs
     * @param solutions
     * @return
     */
    private static Solutions<Variable<Integer>> segundavuelta(Properties properties, int threadId, Observer obs, Solutions<Variable<Integer>> solutions) {
        
        GramEvalTemporalModel problem = null;
        try {
            problem = new GramEvalTemporalModel(properties, threadId);
        } catch (IOException ex) {
            logger.severe(ex.getLocalizedMessage());
        }
        
        // Adjust some properties
        double crossOverProb = SinglePointCrossover.DEFAULT_PROBABILITY;
        if (properties.getProperty(data.Common.CROSSOVER_PROB_PROP) != null) {
            crossOverProb = Double.valueOf(properties.getProperty(data.Common.CROSSOVER_PROB_PROP));
        }
        double mutationProb = 1.0 / problem.reader.getRules().size();
        if (properties.getProperty(data.Common.MUTATION_PROB_PROP) != null) {
            mutationProb = Double.valueOf(properties.getProperty(data.Common.MUTATION_PROB_PROP));
        }
        
       algorithm2 = new GrammaticalEvolution(problem, Integer.valueOf(properties.getProperty(data.Common.NUM_INDIVIDUALS_PROP)), Integer.valueOf(properties.getProperty(data.Common.NUM_GENERATIONS_PROP)), mutationProb, crossOverProb);    
       algorithm2.initialize2(solutions);
       return algorithm2.execute();
	}


	private static DefaultTableModel solutionsTableModel = new DefaultTableModel();
    
    public static void setViewTable(JTable tableSolutions) {
        solutionsTableModel.setColumnIdentifiers(new String[]{"Obj.","Solution"});
        tableSolutions.setModel(solutionsTableModel);
    }
    
    /**
     * 
     * @param strSols
     * @return
     */
    public boolean validateSolutions(ArrayList<String> strSols) {

            evaluator = null;
            EvaluationCode code = new EvaluationCode();
            code.createCode1(threadId);
            //code.createCode2(strSols);
           
            //compilation
            SimpleCompiler compiler = new SimpleCompiler();
            try {
            	String aux = code.getCode();
        			compiler.cook(new StringReader(aux));		
        			Class cl = compiler.getClassLoader().loadClass("algorithm.PopEvaluator"+threadId);
        			evaluator = (AbstractPopEvaluator) cl.newInstance();
        			evaluator.setTimeTables(dataTable.getTrainingTable());
        		} catch (ClassNotFoundException e) {
        			e.printStackTrace();
        		} catch (CompileException e) {
            		e.printStackTrace();
            	} catch (IOException e) {
            		e.printStackTrace();
            	} catch (InstantiationException e) {
        			e.printStackTrace();
        		} catch (IllegalAccessException e) {
        			e.printStackTrace();
        	}
            return true;
    }
    

	public static double sigmoide(double x,double centro,double pendiente){
		return (1 / (1 + Math.exp(-pendiente*(x-centro))));
	}
	
    public static double penaltyDelFalvero(double real, double pred){
    	double tetaH=155,betaH=100,tetaL=85,betaL=30,lambdaH=20,lambdaL=10,alphaL=1.5,alphaH=1;
    	double Aux1zona1=sigmoide(real,tetaL-(betaL/2),-0.3);
    	double Aux2zona1=sigmoide(pred,real+(lambdaL/2),0.6);
    	double zona1=alphaL*Aux1zona1*Aux2zona1;
    	double Aux1zona2=sigmoide(real,tetaH+(betaH/2),0.1);
    	double Aux2zona2=sigmoide(pred,real-(lambdaH/2),-0.4);
    	double zona2=alphaH*Aux1zona2*Aux2zona2;
    	double zona3=1;
    	//System.out.println(zona1+zona2+zona3);
    	return zona1+zona2+zona3;
    }

    
    /**
     * Función principal del programa
     * @param args
     */
    public static void main(String[] args) {
    	
    GramEvalTemporalModel.threadId = System.currentTimeMillis();
    
    for(int patient=1; patient<100  ;patient++){	
        String propertiesFilePath = "Data/properties"+patient+".properties";
	        int threadId = 1;
	        if (args.length == 1) {
	            propertiesFilePath = args[0];
	        } else if (args.length >= 2) {
	            propertiesFilePath = args[0];
	            threadId = Integer.valueOf(args[1]);
	        }
	        Properties properties = loadProperties(propertiesFilePath);
	        runGE(properties,threadId,null,patient);
	    }
    }
}
