package data;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.ArrayList;

public class DataDay {
	
	private ArrayList<Double> insulina_basal_SIN;
	private ArrayList<Double> insulina_bolus_SIN;
	private ArrayList<Double> glucosa_SIN;
	private ArrayList<Double> carbohidratos_SIN;
	private ArrayList<Integer> hora_SIN;
	private ArrayList<String> day_SIN;


	DataDay(){

		day_SIN=new ArrayList<String>();
		insulina_basal_SIN =new  ArrayList<Double>();
		insulina_bolus_SIN =new  ArrayList<Double>();
		glucosa_SIN =new ArrayList<Double>();
		carbohidratos_SIN =new ArrayList<Double>();
		hora_SIN =new ArrayList<Integer>();

	}

	public Double format(Double x){
		//Obtenemos una instancia de la clase 
		NumberFormat nf = NumberFormat.getInstance(); 
		//Establecemos el numero de decimales 
		nf.setMaximumFractionDigits(4); 
		//Convertimos el numero 
		String st=nf.format(x); 
		//lo vuelves a convertir a double 
		return new Double(Double.valueOf(st.replaceAll(",",".")));
	}
	
	public void eventoIN(Integer range, Double basalRate, Double bolusVol,
			String tipoBolus, Integer bolusTime, Double carbohidrato) {	
		
		if(basalRate.compareTo(-1.0)!=0){
			insulina_basal_SIN.set(range, format(basalRate/60*5));
		}
		if(bolusVol.compareTo(-1.0)!=0){
			if((tipoBolus.compareTo("Normal")==0)||(tipoBolus.compareTo("Dual (normal part)")==0)){
				insulina_bolus_SIN.set(range, bolusVol);
			}
			if((tipoBolus.compareTo("Square")==0)||(tipoBolus.compareTo("Dual (square part)")==0)){
				
				for(int s=0;s<bolusTime;s++){
					if(range+s<insulina_bolus_SIN.size())
						insulina_bolus_SIN.set(range+s, format(bolusVol/bolusTime)+insulina_bolus_SIN.get(range+s));
				}
			}
		}
		if(carbohidrato.compareTo(-1.0)!=0){
			carbohidratos_SIN.set(range, carbohidratos_SIN.get(range)+carbohidrato);
		}		
	}

	public void eventoGL(String day, Integer range, String glucose) {
		for( int i=0; i<day_SIN.size(); i++){
			if(day_SIN.get(i).compareTo(day)==0){
				for( int j=i; j<hora_SIN.size(); j++){
					if(hora_SIN.get(j).compareTo(range)==0){
						if(glucose.compareTo("#")==0) glucosa_SIN.set(j,-1.0); 					
						else glucosa_SIN.set(j,Double.valueOf(glucose.replace(",", ".")));
						break;
					}
				}
			break;
			}
		}
		
	}
	/*
	 * Por cada dia creamos sus respectivos rangos de 5 minutos (288 rangos=24*60/5)
	 */
	public void newDAY(String dia) {
			for(int i=0;i<24;i++){
				for(int j=5;j<=60;j=j+5){
					day_SIN.add(new String(dia));
					hora_SIN.add(new Integer(i*100+j));
					insulina_bolus_SIN.add(new Double(0.0));
					insulina_basal_SIN.add(new Double(-1.0));
					glucosa_SIN.add(new Double(-1.0));
					carbohidratos_SIN.add(new Double(0.0));
				}
			}
	}

	/*
	 * Imprimir info de debug
	 */
	public String info() {
		String s="";
		for(int i=0;i<day_SIN.size();i++){
		  s=s+"\n "+day_SIN.get(i)+";"+hora_SIN.get(i)+"; C"+carbohidratos_SIN.get(i)+"; BO"+insulina_bolus_SIN.get(i)+"; BA"+insulina_basal_SIN.get(i)+";"+"; GL"+glucosa_SIN.get(i)+";";
		}
		return s;
	}
	
    /*
     * completamos la basal para todos los rangos de tiempo
     */
	public void completa(){
		for(int i=1;i<insulina_basal_SIN.size();i++){
			if((insulina_basal_SIN.get(i).compareTo(-1.0)==-0)&&(insulina_basal_SIN.get(i-1).compareTo(-1.0)!=-1)){
				insulina_basal_SIN.set(i,insulina_basal_SIN.get(i-1));
			}
		}
	}
	
	
	/*
	 * creacion de archivo final
	 */
	public void newFile(String name) throws IOException {
        FileWriter fich = null;
		PrintWriter pSo = null;   
        fich = new FileWriter("PacientesResultados/"+"Post_"+name);
        pSo = new PrintWriter(fich);
		pSo.println("GLUCOSA(ISIG);INSULINE;CH(INSULINE*EXCHANGE OF CH);DAY;TIME(HHMM);");   	
        for(int i=0; i< day_SIN.size(); i++){
        	    Double totalIN= new Double (insulina_bolus_SIN.get(i)+insulina_basal_SIN.get(i));
        	//    if(totalIN>100)
        	//    	System.out.println("EROR");
        		pSo.println(glucosa_SIN.get(i)+";"+totalIN+";"+carbohidratos_SIN.get(i)+";"+day_SIN.get(i)+";"+hora_SIN.get(i)+";");   		
        } 
    	pSo.close();	
	}
	
		
}
