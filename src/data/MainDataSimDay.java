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

public class MainDataSimDay {

     static ArrayList<Integer> TIME;
	 static ArrayList<Double> GLUCOSA;
     static ArrayList<Double> CARBOHIDRATOS;
     static ArrayList<Double> INSULINA;




	private static void readDias(String problemList) throws FileNotFoundException, IOException{
	        System.out.println(problemList);

	        FileReader fr = new FileReader("PacienteSim/"+problemList);
	        BufferedReader br = new BufferedReader(fr);
	        String strLine;
	        int indexI=1;
	        TIME= new ArrayList<Integer>();
	        GLUCOSA= new ArrayList<Double>();
	        CARBOHIDRATOS= new ArrayList<Double>();
	        INSULINA= new ArrayList<Double>();	      
        	
	        double insulinAux=0;
	        double carbohidratesAux=0;
	        double glucoseAux=0;
	        int timeAux=0;
	        
	        strLine= br.readLine();
	        while (strLine  != null){
	        	
	        	strLine=br.readLine();

	            if(strLine!=null){

		        	StringTokenizer str = new StringTokenizer(strLine,";");
		            int indexJ=0;
		        	
		        	while(str.hasMoreElements()){
		           		String aux=str.nextToken();
		            	if(indexJ==0)
		            		glucoseAux=glucoseAux+Double.valueOf(aux.replace(",", "."));
		            	else if(indexJ==1)        
		            		insulinAux=insulinAux+Double.valueOf(aux.replace(",", "."));       
		            	else if(indexJ==2)
		            		carbohidratesAux=carbohidratesAux+Double.valueOf(aux.replace(",", "."));
		                indexJ++;              
		        	}
	            	if((indexI%5)==0){
                    	INSULINA.add(insulinAux);
    				    CARBOHIDRATOS.add(carbohidratesAux);
    				    GLUCOSA.add(glucoseAux/5);
    				    TIME.add(dameTiempo(timeAux));
    				    insulinAux=0;
    				    carbohidratesAux=0;
    				    glucoseAux=0;
    				    timeAux=timeAux+1;
	            	}
	            }
		        indexI++;
	        }
	        
        	CreaDiaPaciente(problemList);
	        br.close();
	        fr.close();
	    }
	 
	


	private static Integer dameTiempo(int timeAux) {

		int horas= timeAux*5/60;
		double minutes=timeAux*5-horas*60;
		int result= horas*100+((int)minutes);
	//	System.out.println(result);
		return result;
	}




	private static void CreaDiaPaciente(String problemList) throws IOException {
        FileWriter fich = null;
		PrintWriter pSo = null;   
        File directorio = new File("PacientesDias/PacienteSim");
        directorio.mkdir();
        fich = new FileWriter("PacientesDias/PacienteSim/"+"D"+problemList+".csv");
        pSo = new PrintWriter(fich);
        for(int i=0; i<TIME.size();i++){
        		pSo.println(GLUCOSA.get(i)+";"+CARBOHIDRATOS.get(i)+";"+INSULINA.get(i)+";"+TIME.get(i)+";");      		
    	   }   
    	pSo.close();	
	}



    public static void main (String[] args) {
	    String DirSource = "PacienteSim";
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