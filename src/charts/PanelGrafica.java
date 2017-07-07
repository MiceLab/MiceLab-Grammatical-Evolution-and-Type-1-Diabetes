package charts;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RefineryUtilities;

/**
 *
 * @author micelab1
 */
public class PanelGrafica extends JPanel {

    private static final long serialVersionUID = 1L;
    protected XYSeries serieAct;
    protected XYSeries seriePrd;
    private JPanel jPanel1;
    private JScrollPane jScrollPane1;
    private JPanel panelPlotInput;
    private JTextArea txtStats;
	private String name;
	private String stats;



    /**
     * Creates new form ScopeV2
     */
    public PanelGrafica( String title, String xTitle, String yTitle) {
        panelPlotInput = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtStats = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        serieAct = new XYSeries(yTitle + " - Actual");
        seriePrd = new XYSeries(yTitle + " - Predicted");   
        name=title;
        stats=new String();
    }

    public PanelGrafica() {

	}

 
    
	protected void inicializeComponents(String title) {
       
        panelPlotInput = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtStats = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();

        panelPlotInput.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout panelPlotInputLayout = new javax.swing.GroupLayout(panelPlotInput);
        panelPlotInput.setLayout(panelPlotInputLayout);
        panelPlotInputLayout.setHorizontalGroup(
            panelPlotInputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        panelPlotInputLayout.setVerticalGroup(
            panelPlotInputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        txtStats.setColumns(20);
        txtStats.setRows(5);
        jScrollPane1.setViewportView(txtStats);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);

        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING));
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING));
         
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()               
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(panelPlotInput, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE))
                )
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelPlotInput, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        
        
    }




	public String getName(){
	    	return name;
	}
	   
    public void addComment(String str) {
        stats=str;
    }
    
    public void renoveComment() {
        this.txtStats.setText(stats);
    }
    
    public void addPointAct(double x, double y) {
        serieAct.add(x, y);
    }

    public void addPointPrd(double x, double y) {
        seriePrd.add(x, y);
    }
    
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(1400, 600);
    }

	public void createInputDataChart(String title) {
        // Panel for plotting data
        this.panelPlotInput.setLayout(new java.awt.BorderLayout());
        
        XYSeriesCollection dataSet = new XYSeriesCollection();
        
        dataSet.addSeries(serieAct);
        dataSet.addSeries(seriePrd); 
        
        JFreeChart chart = ChartFactory.createXYLineChart("", "", "", dataSet, PlotOrientation.VERTICAL, true, false, false);
        chart.setTitle(new org.jfree.chart.title.TextTitle(title,new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12)));
        
   
        chart.getXYPlot().setDomainAxis(new NumberAxis());
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(panelPlotInput.getWidth(), panelPlotInput.getHeight()));
        chartPanel.setMouseZoomable(true, false);
        chart.removeLegend();
        
        XYPlot plot = (XYPlot) chart.getPlot(); 
        plot.setBackgroundPaint(Color.white); 
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY); 
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY); 
        //plot.getRangeAxis().setVisible(false);
        plot.getDomainAxis().setVisible(false);
        plot.setBackgroundPaint(Color.DARK_GRAY);
        plot.setRangeGridlinePaint(Color.white);
        
        DeviationRenderer renderer = new DeviationRenderer(true, false); 
        renderer.setSeriesPaint(0, new Color(0,250,0)); 
        renderer.setSeriesPaint(1, new Color(150,150,250)); 
        plot.setRenderer(renderer); 
        plot.setDomainCrosshairVisible(true); 
        plot.setRangeCrosshairVisible(true); 
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis(); 
        yAxis.setAutoRangeIncludesZero(false); 
    
        panelPlotInput.add(chartPanel, java.awt.BorderLayout.CENTER);
        panelPlotInput.validate();
		renoveComment();
	}
}
