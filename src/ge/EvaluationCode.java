package ge;

import java.util.ArrayList;

/**
 * 
 * @author micelab1
 *
 */
public class EvaluationCode {

    public StringBuffer code = new StringBuffer();    

    public EvaluationCode(){}

    public StringBuffer createCode1(long threadId) {
        //Head
	// Set package for code to be generated
        code.append("package algorithm;\n\n"); 
	// Chose the class that the code should extend
        code.append("public class PopEvaluator").append(threadId).append(" extends AbstractPopEvaluator {\n\n");
	// Create the constructor for the new class
        code.append("\tpublic PopEvaluator").append(threadId).append("() {\n\tsuper();fit=0;}\n\tprivate double fit;\n");
    return code;
    }
    
    
    public void createCode2(ArrayList<String> exprList) {    
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
	    	    
	    for (int i = 0; i < exprList.size(); ++i) {
	    	code.append("\tpublic void f").append(i).append("(int k){\n");
	        code.append("\t\tdouble result = 0.0;\n");
	    	code.append(exprList.get(i));
	    	code.append("\t\tfit=result;\n");
	    	code.append("\t}\n");
	    }
	    code.append("}\n"); //End class

    }

    
    public String getCode(){
    	return code.toString();	
    }
     
}
