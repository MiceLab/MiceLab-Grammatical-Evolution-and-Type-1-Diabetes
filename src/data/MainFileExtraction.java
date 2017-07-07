package data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class MainFileExtraction {
	     
	     public static void main (String[] args) {
	    	    String DirSource = "SERIES";
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
	    		  MedtronicExtraction file1= new MedtronicExtraction(ProblemList[j]);
	    		  file1.mainExtractFile();
	    	  }
	 }
}