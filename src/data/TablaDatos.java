package data;

import java.util.ArrayList;

/**
 * 
 * @author micelab1
 *
 */
public class TablaDatos {
	
	private String nameData;
	
	protected ArrayList<double[]> trainingTable = new ArrayList<>();
	protected ArrayList<double[]> trainingTableNormalised = new ArrayList<>();

	private double normaliseM=1;
	private double normaliseN=0;
	private double zonaA=0;
	private double zonaB=0;
	private double zonaC=0;
	private double zonaD=0;
	private double zonaE=0;

	public ArrayList<double[]> getTT(int normalise) {
		if(normalise==0)
			return trainingTable;
		else
			return trainingTableNormalised;
	}

	public double[] get(int index,int normalise) {
		if(normalise==0)
			return trainingTable.get(index);
		else
			return trainingTableNormalised.get(index);
	}

	public void add(double[] dataLine) {
		trainingTable.add(dataLine);		
	}

	public int size(){
		return trainingTable.size();
	}

	public String getNameData() {
		return nameData;
	}

	public void setNameData(String nameData) {
		this.nameData = nameData;
	}

	public void setM(double d) {
		normaliseM=d;
	}
	
	public void setN(double d) {
		normaliseN=d;
	}

	public double getNormaliseM() {
		return normaliseM;
	}

	public double getNormaliseN() {
		return normaliseN;
	}

	public double getZonaA() {
		return zonaA;
	}

	public void setZonaA(double zonaA) {
		this.zonaA = zonaA;
	}

	public double getZonaB() {
		return zonaB;
	}

	public void setZonaB(double zonaB) {
		this.zonaB = zonaB;
	}

	public double getZonaC() {
		return zonaC;
	}

	public void setZonaC(double zonaC) {
		this.zonaC = zonaC;
	}

	public double getZonaE() {
		return zonaE;
	}

	public void setZonaE(double zonaE) {
		this.zonaE = zonaE;
	}

	public double getZonaD() {
		return zonaD;
	}

	public void setZonaD(double zonaD) {
		this.zonaD = zonaD;
	}

	public void clone(ArrayList<double[]> clone) {
		for(int i=0; i<trainingTable.size();i++){
			    trainingTableNormalised.add(new double[4]);
				for(int j=0; j<4;j++)
					trainingTableNormalised.get(trainingTableNormalised.size()-1)[j]=trainingTable.get(i)[j];			
		}	
	}
}
