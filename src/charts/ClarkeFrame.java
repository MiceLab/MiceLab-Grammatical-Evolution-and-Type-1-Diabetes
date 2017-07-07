package charts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Shape;
import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeUtilities;

/**
 * @see http://stackoverflow.com/a/20359200/230513
 * @see http://stackoverflow.com/a/6669529/230513
 */
public class ClarkeFrame extends JFrame {

    private static final int SIZE = 1000;
    private final XYSeries series = new XYSeries("Data");
	private XYSeriesCollection data;

    public ClarkeFrame(String s) {
        super(s);
    }

    public ChartPanel createDemoPanel() {
        JFreeChart chart = ChartFactory.createScatterPlot("Error Grid Clarke", "X", "Y", data, PlotOrientation.VERTICAL, true, true, false);
              
        XYPlot xyPlot = (XYPlot) chart.getPlot();
        xyPlot.setDomainCrosshairVisible(true);
        xyPlot.setRangeCrosshairVisible(true);
        Image image = new ImageIcon(getClass().getResource("clarke.png")).getImage();
		xyPlot.setBackgroundImage(image);
        xyPlot.setRenderer(new XYLineAndShapeRenderer(false, true) {
        	
            @Override
            public Shape getItemShape(int row, int col) {
                    return ShapeUtilities.createDiagonalCross(1,1);
            }
        });
        adjustAxis((NumberAxis) xyPlot.getDomainAxis(), true);
        adjustAxis((NumberAxis) xyPlot.getRangeAxis(), false);
        xyPlot.setBackgroundPaint(Color.white);
        
        ChartPanel cp= new ChartPanel(chart) {

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(SIZE, SIZE);
            }
        };

        this.add(cp, BorderLayout.CENTER);
        return cp;         
    }

    private void adjustAxis(NumberAxis axis, boolean vertical) {
        axis.setRange(0, 400);
        axis.setTickUnit(new NumberTickUnit(50));
        axis.setVerticalTickLabels(vertical);
    }

    public void createData(double rf[], double b[]) {
    	XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
        for (int i = 37; i < b.length-1; i++) {
       // 	System.out.println(rf[i]+"   "+ b[i]);
            series.add(rf[i], b[i]);
        }
        xySeriesCollection.addSeries(series);
        data= xySeriesCollection;
    }

}