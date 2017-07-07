package ge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.logging.Logger;

import charts.PanelGrafica;
import charts.PlotsJFrame;
import charts.ScopeV2;
import data.TablaDatos;
import ge.core.algorithm.moge.AbstractProblemGE;
import ge.core.problem.Solution;
import ge.core.problem.Variable;


/**
 * FUNCIONES MÄS IMPORTANTES 
 * 
 * fillDataTable
 * computeCorelationBetweenCurves
 * ClarkParK
 * computeFitness 
 * evaluate
 * 
 * @author micelab1
 *
 */
public class EvaluateDataTable {

    private static final Logger logger = Logger.getLogger(EvaluateDataTable.class.getName());


    private static long threadId;
	private static final int SALTOS = 1;
	private static final int lowerBound = -1;
	private static final int upperBound =2401;
	private static final int normalised=0;

	static ArrayList<ScopeV2> listaVentanas;

	protected static int predictionHorizon = 0;
	protected static int historicHorizon = 0;
	protected static int LapseTime;
	private static ArrayList<PanelGrafica> listaVentanas2;
    protected AbstractProblemGE problem;
    protected String trainingPath = null;
    protected String validationPath = null;
    protected boolean viewResults = false;
    protected double errorThreshold = 0;
  //protected ArrayList<double[]> trainingTable = new ArrayList<>();
    protected ArrayList<TablaDatos> trainingTables = new ArrayList<>();
    protected ArrayList<TablaDatos> validationTables = new ArrayList<>();
    protected int numInputColumns = 0;
    protected int numTotalColumns = 0;
    protected double[] xLs = null;
    protected double[] xHs = null;

    protected double bestFitness;
    protected double bestFitness2;
    protected static double[] clarke={0,70,110,130,180,240};
	private double fitness2;
 	private static PlotsJFrame plots=new PlotsJFrame("TRAINING");


	private static boolean clean;

 	String tipoFitness="DelFalvero";

 	/**
 	 * Constructor
 	 * @param problem
 	 * @param trainingPath
 	 * @param validationPath
 	 * @param errorThreshold
 	 * @param viewResults
 	 * @param normalizeData
 	 * @param threadId2
 	 * @throws IOException
 	 */
    public EvaluateDataTable(AbstractProblemGE problem, String trainingPath, String validationPath,
            double errorThreshold, boolean viewResults, boolean normalizeData, long threadId2, int predictionHorizon, int historicHorizon,int timeLapse) throws IOException {
    	EvaluateDataTable.threadId=threadId2;
        this.problem = problem;
        this.trainingPath = trainingPath;
        this.validationPath = validationPath;
        this.errorThreshold = errorThreshold;
        this.viewResults = viewResults;
        logger.info("Reading data file ...");
        EvaluateDataTable.predictionHorizon=predictionHorizon;
        EvaluateDataTable.historicHorizon=historicHorizon;
        EvaluateDataTable.LapseTime=timeLapse;
        fillDataTableT();
        fillDataTableV();
        
        normalize(0,40,normalizeData);  
        logger.info("... done.");   

        bestFitness = Double.POSITIVE_INFINITY;
        bestFitness2 = Double.POSITIVE_INFINITY;
    }
    

    /**
     * Carga datos de validación
     * @throws IOException
     */
    public final void fillDataTableV() throws IOException {
      File directory = new File (validationPath);
   	  File[] files = directory.listFiles();
   	  System.out.println("Number of Problems to validate:" +files.length);
   	  String[] ProblemList= new String[files.length];
   	  int i=0;
   	  for(File file:files){
   		  ProblemList[i]=file.getName();
   		  i++;
   	  }
   	  for (int j=0; j <files.length; j++){
	   	    BufferedReader reader = new BufferedReader(new FileReader(new File(validationPath+ProblemList[j])));
	   	    TablaDatos newTable=new TablaDatos();
	        String line;
	        while ((line = reader.readLine()) != null) {
		              	if (line.isEmpty() || line.startsWith("#")) {
		              		continue;
		              	}
		               String[] parts = line.split(";");
		               if (parts.length > numInputColumns) {
		                   numInputColumns = parts.length;
		                   numTotalColumns = numInputColumns + 1;
		               }
		               double[] dataLine = new double[numTotalColumns];
		               for (int p = 0; p < numInputColumns; ++p) {
		                   dataLine[p] = Double.valueOf(parts[p]);
		               }
		               newTable.add(dataLine);        
	           }
	           reader.close();
	
		        //Calculamos analiticamente el IOB inicial en base a la insulina basal y la función de Ra   
		        modeloIOB(newTable);
			
		        
		    	//MODELO DE APARICION DE CHO
		        modeloAparicionCHO(newTable);
 	    
       	
		        validationTables.add(newTable);
		        newTable.setNameData(ProblemList[j]);
		    	
		        //Filtro de suaviazado		        
		        //filtreByMovingAverages(newTable);
		    	
		        //Da valor a los valores del Clarke en  el ZOH
		        setZeroOrderHorizon(newTable);
		      
		   	}
    }

    /**
     * Carga datos de ajuste o entrenamiento
     * @throws IOException
     */
    public final void fillDataTableT() throws IOException {
	 
      File directory = new File (trainingPath);
	  File[] files = directory.listFiles();
	  System.out.println("Number of Problems to validate:" +files.length);
	  String[] ProblemList= new String[files.length];
	  int i=0;
	  
	  for(File file:files){
		  ProblemList[i]=file.getName();
		  i++;
	  }
	  
	  for (int j=0; j <files.length; j++){
			    BufferedReader reader = new BufferedReader(new FileReader(new File(trainingPath+ProblemList[j])));
			    TablaDatos newTable=new TablaDatos();
		        String line;
		        while ((line = reader.readLine()) != null) {
			            if (line.isEmpty() || line.startsWith("#")) {
			                continue;
			            }
			            String[] parts = line.split(";");
			            if (parts.length > numInputColumns) {
			                numInputColumns = parts.length;
			                numTotalColumns = numInputColumns + 1;
			            }
			            double[] dataLine = new double[numTotalColumns];
			            for (int p = 0; p < numInputColumns; ++p) {
			                dataLine[p] = Double.valueOf(parts[p]);
			            }
			            newTable.add(dataLine);        
		        }
		        reader.close();
		
		        //Calculamos analiticamente el IOB inicial en base a la insulina basal y la función de Ra   
		        modeloIOB(newTable);
			
		    	//MODELO DE APARICION DE CHO
		        modeloAparicionCHO(newTable);
				  
		        trainingTables.add(newTable);
		        newTable.setNameData(ProblemList[j]);

		        //Filtro de suaviazado		        
		        // filtreByMovingAverages(newTable);
		    	
		        //Da valor a los valores del Clarke en  el ZOH
		        setZeroOrderHorizon(newTable);
		    	
	}
  }
    
    
    
