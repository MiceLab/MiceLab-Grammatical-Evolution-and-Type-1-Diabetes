package util;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.stat.StatUtils;

/**
 * Utility methods for statistics.
 * 
 * @author J. M. Colmenar
 */
public class UtilStats {

    public static double computeRSME(double[] l1, double[] l2) {
        double acu = 0.0;

        for (int i = 0; i < l1.length; i++) {
            acu += Math.pow(l1[i] - l2[i], 2);
        }

        return Math.sqrt(acu / l1.length);
    }
    
    public static double computeAvgError(double[] expected, double[] observed) {
        double error = 0.0;
        for (int k = 0; k < expected.length; ++k) {
            error += Math.abs(expected[k] - observed[k]);
        }
        error /= expected.length;
        return error;
    }    
    
    public static double getAverage(double[] darr) {
        return StatUtils.mean(darr);
    }
    
    public static double calculatePvalueChiSquare(double[] expectedIn, double[] observedIn) {
        double pValue = 0.0;
        
        double acu = 0.0;
        for (int i=0; i<expectedIn.length; i++) {
            acu += (Math.pow(expectedIn[i]-observedIn[i], 2)) / expectedIn[i];
        }
        
        ChiSquaredDistribution chiDist = new ChiSquaredDistribution((double) expectedIn.length-1);
        try {
            pValue = 1.0 - chiDist.cumulativeProbability(acu);
        } catch (Exception e) {
            System.err.println("Error calculating p-value: "+e.getLocalizedMessage());
            System.err.println("--> Accumulated value was: "+acu);
        }
        return pValue;
    }
}
