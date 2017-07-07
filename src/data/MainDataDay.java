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
import java.util.StringTokenizer;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class MainDataDay {

	 private static final int MIN = 280;
     static ArrayList<String> TIME;
	 static ArrayList<String> GLUCOSA;
     static ArrayList<String> CONT;
     static ArrayList<String> DIA;
     static ArrayList<String> CARBOHIDRATOS;
     static ArrayList<String> GLU;
     static ArrayList<String> INSULINA;




	private static void readDias(String problemList) throws FileNotFoundException, IOException{
	   //     String filePath = "pacientesCADENAS";
	        System.out.println(problemList);

		
	        FileReader fr = new FileReader("PacientesResultados/"+problemList);
	        BufferedReader br = new BufferedReader(fr);
	        String strLine;
	        int indexI=0;
	        String dateOld="";
	        TIME= new ArrayList<String>();
	        GLUCOSA= new ArrayList<String>();
	        CARBOHIDRATOS= new ArrayList<String>();
	        INSULINA= new ArrayList<String>();	      
	        DIA= new ArrayList<String>();
        	
	        strLine= br.readLine();
	        while (strLine  != null){
	        	
	        	strLine=br.readLine();

	            if(strLine!=null){

		        	StringTokenizer str = new StringTokenizer(strLine,";");
		            int indexJ=0;
		        	
		        	while(str.hasMoreElements()){
		           		String aux=str.nextToken();
		           		
		            	if(indexJ==3){
		            		if(indexI==0) dateOld=aux;
		    		        if(dateOld.compareTo(aux)!=0){
		    		        	INSULINA.remove(INSULINA.size()-1);
		    		        	GLUCOSA.remove(GLUCOSA.size()-1);
		    		        	CARBOHIDRATOS.remove(CARBOHIDRATOS.size()-1);
		    		        	if(cuenta(GLUCOSA)&&cuenta(INSULINA))
		    		        		CreaDiaPaciente(problemList,dateOld);
		    				    INSULINA.clear();
		    				    CARBOHIDRATOS.clear();
		    				    GLUCOSA.clear();
		    				    TIME.clear();
		            			dateOld=aux;
		            			str = new StringTokenizer(strLine,";");
		    		            indexJ=-1;
		            		}
		    		        
		            	}

		            	else if(indexJ==0)
		            		GLUCOSA.add(aux);
		            	else if(indexJ==2)        
		            		CARBOHIDRATOS.add(aux);        
		            	else if(indexJ==1)
		            		INSULINA.add(aux.replace(",", "."));
			            else if(indexJ==4)
			            		TIME.add(aux);	            		            	
		                indexJ++;              
		        	}
	                if(TIME.size()!=INSULINA.size())
	                	System.out.println("ERROR");
	            }
		        indexI++;
	        }
        	if(cuenta(GLUCOSA)&&cuenta(INSULINA))
        		CreaDiaPaciente(problemList,dateOld);
	        br.close();
	        fr.close();
	    }
	 
	
	
    private static boolean cuenta(ArrayList<String> arr) {
    	int cont=0;
		for(int i=0; i<arr.size();i++){
			if(arr.get(i).compareTo("-1.0")!=0){
				cont++;
			}
		}
		if(cont>287) return true;
		else return false;
	}



	private static void CreaDiaPaciente(String problemList,String Date) throws IOException {
        FileWriter fich = null;
		PrintWriter pSo = null;   
        File directorio = new File("PacientesDias/"+problemList.replace(".csv", ""));
        directorio.mkdir();
        //	fich = new FileWriter("PacientesResultadosCAD/"+"P"+problemList.substring(1,2)+"_D"+DIA.get(0)+"_D"+df.format(std.getStandardDeviation())+"_M"+df.format(std.getMean())+"_C"+auxS+".csv");
        fich = new FileWriter("PacientesDias/"+problemList.replace(".csv", "")+"/"+
        "D"+Date.replaceAll("/", "-" )+
        ".csv");
        pSo = new PrintWriter(fich);
        for(int i=0; i<TIME.size();i++){
        		pSo.println(GLUCOSA.get(i)+";"+CARBOHIDRATOS.get(i)+";"+INSULINA.get(i)+";"+TIME.get(i)+";");      		
    	   }   
    	pSo.close();	
	}



    public static void main (String[] args) {
	    String DirSource = "PacientesResultados";
	     // ProblemsList= aList;
	  File directory = new File (DirSource);
	  File[] files = directory.listFiles();
	  int size=files.length;
	  System.out.println("Number of Problems:" +size);
	  String[] ProblemList= new String[size];
	  int i=0;
	  for(File file:files){
	      ProblemList[i]=file.getName();
	      i++;
	  }
	  
	  for (int j=0; j <size; j++){
		  try {
			readDias(ProblemList[j]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }
}
}