    private void setZeroOrderHorizon(TablaDatos newTable) {
    	for(int fcero=0; fcero<newTable.size()-historicHorizon-predictionHorizon;fcero++){
        	int t0=fcero+historicHorizon;
        	int t1=fcero+predictionHorizon+historicHorizon;
        	
    		double line_eq1 = 1.4*(newTable.get(t1,0)[0] -clarke[3]);
        	double line_eq2 = newTable.get(t1,0)[0] +clarke[2];

        	double are=(Math.abs(newTable.get(t0,0)[0]-newTable.get(t1,0)[0])/newTable.get(t1,0)[0])*100;
        	
        	if (( are<=20) || ((newTable.get(t1,0)[0]<clarke[1])&&(newTable.get(t0,0)[0]<clarke[1]))) 
        		newTable.setZonaA(newTable.getZonaA()+1);
        	else if (((newTable.get(t1,0)[0]<=clarke[1] )&&(newTable.get(t0,0)[0]>=clarke[4])) || ((newTable.get(t1,0)[0]>=clarke[4])&&(newTable.get(t0,0)[0]<=clarke[1]))) 
        		newTable.setZonaE(newTable.getZonaE()+1);
        	else if (((newTable.get(t1,0)[0]<clarke[1]) && ((newTable.get(t0,0)[0]>clarke[1])&&(newTable.get(t0,0)[0]<clarke[4]))) || ((newTable.get(t1,0)[0]>clarke[5]) && ((newTable.get(t0,0)[0]>clarke[1]) && (newTable.get(t0,0)[0]<clarke[4]))))
        		newTable.setZonaD(newTable.getZonaD()+1);
        	else if ((((newTable.get(t1,0)[0]>=clarke[3])&&(newTable.get(t1,0)[0]<=clarke[4])) && (newTable.get(t0,0)[0]<line_eq1))  ||  ((newTable.get(t1,0)[0]>clarke[1]) && (newTable.get(t0,0)[0]>clarke[4]) && (newTable.get(t0,0)[0]>line_eq2))) 
        		newTable.setZonaC(newTable.getZonaC()+1);
        	else newTable.setZonaB(newTable.getZonaB()+1);;
    	}
    	int size=newTable.size()-historicHorizon-predictionHorizon;
    	newTable.setZonaA(newTable.getZonaA()/size*((double)100));
    	newTable.setZonaB(newTable.getZonaB()/size*((double)100));
    	newTable.setZonaC(newTable.getZonaC()/size*((double)100));
    	newTable.setZonaD(newTable.getZonaD()/size*((double)100));
    	newTable.setZonaE(newTable.getZonaE()/size*((double)100));	
	}


	private void filtreByMovingAverages(TablaDatos newTable) {
    	double[] aux=new double[newTable.getTT(0).size()];
    	double[] aux2=new double[newTable.getTT(0).size()];
    
    	for(int tt=0; tt< newTable.getTT(0).size(); tt++){
        	aux[tt]=newTable.get(tt,0)[0];
        }
    	movingAverage(50,aux2, aux, newTable.getTT(0).size());
    	for(int tt=0; tt< newTable.getTT(0).size(); tt++){
    		newTable.getTT(0).get(tt)[0]=aux[tt];
        }      		
	}


	private void modeloAparicionCHO(TablaDatos newTable) {
        double Ag=0.8;
      	int tmaxG=50;
      	int tmax2=tmaxG*tmaxG;
      	double numerador;
      	int contador;
      	double exponente;
    	double ra;
    	double[] arrayACHO =new double [newTable.getTT(0).size()];
    	for(int p=0; p< newTable.getTT(0).size(); p++){
          	if (newTable.get(p,0)[1]>0){
          		int idx=p;
          		contador=1;
            	ra=-1;
          		while((ra!=0)&&(idx<newTable.getTT(0).size())){
          			exponente= Math.exp(-contador/((double)tmaxG));
    		    	numerador= newTable.get(p,0)[1]*1000*Ag*contador*exponente;
    		    	ra=numerador/tmax2;
    		
    		    	if(ra<1)
    		    		ra=0;
    		    	arrayACHO[idx]=arrayACHO[idx]+ra;
    		    	
    			    contador = contador +5;	
    			    idx++;
          		}  	
          	}
		}
    	for(int p=0; p< newTable.getTT(0).size(); p++){
    		newTable.get(p,0)[1]=arrayACHO[p];
    		//System.out.println(newTable.getTT(0).get(p)[0]+"      "+newTable.getTT(0).get(p)[1]+"       "+newTable.getTT(0).get(p)[2]+"       "+newTable.getTT(0).get(p)[3]);
    	}
	}
	
	

	void modeloIOB(TablaDatos newTable){
        double Kdia=0.039;
        double C1k=newTable.get(0,0)[2]/Kdia;         
        double C2k=C1k;
        double IOB=C1k+C2k;
        double C1knew=0, C2knew=0;
    
    	for(int p=0; p< newTable.getTT(0).size(); p++){
	        // estimamos IOB y su derivada
	        C1knew=C1k+newTable.get(p,0)[2]*LapseTime-Kdia*C1k*LapseTime;
	        C2knew=C2k+Kdia*LapseTime*(C1k-C2k);
	        IOB=C1knew+C2knew;
	        //System.out.println(newTable.get(p,0)[2]+" "+IOB);
	        newTable.get(p,0)[2]=IOB;
	        //System.out.println(newTable.getTT(0).size());
	      
	        //System.out.println(newTable.getTT(0).get(p)[2]);
	        
	        //System.out.println(newTable.getTT(0).get(p)[0]+"      "+newTable.getTT(0).get(p)[1]+"       "+newTable.getTT(0).get(p)[2]+"       "+newTable.getTT(0).get(p)[3]);
      
	        C1k=C1knew;
	        C2k=C2knew;
    	}   	
    }
	
