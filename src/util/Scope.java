/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.JFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RefineryUtilities;

/**
 *
 * @author jlrisco
 */
public class Scope extends JFrame {

    private static final long serialVersionUID = 1L;
    protected XYSeries serieAct;
    protected XYSeries seriePrd;

    public Scope(String windowsTitle, String title, String xTitle, String yTitle) {
        super(windowsTitle);
        XYSeriesCollection dataSet = new XYSeriesCollection();
        serieAct = new XYSeries(yTitle + " - Actual");
        seriePrd = new XYSeries(yTitle + " - Predicted");
        dataSet.addSeries(serieAct);
        dataSet.addSeries(seriePrd);
        JFreeChart chart = ChartFactory.createXYLineChart(title, xTitle, yTitle, dataSet, PlotOrientation.VERTICAL, true, false, false);
        chart.getXYPlot().setDomainAxis(new NumberAxis());
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        chartPanel.setMouseZoomable(true, false);
        setContentPane(chartPanel);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        super.pack();
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);
    }

    public void addPointAct(double x, double y) {
        serieAct.add(x, y);
    }

    public void addPointPrd(double x, double y) {
        seriePrd.add(x, y);
    }
    
    public void clear() {
        List items = serieAct.getItems();
        for(Object item : items) {
            ((XYDataItem)item).setY(0);
        }
        items = seriePrd.getItems();
        for(Object item : items) {
            ((XYDataItem)item).setY(0);
        }
    }

}
