package ge;

import java.util.ArrayList;

import data.TablaDatos;


/**
 *
 * @author Iván Contreras
 */
public abstract class AbstractPopEvaluator {

	protected int timeLapse;
	protected int historicHorizon;
    protected int predictionHorizon;
    protected int Max_PREDICTION_THRESHOLD;
    protected ArrayList<TablaDatos> timeTables = new ArrayList<>();
    protected int horaMenor;
    protected int horaMayor;
	protected Integer idxTable;
	private String expresionM;
    
    public abstract double evaluate(int idxExpr, int k);
    
    
    public AbstractPopEvaluator(){
    	
    }
    
    
	public double evaluateExpression(int idxExpr, int WINDOW, int timeLapse) {
		historicHorizon=WINDOW;
		this.timeLapse=timeLapse;

		Double resultAux=Double.POSITIVE_INFINITY;
		try{
				resultAux=evaluate(idxExpr,WINDOW);
		}
		catch (Exception ee) {
						System.err.println(ee.getLocalizedMessage());
						resultAux = Double.POSITIVE_INFINITY;
		}
		if(Double.isNaN(resultAux)) resultAux = Double.POSITIVE_INFINITY;
		
		return resultAux;
	}

	public double getTableValues(int time, int idxVar){
			//LOS ENTREGA NORMALIZADOS
			return timeTables.get(idxTable).get(time,0)[idxVar];
	}
	
	public double getTimeLapses3(Double var){
		
		String x=String.valueOf(var.intValue());
		Double a;
		if(x.length()<=2){
			a= var;
		}else if(x.length()==3){
			a=Double.valueOf((x.substring(1,3)))+60*Double.valueOf((x.substring(0,1)));
		}else{
			a=Double.valueOf((x.substring(2,4)))+60*Double.valueOf((x.substring(0,2)));
		}
	
		//System.out.println(a);
		return a;
	}

	
	public double getTimeLapses(Double var){
		
		String x=String.valueOf(var.intValue());
		Double a;
		if(x.length()<=2){
			a= var;
		}else if(x.length()==3){
			a=Double.valueOf((x.substring(1,3)))+60*Double.valueOf((x.substring(0,1)));
		}else{
			a=Double.valueOf((x.substring(2,4)))+60*Double.valueOf((x.substring(0,2)));
		}
	
		//System.out.println(a);
		return a/timeLapse;
	}


    
	public void setLimits(int historicHorizon, int predictionHorizon) {
	    this.historicHorizon = historicHorizon;
	    this.predictionHorizon = predictionHorizon;
	}
	
	public boolean modeloDia(int horaActual, int horamenor, int horamayor){
		this.horaMayor=(int) getTimeLapses((double)horamayor);
		this.horaMenor=(int) getTimeLapses((double)horamenor);
		//if(horaActual==36) expresionM=expresionM+"MODELO DIA("+horaMenor+"-"+horaMenor+")\n";
		//PARCHE PARA COMPARAR MODELOS NOCTURNOS PADOVA (Eliminar primer if y quitar los comentarios del siguiente)
		if ((getTableValues(horaActual,3)>=horamenor)&&(getTableValues(horaActual,3)<horamayor)){
		//if ((getTableValues(horaActual,3)<horamenor)&&(getTableValues(horaActual,3)>horamayor)){
			return true;
		}
		else
			return false;
	}
	
	public double calculateHistoryG(int var, int currentTime, int tiempo, String operadorComplejo, String operadorSimple, double cte, double exp){
		double result=operateS(operadorSimple,operateC(operadorComplejo,Math.pow(getTableValues(currentTime-tiempo,var),exp)),cte);
//		expresionM="ExprGL(t)="+operadorComplejo+"(GL(t-12)^("+exp+"))"+operadorSimple+cte+"\n";
		return result; 
	}
	
	public double calculateHistoryC(int var, int currentTime, int tiempo, String operadorComplejo, String operadorSimple, double cte, double exp){
		double result=operateS(operadorSimple,operateC(operadorComplejo,Math.pow(getTableValues(currentTime-tiempo,var),exp)),cte);
//		expresionM=expresionM+"ExprC(t)=SUMATORIO[i=t-"+ini+";i=t-"+end+"]("+operadorComplejo+"(C(t-i)^("+exp+"))"+operadorSimple+cte+")\n";
		return result; 
	}


	public double calculateHistoryU(int var, int currentTime, int tiempo, String operadorComplejo,  String operadorSimple, double cte, double exp){
		double result=operateS(operadorSimple,operateC(operadorComplejo,Math.pow(getTableValues(currentTime-tiempo,var),exp)),cte);
//		expresionM=expresionM+"ExprIN(t)="+operadorComplejo+"(IN(t-"+tiempo+")^("+exp+"))"+operadorSimple+cte+"\n";
//		expresionM=expresionM+"Expr(t)=ExprGL(t)-ExprIN(t)+ExprC(t)";
		return result; 
	}
	
	public double operateS(String operadorS, double operando1, double operando2){
		switch (operadorS){
			case "+": return operando1+operando2; 
			case "-": return operando1-operando2; 
			case "*": return operando1*operando2; 
			case "/": return operando1/operando2; 
			case "": return 0; 
		default : return 0;
		}
	}
		
	public double operateC(String operador, double operando){
//		System.out.println(operador);
		switch (operador){
			case "INV": return 1/operando; 
			case "LOG": return Math.log(operando); 
			case "SQR": return Math.sqrt(operando);
			//case "PO2": return Math.pow(operando,2); 
			//case "PO3": return Math.pow(operando,3); 
			case "SIN": return Math.sin(operando); 
			case "EXP": return Math.exp(operando);
			case "EMP": return operando; 
		default : return operateC(operador.substring(0,3),operateC(operador.substring(3),operando));
		}
	}
	
	public double timeExpr(int currentTime, double cte1, double cte2,double cte3){
		double hourLapse=getTimeLapses(getTableValues(currentTime,3));
		return Math.sin((hourLapse/(1440))*Math.PI*cte2)*(cte3);
	}

	public void setTimeTables(ArrayList<TablaDatos> trainingTables) {
		timeTables=(ArrayList<TablaDatos>) trainingTables.clone();
		
	}
	
	public ArrayList<TablaDatos> getTimeTables() {
		return timeTables;		
	}

	public String getExpresionM() {
		return expresionM;
	}


	public int getDayEnd() {
		return horaMenor;
	}


	public int getDayStart() {
		return horaMayor;
	}


	public void reset(int idxTable) {
		this.idxTable=idxTable;
		
		
	}
}