    /**
     * Computa el valor de fitness de todas las tablas (todos los archivos input)
     * @param evaluator
     * @param solution
     * @param idx
     * @param INI_TRAIN
     * @param min_PREDICTION_THRESHOLD
     * @param max_PREDICTION_THRESHOLD
     * @param validation
     * @param patient2 
     * @return
     */
     public double evaluate(AbstractPopEvaluator evaluator, Solution<Variable<Integer>> solution, int idx, String validation, int patient) {
        String functionAsString = problem.generatePhenotype(solution).toString();
        double fitness =0;
        //FOR(DIA 1 TO DIA N)
		ArrayList<double[]> addingPredictions = new ArrayList<double[]>();
		for(int idxTable=0; idxTable<evaluator.getTimeTables().size();idxTable++){
			addingPredictions.add(new double[evaluator.getTimeTables().get(idxTable).size()]);
			fitness = fitness+computeFitness(evaluator, idx,addingPredictions.get(idxTable), idxTable);
		}
		
   //	ArrayList<double[]> addingPredictionsH =new ArrayList<double[]>();
   // 	for(int i=0;i<addingPredictions.size();i++){
   //		addingPredictionsH.add(new double[evaluator.getTimeTables().get(i).size()]);
   // 		for(int j=0;j<addingPredictions.get(i).length;j++)
   // 			addingPredictionsH.get(i)[j]=addingPredictions.get(i)[j];
   // 		hullMovingAverage(16,addingPredictions.get(i), addingPredictionsH.get(i), addingPredictionsH.get(i).length);
   //	}
        
        
        if ((fitness < bestFitness)||(validation.compareTo("validation")==0)||(validation.compareTo("validation2")==0)) {
            bestFitness = fitness;
            System.out.println(tipoFitness);
            bestFitness2 = fitness2;
            for (int i = 0; i < numTotalColumns; ++i) {
                if (i == 0) {
                    functionAsString = functionAsString.replaceAll("getVariable\\(" + i + ",", "yr\\(");
        //        } else if (i == numTotalColumns - 1) {
        //            functionAsString = functionAsString.replaceAll("getVariable\\(" + i + ",", "yp\\(");
                } else {
                    functionAsString = functionAsString.replaceAll("getVariable\\(" + i + ",", "u" + i + "\\(");
                }
            }
            logger.info("Best FIT=" + bestFitness +" + "+bestFitness2+"; Expression=" + functionAsString);// + "; Java Expression: "+ origFunctionAsString);
            logger.info("\n\n" + ""+"\n\n");// + "; Java Expression: "+ origFunctionAsString);
            if ((viewResults)&&(validation.compareTo("validation")!=0)&&(validation.compareTo("validation2")!=0))  {
                   try {
					createDataPlot(evaluator,addingPredictions,"Predicted value (training)",validation,functionAsString,patient,fitness);
				} catch (IOException e) {
					e.printStackTrace();
				}   
             }
            if ((validation.compareTo("validation")==0)||(validation.compareTo("validation2")==0)) {
                try {
					createDataPlot(evaluator,addingPredictions,"Predicted value (validation)",validation,functionAsString,patient,fitness);
				} catch (IOException e) {
					e.printStackTrace();
				}   
                System.out.println(fitness+"  -   "+fitness2);
             }
        }
        return fitness;
    }
     
     
 

	/**
     * Computa el valor de fitness de una tabla (un archivo input)
     * @param evaluator
     * @param idxExpr
     * @param historicHorizon
     * @param predictionHorizon
     * @param max_PREDICTION_THRESHOLD
     * @param addingPredictions
     * @return
     */
    public double computeFitness(AbstractPopEvaluator evaluator, int idxExpr, double[] addingPredictions, int idxTable) {
            	
    	double fitness = 0;
    	//PREDECIMOS LOS VALORES POR VENTANAS DE 1 HORA
    	//Los valores predecidos hasta INI_TRAIN -> valores reales. Lo demas a 0 
  /*  	for(int test=0; test<(evaluator.getTimeTables().get(idxTable).size());test++){
    		evaluator.getTimeTables().get(idxTable).get(test,0)[evaluator.getTimeTables().get(idxTable).get(0,0).length-1]=0;
    		if(test<historicHorizon+ predictionHorizon)
			 addingPredictions[test]=evaluator.getTimeTables().get(idxTable).get(test,0)[0];
    		else
   			 addingPredictions[test]=0;
    	}
    	*/
    	evaluator.reset(idxTable);
    	//A partir del Periodo INI_TRAIN
    	for(int t=0; t < (evaluator.getTimeTables().get(idxTable).size()); t=t+SALTOS){
    		if(t<historicHorizon+ predictionHorizon)
    			addingPredictions[t]=evaluator.getTimeTables().get(idxTable).get(t,normalised)[0];
    		if((evaluator.getTimeTables().get(idxTable).get(t,normalised)[3]<lowerBound)||(evaluator.getTimeTables().get(idxTable).get(t,normalised)[3]>upperBound)){
    			addingPredictions[t]=-1;
    		}
    		else {
    			double predGlucose=evaluator.evaluateExpression(idxExpr,t,LapseTime);    
    			//MAYOR que 50000 para evitar infinitos y valores que generen problemas en el fitness
    			if(predGlucose>50000)
    				addingPredictions[t]=-4000;
    			else if(predGlucose<-50000)
    				addingPredictions[t]=4000;
    			else
    				addingPredictions[t]=predGlucose;
    		}
    	}
    	//HULL
  //  	double fitnessHull=0, fitnessClarkPark=0, fitnessErrorCuadratico=0, fitnessCorrelacion=0,fitnessMIXED=0, 
    	double fitnessDelFalvero = 0;
  //  	double[] addingPredictionsH =new double[addingPredictions.length];
 //     hullMovingAverage(32,addingPredictionsH, addingPredictions, addingPredictionsH.length);
    	
        //CALCULAMOS EL FITNESS
    	fitness2=0;
    	fitness=0;
    	int cont=0;
		for (int i = historicHorizon+predictionHorizon; i <  (evaluator.getTimeTables().get(idxTable).size()); i=i+SALTOS) {
			//PARCHE PARA EVALUAR HORAS DE NOCHE 1-7 am		
			if((addingPredictions[i]!=-1)&&(evaluator.getTimeTables().get(idxTable).get(i,0)[0])<100){
				double valor=Math.abs( addingPredictions[i]-  evaluator.getTimeTables().get(idxTable).get(i,0)[0]);
	//			fitnessDelFalvero = fitnessErrorCuadratico +valor*valor;
	//          System.out.println(penaltyDelFalvero(evaluator.getTimeTables().get(idxTable).get(i,0)[0],addingPredictions[i]));
				fitnessDelFalvero=fitnessDelFalvero+penaltyDelFalvero(evaluator.getTimeTables().get(idxTable).get(i,0)[0],addingPredictions[i])*valor*valor;
	//	  		fitnessHull =fitnessHull+ Math.abs(addingPredictionsH[i]-evaluator.getTimeTables().get(idxTable).get(i,0)[0])*Math.abs(addingPredictionsH[i]-evaluator.getTimeTables().get(idxTable).get(i,0)[0]);
	//	  		fitnessClarkPark= fitnessClarkPark + ClarkParK(evaluator.getTimeTables().get(idxTable).get(i,0)[0], addingPredictionsH[i],evaluator.getTimeTables().get(idxTable));
	//	  		fitnessMIXED=fitnessMIXED+ClarkParK(evaluator.getTimeTables().get(idxTable).get(i,0)[0], addingPredictionsH[i],evaluator.getTimeTables().get(idxTable))*valor*valor;
				cont++;
			}
         }
		
   /* 	if(tipoFitness.compareTo("MIXED")==0){
    		fitness=fitnessMIXED;
    	}
    	else if (tipoFitness.compareTo("DelFalvero")==0){*/
    		fitness=Math.sqrt(fitnessDelFalvero/cont);
    	/*}
    	else System.out.println("FITNESS NO ASIGNADO");
    		*/
    	
    	
    //	fitnessCorrelacion=computeCorelationBetweenCurves(evaluator.getTimeTable().get(idxTable).getTT(),addingPredictions,INI_TRAIN);
    //	fitness2=fitnessClarkPark;
    
     // fitness=fitness2*aux;
        return fitness;
    }
    
    
    
