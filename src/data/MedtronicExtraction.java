package data;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class MedtronicExtraction {
     
	  ArrayList<String> IN_DAY;
	  ArrayList<String> IN_TIME;
	  ArrayList<String> IN_BASAL_RATE;
      ArrayList<String> IN_BOLUS_VOL;
      ArrayList<String> IN_BOLUS_TYPE;
      ArrayList<String> IN_BOLUS_TIME;
      ArrayList<String> IN_CARBOHIDRATOS;
     
      ArrayList<String> GL_DAY;
	  ArrayList<String> GL_TIME;
      ArrayList<String> GL_GLUCOSE;
     
      DataDay FINAL_DATA;

      String name ="";


	public MedtronicExtraction(String string) {
		name=string;
	}




	private  void readMedtronicCSV() throws FileNotFoundException, IOException{
	        
		    System.out.println(name);
		
	        FileReader fr = new FileReader("SERIES/"+name);
	        BufferedReader br = new BufferedReader(fr);
	        String strLine;
	        strLine= br.readLine();
	        boolean startExtraction=false;
	     
	        
	        //Buscamos la linea donde empiezan las lecturas
	        while ((strLine  != null)&&(!startExtraction)){
	        	strLine=br.readLine();
	            StringTokenizer str = new StringTokenizer(strLine,";");
	            if(str.nextToken().compareTo("Index")==0){
	            	startExtraction=true;      	
	            }
	        }
	        
	        //Comenzamos almacenando los datos de IN y CH en Java
	        IN_DAY= new ArrayList<String>();
	        IN_TIME= new ArrayList<String>();
	        IN_BASAL_RATE= new ArrayList<String>();
	        IN_BOLUS_VOL= new ArrayList<String>();;
	        IN_BOLUS_TYPE= new ArrayList<String>();;
	        IN_BOLUS_TIME= new ArrayList<String>();
	        IN_CARBOHIDRATOS= new ArrayList<String>();
	        int t=0;
	        boolean in_ended=false;
	   	    strLine= br.readLine();
	        while ((strLine  != null) && (!in_ended)){	
	         	
	        	    if(strLine.compareTo("")==0)
           				in_ended=true;
		        	
           			StringTokenizer str = new StringTokenizer(strLine,";",true);
		            int indexJ=0;	
		  
		        	String vacio="";
		            while(str.hasMoreElements()&&(!in_ended)){
		           		String aux=str.nextToken();
		           	
		           		if((aux.compareTo(";")==0)&&(vacio.compareTo(";")==0)){
		           			aux="#";
		           		}
		           		else vacio=aux;
		           		       		
		           		
		           		if(aux.compareTo(";")!=0){
	          		        if(indexJ==1){
			            		IN_DAY.add(aux);
			            	}
			            	else if(indexJ==2){
					            IN_TIME.add(aux);
			            	}
			            	else if(indexJ==6){
			            		if(aux.compareTo("RATE UNKNOWN")==0)
			            			IN_BASAL_RATE.add("#");		
			            		else
			            			IN_BASAL_RATE.add(aux);
			            	}
			            	else if(indexJ==12){
			            		IN_BOLUS_VOL.add(aux);
			            	}
			            	else if(indexJ==10){
			            		IN_BOLUS_TYPE.add(aux);
			            	}
			            	else if(indexJ==13){
			            		IN_BOLUS_TIME.add(aux);
			            	}
			            	else if(indexJ==24){        
			            		IN_CARBOHIDRATOS.add(aux);
			            	}        
			                indexJ++;
		           		}
		           	}	   
		            strLine= br.readLine();
	        }
	        
	        startExtraction=false;	        
	        //buscamos el comienzo de la serie de glucosas
	        while (!startExtraction){
	            StringTokenizer str = new StringTokenizer(strLine,";");
	            if(str.nextToken().compareTo("Index")==0){
	            	startExtraction=true;     
	            	break;
	            }
	        	strLine=br.readLine();
	        }
	        	        
	        //Comenzamos a leer glucosas
	        GL_DAY= new ArrayList<String>();
	        GL_TIME= new ArrayList<String>();
	        GL_GLUCOSE= new ArrayList<String>();

    	    strLine= br.readLine();
	        while ((strLine  != null)&&(strLine.compareTo("")!=0)){	

           			StringTokenizer str = new StringTokenizer(strLine,";",true);
		           
           			int indexJ=0;
           			String vacio="";
           			
		            while(str.hasMoreElements()){		            	
		           		String aux=str.nextToken();
		           		
		           		if((aux.compareTo(";")==0) && (vacio.compareTo(";")==0)){
		           			aux="#";
		           		}
		           		else vacio=aux;           		       				           		
		           		if(aux.compareTo(";")!=0){
		           	        if(indexJ==1){
			            		GL_DAY.add(aux);
			            	}
			            	else if(indexJ==2){
					            GL_TIME.add(aux);
			            	}
			            	else if(indexJ==30){        
			            		GL_GLUCOSE.add(aux);
			            	}        
			                indexJ++;
		           		}
		            }
	
		    	    if(GL_GLUCOSE.size()!=GL_DAY.size()){
	            		GL_GLUCOSE.add("#");
		    	    }
		    	    strLine= br.readLine();
	        }	   

	        br.close();
	        fr.close();
	    }
	 
	


    /*
     * Una vez almacenados en Java los datos de la bomba extraemos los datos
     */
	private  void extractData(){
		
		int contadorDias=-1;
		FINAL_DATA=new DataDay();
		for(int i=IN_DAY.size()-1;i>=0;i--){
			if(i==IN_DAY.size()-1){
				contadorDias++;
				FINAL_DATA.newDAY(IN_DAY.get(i));
			}else if(IN_DAY.get(i).compareTo(IN_DAY.get(i+1))!=0){
				FINAL_DATA.newDAY(IN_DAY.get(i));
				contadorDias++;
			}
			Integer range=contadorDias*288+rangesLocation(IN_TIME,i);
		//	System.out.println(range);
			Double basalRate=getDouble(IN_BASAL_RATE,i);
			Double bolusVol=getDouble(IN_BOLUS_VOL,i);
			Integer bolusTimeRanges=getTime(IN_BOLUS_TIME,i);
			Double carbohidratos=getDouble(IN_CARBOHIDRATOS,i);
			
			FINAL_DATA.eventoIN(range,basalRate,bolusVol,IN_BOLUS_TYPE.get(i),bolusTimeRanges,carbohidratos);		
		}
		FINAL_DATA.completa();

		contadorDias=1;			try{
		for(int i=GL_DAY.size()-1;i>=0;i--){
			Integer range=rangesLocation2(GL_TIME,i);
		    FINAL_DATA.eventoGL(GL_DAY.get(i),range,GL_GLUCOSE.get(i));
			}
		}catch(Exception E){
			System.out.println(E.getStackTrace().toString());
	}
		
		
	//	System.out.println(FINAL_DATA.info());
		
	}
	
	/*
	 * Rangos afectados por el tiempo de square
	 * */
	private  Integer getTime(ArrayList<String> time, int i) {
		if(time.get(i).compareTo("#")==0)
			return -1;
		if(time.get(i).substring(1, 2).compareTo(":")==0){
			int horasToMin= Integer.valueOf(time.get(i).substring(0,1))*60;
			int minutos=Integer.valueOf(time.get(i).substring(2,4));
			return (horasToMin+minutos)/5;
		}
		else{
			int horasToMin= Integer.valueOf(time.get(i).substring(0,2))*60;
			int minutos=Integer.valueOf(time.get(i).substring(3,5));			
			return (horasToMin+minutos)/5;
		}
		

	}

	private  Integer rangesLocation2(ArrayList<String> list, int indRange){
		
		//To Integer
		int range= timeString(list,indRange);
		
		//Linked range
		for(int i=0;i<24;i++)
			for(int j=5;j<=60;j=j+5)
				if(range<=(i*100+j))
					return (i*100+j);	
		return-1;
	}


	private  Double getDouble(ArrayList<String> list, int i) {
		if(list.get(i).compareTo("#")==0)
			return -1.0;
		return Double.valueOf(list.get(i).replaceAll(",", "."));
	}

	private  Integer rangesLocation(ArrayList<String> list, int indRange){
		
		//To Integer
		int range= timeString(list,indRange);
		
		//Linked range
		for(int i=0;i<24;i++)
			for(int j=5;j<=60;j=j+5)
				if(range<=(i*100+j))
					return (i*60/5+(j-5)/5);	
		return-1;
	}
	
	private  Integer timeString(ArrayList<String> list, int  i){
		if(list.get(i).substring(1, 2).compareTo(":")==0)
			return Integer.parseInt(list.get(i).substring(0,1).concat(list.get(i).substring(2,4)));
		else{
			try{
			return Integer.parseInt(list.get(i).substring(0,2).concat(list.get(i).substring(3,5)));
			}
			
			catch(Exception e){
				System.out.println(e);
				
			}
			}
		return i;
	}


	public  void mainExtractFile () {
		
		    try {
			      readMedtronicCSV();	
			      extractData();
			      newFile();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
     }




	private  void newFile() throws IOException {
		FINAL_DATA.newFile(name);
		
	}


	
}