    public static double penaltyDelFalvero(double real, double pred){
    	double tetaH=155,tetaL=85;
    	double betaH=100,betaL=30;
    	double lambdaH=20,lambdaL=10;
    	double alphaL=1.5,alphaH=1;
  

    	double Aux1zona1=C2_sigmoid(real,tetaL,betaL,"<=");
    	double Aux2zona1=C2_sigmoid(real,pred,lambdaL,"<=");
    	double zona1=alphaL*Aux1zona1*Aux2zona1;
    	
    	double Aux1zona2=C2_sigmoid(real,tetaH,betaH,">=");
    	double Aux2zona2=C2_sigmoid(real,pred,lambdaH,">=");
    	double zona2=alphaH*Aux1zona2*Aux2zona2;
    	
    	double zona3=1;
    	
    	double aux= zona1+zona2+zona3;
    		
    	return aux;
    }
    
    public static double C2_sigmoid(double x, double a, double e, String type){
    	double xi=0;
    	if(type.compareTo(">=")==0) 
    		              xi=2/e*(x-a-(e/2));
    	else if(type.compareTo("<=")==0)
    		              xi=-2/e*(x-a+(e/2));     
    	double y=0;         
    	if(xi<=-1)
    		y=0;
    	else if (xi>=1)
    	    y=1;
        else if (xi<=0)    
            y=0.5*(-(xi*xi*xi*xi)-2*(xi*xi*xi)+2*xi+1);
        else 
        	y=0.5*((xi*xi*xi*xi)-2*(xi*xi*xi)+2*xi+1);   	
    	return y;
    }
    
    /**
     * Función auxiliar de la media movil de hull: media movil ponderada
     * @param serie
     * @param expected2
     * @param expected
     */
	public static void weightedMovingAverage(int serie, double[] expected2, double[] expected){
		for (int i=0;i<expected.length;i++){
			if (i<serie)
				expected2[i]=expected[i];
			else{
				double returnAdd=0;
				for(int m= i-serie; m <= i; m++)
					returnAdd=returnAdd+ expected[m]*((m-(i-serie))+1);
				expected2[i]= returnAdd/((serie+1)*((serie+1)+1)/2);
				}
			}
		return;
	}
	
	/**
	 * Media movil de hull. Filtro basado en medias moviles ponderadas
	 * 
	 * @param serie
	 * @param outHMA
	 * @param expected
	 * @param end
	 */
	public static void hullMovingAverage(int serie, double[] outHMA, double[] expected, int end){
		double wma1[]=new double[expected.length];
		double wma2[]=new double[expected.length];
		double MMA[]=new double[expected.length];
		weightedMovingAverage(serie,wma1,expected);
		weightedMovingAverage(serie/2,wma2,expected);			
		for(int k=0; k<end; k++){
			 MMA[k]=2*wma2[k]-wma1[k]; 
		}
		int hull=(int) Math.sqrt(serie);
		weightedMovingAverage(hull,outHMA,MMA);		
	}
    
	/**
	 * Media movil de hull. Filtro basado en medias moviles ponderadas
	 * 
	 * @param serie
	 * @param outHMA
	 * @param expected
	 * @param end
	 */
	public static void movingAverage(int serie, double[] out, double[] in, int end){
		for(int k=0; k<end; k++){
			double aux=0;
			if(k>=(end-serie)){
				out[k]=in[k];
			}
			else{
				for(int j=k; j<(k+serie); j++)
					aux=aux+in[j];
				out[k]=aux/serie;
			}
		}		
	}
	
	
    /**
     * Función de fitness para el Parke and Clarke
     * @param RFBG
     * @param SBG
     * @param tablaDatos 
     * @return
     */
    private double ClarkParK(double RFBG, double SBG, TablaDatos tablaDatos) {

        clarke[0]=clarke[0];
        clarke[1]=clarke[1]*tablaDatos.getNormaliseM();
        clarke[2]=clarke[2]*tablaDatos.getNormaliseM();
        clarke[3]=clarke[3]*tablaDatos.getNormaliseM();
        clarke[4]=clarke[4]*tablaDatos.getNormaliseM();
        clarke[5]=clarke[5]*tablaDatos.getNormaliseM();
    	
    	
    	int A=0, B=0, C=0, D=0, E=0;
    	double line_eq1 = 1.4*(RFBG -clarke[3]);
    	double line_eq2 = RFBG +clarke[2];

    	double are=(Math.abs(SBG-RFBG)/RFBG)*100;

    	if (( are<=20) || ((RFBG<clarke[1])&&(SBG<clarke[1]))) 
    		A=1;
    	else if (((RFBG <=clarke[1])&&(SBG>=clarke[4])) || ((RFBG>=clarke[4])&&(SBG<=clarke[1]))) 
    		E=1;
    	else if (((RFBG<clarke[1]) && ((SBG>clarke[1])&&(SBG<clarke[4]))) || ((RFBG>clarke[5]) && ((SBG>clarke[1]) && (SBG<clarke[4]))))
    		D=1;
    	else if ((((RFBG>=clarke[3])&&(RFBG<=clarke[4])) && (SBG<line_eq1))  ||  ((RFBG>clarke[1]) && (SBG>clarke[4]) && (SBG>line_eq2))) 
    		C=1;
    	else B=1;
    	
    	double zone=Math.abs(RFBG-SBG);
    	if (A==1)
    		zone=1;
    	else if (B==1)
    		zone=2;
    	else if (C==1) 
    		zone=3;
    	else if (D==1)
    		zone=4;
    	else if(E==1) {
    		zone=5;
    	}
    	else
    		zone=500000000;   	
		return zone;
}
    
    

    /**
     * El fitness es calculado mediante la estrategía de calculo de correlación entre dos curvas
     * @param arrayList
     * @param addingPredictions
     * @param INI_T
     * @return
     */
    public double computeCorelationBetweenCurves(ArrayList<double[]> arrayList, double[] addingPredictions, int INI_T){
    		int ini=INI_T;
    		int end=arrayList.size();
	    	// Calcula la media y la varianza de la curva real
		 	double dMediaReal      = 0;
		 	double dVarianzaReal   = 0;
		 	// Calcula la media y la varianza de la curva predecida          
		 	double dMediaPred = 0;
		 	double dVarianzaPred = 0;
		 	double dCorrelation = 0;
	
		 	for (int w = ini; w < end; w++)
			{
		 		dMediaReal = dMediaReal + arrayList.get(w)[0];
				dMediaPred = dMediaPred +  addingPredictions[w];
		 	}
		 	dMediaPred = dMediaPred / (end-ini);
		    dMediaReal = dMediaReal / (end-ini);
	
		 	if ((dMediaPred == 0)||(Double.isNaN(dMediaPred)))
		 	           dCorrelation = -Double.POSITIVE_INFINITY;
		 	else
		    {	
		 		for (int w = ini; w < end; w++)
		 		{
		 			dVarianzaReal = dVarianzaReal + (arrayList.get(w)[0] - dMediaReal)*(arrayList.get(w)[0] - dMediaReal);
		 			dVarianzaPred = dVarianzaPred + (addingPredictions[w] - dMediaPred)*(addingPredictions[w] - dMediaPred);
		 		}
	
		 		dVarianzaReal = Math.sqrt(dVarianzaReal / ((end-ini) - 1));
		 		dVarianzaPred = Math.sqrt(dVarianzaPred / ((end-ini) - 1));
	
		 	    if ((dVarianzaPred == 0)||(Double.isNaN(dVarianzaPred)))
		 	        dCorrelation = -Double.POSITIVE_INFINITY;
		 	    else
		 	    {         
		 	        // Calcula la correlación entre ambas curvas
		 	        for (int w = ini; w < end; w++)
		 	        {
		 	        	dCorrelation = dCorrelation + (arrayList.get(w)[0] - dMediaReal) * (addingPredictions[w] - dMediaPred);
		 	        }
		 	        dCorrelation = dCorrelation / ((end-ini-1) * dVarianzaPred * dVarianzaReal);
		 	     }
		 	}
		 if (Double.isNaN(dCorrelation))
	 	        dCorrelation = Double.POSITIVE_INFINITY;
		 return  5-(dCorrelation+2);   	
    }

    
    static void cuentahyposhyper(double[] real, double[] pre, int patient) throws IOException{
    	int cuentaHipos=0;
    	int cuentaHiper=0;
    	int cuentaHiper250=0;
    	int cuentaHiper180=0;
    	int cuentaHipos70=0;
    	int cuentaHipos60=0;
    	  	
    	for (int k = 0; k < real.length-1; k++) {
    			if((real[k+1]>=250)&&(real[k]<250)){
    				cuentaHiper++;
    				double are=(Math.abs(pre[k+1]-real[k+1])/real[k+1])*100;
    				if ((are<=20)||(pre[k+1]>250)) cuentaHiper250++;  
    			}
    			else if((real[k+1]>=180)&&(real[k]<180)){
    				cuentaHiper++;
    				double are=(Math.abs(pre[k+1]-real[k+1])/real[k+1])*100;
    				if((are<=20)||(pre[k+1]>180)) cuentaHiper180++;
    			}
    			else if((real[k+1]<=60)&&(real[k]>60)){
    				cuentaHipos++;
    				double are=(Math.abs(pre[k+1]-real[k+1])/real[k+1])*100;   
    				if((pre[k+1]<=60)||(are<=20))
    					cuentaHipos60++;
				}
    			else if((real[k+1]<=70)&&(real[k]>70)){
    				cuentaHipos++;
    				double are=(Math.abs(pre[k+1]-real[k+1])/real[k+1])*100;   
    				if((pre[k+1]<=70)||(are<=20))
    				if(are<=20) cuentaHipos70++;
    			}
    	}
    	FileWriter ficheroCuenta =new FileWriter("B:/Glycemias"+"("+threadId+")"+".csv",true);
    	PrintWriter fileCuenta = new PrintWriter(ficheroCuenta);
	    fileCuenta.print("Patient "+patient+";"+cuentaHipos+";"+cuentaHiper+";"+cuentaHiper180+";"+cuentaHiper250+";"+cuentaHipos60+";"+cuentaHipos70+"\n");
    	ficheroCuenta.close();
    }
    
    
    /**
     * Dibuja la gráfica de la evolucion del entrenamiento o la validación
     * 
     * @param timeTables
     * @param addingPredictions
     * @param windowTitle
     * @param functionAsString 
     * @param patient 
     * @param errorTitle
     * @throws IOException 
     */
    public static void createDataPlot(AbstractPopEvaluator evaluator, ArrayList<double[]> addingPredictions, String windowTitle,  String tipo, String functionAsString, int patient, double fitness) throws IOException {
    	ArrayList<TablaDatos> timeTables=evaluator.getTimeTables();
    	double error=0,error2=0;
    	listaVentanas2=new ArrayList<PanelGrafica>();
    	
    	FileWriter ficheroS = null;
	    FileWriter ficheroM= null;
		FileWriter ficheroR =null;
	    FileWriter ficheroD= null;
		FileWriter ficheroN =null;
		PrintWriter fileRawData =null;
		PrintWriter fileModelos =null;
	    PrintWriter fileStatistics  =null;
	
		
		if((tipo.compareTo("validation")==0)||(tipo.compareTo("validation2")==0)){
			ficheroS  = new FileWriter("/Stadistics"+predictionHorizon+"("+threadId+")"+".csv",true);
		    ficheroM = new FileWriter("/Models"+predictionHorizon+"("+threadId+")"+".csv",true);
		    ficheroR =new FileWriter("/RawData"+predictionHorizon+"("+threadId+")"+".csv",true);
		    fileRawData = new PrintWriter(ficheroR);   
		    fileModelos = new PrintWriter(ficheroM);
		    fileStatistics = new PrintWriter(ficheroS);
		}
		
	    for(int idxTable=0;idxTable<addingPredictions.size();idxTable++){
		        error=0;
		        error2=0;
		        int PADOVAPARCHESIZE=0;
		        
		        double[] real = new double[timeTables.get(idxTable).size()];
		        double[] pred = new double[timeTables.get(idxTable).size()];
		        
		        for (int k = 0; k < timeTables.get(idxTable).size(); k++) {//-min_PREDICTION_THERSHOLD
		        	if(k<predictionHorizon+historicHorizon){
		        		real[k] = timeTables.get(idxTable).get(k,0)[0];
		        		pred[k] = timeTables.get(idxTable).get(k,0)[0];	        		
		        	}else{
		        		real[k] = timeTables.get(idxTable).get(k,0)[0];
		        		pred[k] = addingPredictions.get(idxTable)[k];
		        	}
		         }
		        
		    	if(tipo.compareTo("validation")==0)
		    		cuentahyposhyper(real, pred,patient);
		    	//System.out.println(timeTables.get(idxTable).size());
		        //System.out.println(real.length);
		    	for (int k =predictionHorizon+historicHorizon; k < real.length; k=k+SALTOS) {
				    //PARCHE PARA COMPARAR MODELOS NOCTURNOS PADOVA
				    if((timeTables.get(idxTable).get(k,0)[3]>lowerBound)&&(timeTables.get(idxTable).get(k,0)[3]<upperBound)){
			    		error= error+Math.abs(real[k]-pred[k])*Math.abs(real[k]-pred[k]);
			    		error2= error2+Math.abs(timeTables.get(idxTable).get(k,0)[0]-timeTables.get(idxTable).get(k-historicHorizon,0)[0])*Math.abs(timeTables.get(idxTable).get(k,0)[0]-timeTables.get(idxTable).get(k-historicHorizon,0)[0]);
			    		PADOVAPARCHESIZE++;
				    }
		    	}
		    	error=Math.sqrt(error/PADOVAPARCHESIZE);	  
		    	error2=Math.sqrt(error2/PADOVAPARCHESIZE);
		        listaVentanas2.add(new PanelGrafica(timeTables.get(idxTable).getNameData().substring(0,timeTables.get(idxTable).getNameData().length()-4),"2","3"));
		
		        for (int k = 0; k < pred.length; k=k+SALTOS) {//-min_PREDICTION_THERSHOLD
		        	 listaVentanas2.get( listaVentanas2.size()-1).addPointPrd((k)*1000/12, pred[k]);
		        	 listaVentanas2.get( listaVentanas2.size()-1).addPointAct((k)*1000/12, real[k]);
		        }
		               
		      
		    	double fit=0;
		    	double fit2=0;
		    //    PADOVAPARCHESIZE=0;
		        int A=0, B=0, C=0, D=0, E=0;
		    	double size=(double) real.length;
		    	for(int i=predictionHorizon+historicHorizon; i<size;i=i+SALTOS){
		    	//PARCHE PARA COMPARAR MODELOS NOCTURNOS PADOVA
			    	if((timeTables.get(idxTable).get(i,0)[3]>lowerBound)&&(timeTables.get(idxTable).get(i,0)[3]<upperBound)){
			    			
			    		fit=fit+Math.abs(real[i]-pred[i])*Math.abs(real[i]-pred[i])*penaltyDelFalvero(real[i],pred[i]);
			    		fit2=fit2+Math.abs(real[i]-timeTables.get(idxTable).get(i-predictionHorizon,0)[0])*Math.abs(real[i]-timeTables.get(idxTable).get(i-predictionHorizon,0)[0])*penaltyDelFalvero(real[i],timeTables.get(idxTable).get(i-predictionHorizon,0)[0]);
			    		
			   // 		PADOVAPARCHESIZE++;
			    		double line_eq1 = 1.4*(real[i] -clarke[3]);
			        	double line_eq2 = real[i] +clarke[2];
	
			        	double are=(Math.abs(pred[i]-real[i])/real[i])*100;
	        	
			        	if (( are<=20) || ((real[i]<clarke[1])&&(pred[i]<clarke[1]))) 
			        		A++;
			        	else if (((real[i] <=clarke[1] )&&(pred[i]>=clarke[4])) || ((real[i]>=clarke[4])&&(pred[i]<=clarke[1]))) 
			        		E++;
			        	else if (((real[i]<clarke[1]) && ((pred[i]>clarke[1])&&(pred[i]<clarke[4]))) || ((real[i]>clarke[5]) && ((pred[i]>clarke[1]) && (pred[i]<clarke[4]))))
			        		D++;
			        	else if ((((real[i]>=clarke[3])&&(real[i]<=clarke[4])) && (pred[i]<line_eq1))  ||  ((real[i]>clarke[1]) && (pred[i]>clarke[4]) && (pred[i]>line_eq2))) 
			        		C++;
			        	else B++;
			    	}
		    	}
		    	DecimalFormat df = new DecimalFormat("0.00"); 
		    	double a=((double)A)/PADOVAPARCHESIZE*((double)100)*SALTOS;
		    	double b=((double)B)/PADOVAPARCHESIZE*((double)100)*SALTOS;
		    	double c=((double)C)/PADOVAPARCHESIZE*((double)100)*SALTOS;
		    	double d=((double)D)/PADOVAPARCHESIZE*((double)100)*SALTOS;
		    	double e=((double)E)/PADOVAPARCHESIZE*((double)100)*SALTOS;
		    	fit=Math.sqrt(fit/PADOVAPARCHESIZE);
		    	fit2=Math.sqrt(fit2/PADOVAPARCHESIZE);

		       	String clarke1= "GE \t zone A: "+ df.format(a) +"\t zona B: "+ df.format(b)+"\t zone C: "+ df.format(c)+"\t zone D : "+ df.format(d)+"\t zone E: "+ df.format(e)+"\t gRMSE: "+df.format(fit)+"\t RMSE: "+df.format(error);
		       	String clarke2= "\nZOH \t zone A: "+df.format(timeTables.get(idxTable).getZonaA())+"\t zona B:"+df.format(timeTables.get(idxTable).getZonaB())+"\t zone C:"+df.format(timeTables.get(idxTable).getZonaC())+"\t zone D: "+df.format(timeTables.get(idxTable).getZonaD())+"\t zone E:"+df.format(timeTables.get(idxTable).getZonaE())+"\t gRMSE: "+df.format(fit2)+"\t RMSE: "+df.format(error2);
		    	listaVentanas2.get( listaVentanas2.size()-1).addComment(clarke1+clarke2);
		    	
		    	//Penalización DelFalvero solo para guardar la variable (sin E^2)
		    	double fitFalvero=0;
		    	fit=0;
		    	fit2=0;
		    	
		    	if(tipo.compareTo("validation2")==0){	    		
		    		fileRawData.print("Pred Patient FIT"+patient+";");			        
		    
		    		for (int k = predictionHorizon; k < size; k++){
					    //PARCHE PARA COMPARAR MODELOS NOCTURNOS PADOVA
					    if((timeTables.get(idxTable).get(k,0)[3]>lowerBound)&&(timeTables.get(idxTable).get(k,0)[3]<upperBound)){
				         	fitFalvero=fitFalvero+penaltyDelFalvero(real[k],pred[k]);
				    		fit=fit+Math.abs(real[k]-pred[k])*Math.abs(real[k]-pred[k])*penaltyDelFalvero(real[k],pred[k]);
				    		fit2=fit2+Math.abs(real[k]-timeTables.get(idxTable).get(k-predictionHorizon,0)[0])*Math.abs(real[k]-timeTables.get(idxTable).get(k-predictionHorizon,0)[0])*penaltyDelFalvero(real[k],timeTables.get(idxTable).get(k-predictionHorizon,0)[0]);
				    		fileRawData.print(pred[k]+";");
				    	}
				    }
			    	fileRawData.println();			  
			    	fileRawData.print("Real Patient FIT"+patient+";");			        
			    	for (int k = predictionHorizon; k < size; k++){
						//PARCHE PARA COMPARAR MODELOS NOCTURNOS PADOVA
						if((timeTables.get(idxTable).get(k,0)[3]>lowerBound)&&(timeTables.get(idxTable).get(k,0)[3]<upperBound)){
				            fileRawData.print(real[k]+";");
						}
			    	}
			    	
			    	fileRawData.println();
			        fileStatistics.print("Patient "+patient+";"+fitFalvero/PADOVAPARCHESIZE+";"+Math.sqrt(fit/PADOVAPARCHESIZE)+";"+error+";"+a+";"+b+";"+c+";"+d+";"+e+";"+Math.sqrt(fit2/PADOVAPARCHESIZE)+";"+error2+";"+timeTables.get(idxTable).getZonaA()+";"+timeTables.get(idxTable).getZonaB()+";"+timeTables.get(idxTable).getZonaC()+";"+timeTables.get(idxTable).getZonaD()+";"+timeTables.get(idxTable).getZonaE()+";");
				}
		    	
		    	fitFalvero=0;
		    	fit=0;
		    	fit2=0;
		    	PADOVAPARCHESIZE=0;
		    	
				if(tipo.compareTo("validation")==0){
					
			        fileRawData.print("Pred Patient VAL"+patient+";");		
		    	  for (int k = predictionHorizon; k < size; k++){
						//PARCHE PARA COMPARAR MODELOS NOCTURNOS PADOVA
						if((timeTables.get(idxTable).get(k,0)[3]>=lowerBound)&&(timeTables.get(idxTable).get(k,0)[3]<upperBound)){
					    	PADOVAPARCHESIZE++;
				    		fitFalvero=fitFalvero+penaltyDelFalvero(real[k],pred[k]);
				    		fit=fit+Math.abs(real[k]-pred[k])*Math.abs(real[k]-pred[k])*penaltyDelFalvero(real[k],pred[k]);
				    		fit2=fit2+Math.abs(real[k]-timeTables.get(idxTable).get(k-predictionHorizon,0)[0])*Math.abs(real[k]-timeTables.get(idxTable).get(k-predictionHorizon,0)[0])*penaltyDelFalvero(real[k],timeTables.get(idxTable).get(k-predictionHorizon,0)[0]);
				            fileRawData.print(pred[k]+";");
			    	  	}
			    	}
			    	fileRawData.println();
			    	fileRawData.print("Real Patient VAL"+patient+";");			        
			    	for (int k = 0; k < size; k++) {
					//PARCHE PARA COMPARAR MODELOS NOCTURNOS PADOVA
					    if((timeTables.get(idxTable).get(k,0)[3]>=700)&&(timeTables.get(idxTable).get(k,0)[3]<1300)){
					    	fileRawData.print(real[k]+";");		            
					    }
			    	}
			    	fileRawData.println();
			    	fileStatistics.println(";"+fitFalvero/PADOVAPARCHESIZE+";"+Math.sqrt(fit/PADOVAPARCHESIZE)+";"+error+";"+a+";"+b+";"+c+";"+d+";"+e+";"+Math.sqrt(fit2/PADOVAPARCHESIZE)+";"+error2+";"+timeTables.get(idxTable).getZonaA()+";"+timeTables.get(idxTable).getZonaB()+";"+timeTables.get(idxTable).getZonaC()+";"+timeTables.get(idxTable).getZonaD()+";"+timeTables.get(idxTable).getZonaE()+";");     
			}		    	
    	}	
    	
    	if(tipo.compareTo("validation")==0){    
	    	fileModelos.println("Patient"+patient+";"+functionAsString+";");	    	
	    }

    	if((tipo.compareTo("validation")==0)||(tipo.compareTo("validation2")==0)){
    	 	plots=new PlotsJFrame("VALIDATION");
 	    	ficheroS.close();
	        ficheroM.close();
	    	ficheroR.close();
	        ficheroD.close();
	    	ficheroN.close();
	    	clean=false;
    	}else{
    		if(clean) plots.getFrameRoot().dispose();
    		plots=new PlotsJFrame("TRAINING");
        	clean=true;
    	}
    	plots.draw(listaVentanas2); 
    }
    
  
    public final void normalize3(double yL, double yH, boolean normalizeData) {
    	
    	if(!normalizeData) return;
      	
    	int normaliseInputColumns=3;
    	for(int idxTable=0; idxTable<trainingTables.size();idxTable++){
		        logger.info("Normalizing data in [" + yL + ", " + yH + "] ...");
		        xLs = new double[normaliseInputColumns];
		        xHs = new double[normaliseInputColumns];
		        for (int i = 0; i < normaliseInputColumns; i++) {
		            xLs[i] = Double.POSITIVE_INFINITY;
		            xHs[i] = Double.NEGATIVE_INFINITY;
		        }
		        // We compute first minimum and maximum values:
		        for (int i = 0; i < trainingTables.get(idxTable).size(); i++) {
		            for (int j = 0; j < normaliseInputColumns; j++) {
		                if (xLs[j] > trainingTables.get(idxTable).get(i,0)[j]) {
		                    xLs[j] = trainingTables.get(idxTable).get(i,0)[j];
		                }
		                if (xHs[j] < trainingTables.get(idxTable).get(i,0)[j]) {
		                    xHs[j] = trainingTables.get(idxTable).get(i,0)[j];
		                }
		            }
		        }
		
		        // Now we compute "m" and "n", being y = m*x + n
		        // y is the new data
		        // x is the old data
		        double[] m = new double[normaliseInputColumns];
		        double[] n = new double[normaliseInputColumns];
		        for (int j = 0; j < normaliseInputColumns; j++) {
		            m[j] = (yH - yL) / (xHs[j] - xLs[j]);
		            n[j] = yL - m[j] * xLs[j];
		        }
		        trainingTables.get(idxTable).setN(n[0]);
		        trainingTables.get(idxTable).setM(m[0]);
		        // Finally, we normalize ...
		        for (int i = 0; i <  trainingTables.get(idxTable).size(); i++) {
		            for (int j = 0; j < normaliseInputColumns; j++) {
		            	trainingTables.get(idxTable).get(i,1)[j] = m[j] * trainingTables.get(idxTable).get(i,0)[j] + n[j];
		            }
		        }		        
    	}
    	
    	
    	for(int idxTable=0; idxTable<validationTables.size();idxTable++){
	        logger.info("Normalizing data in [" + yL + ", " + yH + "] ...");
	        xLs = new double[normaliseInputColumns];
	        xHs = new double[normaliseInputColumns];
	        for (int i = 0; i < normaliseInputColumns; i++) {
	            xLs[i] = Double.POSITIVE_INFINITY;
	            xHs[i] = Double.NEGATIVE_INFINITY;
	        }
	        // We compute first minimum and maximum values:
	        for (int i = 0; i < validationTables.get(idxTable).size(); ++i) {
	            for (int j = 0; j < normaliseInputColumns; j++) {
	                if (xLs[j] > validationTables.get(idxTable).get(i,0)[j]) {
	                    xLs[j] = validationTables.get(idxTable).get(i,0)[j];
	                }
	                if (xHs[j] < validationTables.get(idxTable).get(i,0)[j]) {
	                    xHs[j] = validationTables.get(idxTable).get(i,0)[j];
	                }
	            }
	        }
	
	        // Now we compute "m" and "n", being y = m*x + n
	        // y is the new data
	        // x is the old data
	        double[] m = new double[normaliseInputColumns];
	        double[] n = new double[normaliseInputColumns];
	        for (int j = 0; j < normaliseInputColumns; j++) {
	            m[j] = (yH - yL) / (xHs[j] - xLs[j]);
	            n[j] = yL - m[j] * xLs[j];
	        }
	        
	       validationTables.get(idxTable).setN(n[0]);
	        validationTables.get(idxTable).setM(m[0]);
	        // Finally, we normalize ...
	        for (int i = 0; i <  validationTables.get(idxTable).size(); ++i) {	           
	            for (int j = 0; j < normaliseInputColumns; j++) {
	            	validationTables.get(idxTable).get(i,1)[j] = m[j] * validationTables.get(idxTable).get(i,0)[j] + n[j];
	            }
	        }
	}
    
    }

    
  public final void normalize(double yL, double yH, boolean normalizeData) {
    	
		for(int idxTable=0; idxTable<trainingTables.size();idxTable++){
			trainingTables.get(idxTable).clone(trainingTables.get(idxTable).getTT(0));
			
		}
		for(int idxTable=0; idxTable<validationTables.size();idxTable++){
			validationTables.get(idxTable).clone(validationTables.get(idxTable).getTT(0));
			
		}
		
    	if(!normalizeData) return;
      
    	
    	
    	int normaliseInputColumns=3;
    	for(int idxTable=0; idxTable<trainingTables.size();idxTable++){
		        logger.info("Normalizing data in [" + yL + ", " + yH + "] ...");
		        xHs = new double[normaliseInputColumns];
		        for (int i = 0; i < normaliseInputColumns; i++) {
		        	xHs[i] = Double.NEGATIVE_INFINITY;
		        }
		        // We compute first minimum and maximum values:
		        for (int i = 0; i < trainingTables.get(idxTable).size(); i++) {
		            for (int j = 0; j < normaliseInputColumns; j++) {
		                if (xHs[j] < trainingTables.get(idxTable).get(i,0)[j]) {
		                    xHs[j] = trainingTables.get(idxTable).get(i,0)[j];
		                }
		            }
		        }
		      //  trainingTables.get(idxTable).setM(yH/xHs[0]);
		        // Finally, we normalize ...
		        for (int i = 0; i <  trainingTables.get(idxTable).size(); i++) {
		            for (int j = 0; j < normaliseInputColumns; j++) {
		            	trainingTables.get(idxTable).get(i,1)[j] = trainingTables.get(idxTable).get(i,0)[j]*yH/xHs[j];
		            }
		        }		        
    	}
    	
    	
    	for(int idxTable=0; idxTable<validationTables.size();idxTable++){
	        logger.info("Normalizing data in [" + yL + ", " + yH + "] ...");
	        xHs = new double[normaliseInputColumns];
	        for (int i = 0; i < normaliseInputColumns; i++) {
	            xHs[i] = Double.NEGATIVE_INFINITY;
	        }
	        // We compute first minimum and maximum values:
	        for (int i = 0; i < validationTables.get(idxTable).size(); ++i) {
	            for (int j = 0; j < normaliseInputColumns; j++) {
	                if (xHs[j] < validationTables.get(idxTable).get(i,0)[j]) {
	                    xHs[j] = validationTables.get(idxTable).get(i,0)[j];
	                }
	            }
	        }
	     //   validationTables.get(idxTable).setM(yH/xHs[0]);
	        // Finally, we normalize ...
	        for (int i = 0; i <  validationTables.get(idxTable).size(); ++i) {	           
	            for (int j = 0; j < normaliseInputColumns; j++) {
	            	validationTables.get(idxTable).get(i,1)[j] = validationTables.get(idxTable).get(i,0)[j]*yH/xHs[j];
	            }
	        }
	}
    
    }
    public ArrayList<TablaDatos> getTrainingTable() {
        return trainingTables;
    }

    public ArrayList<TablaDatos> getValidationTables() {
        return validationTables;
    }

	public double getFitness2() {
		return fitness2;
	}

}
