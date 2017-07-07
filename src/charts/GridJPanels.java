package charts;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;

/**
 * 
 * @author micelab1
 *
 */
 public class GridJPanels extends JPanel {

    	private ArrayList<PanelGrafica> listaVentanas;
    			
        public GridJPanels(ArrayList<PanelGrafica> listaVentanas2) {
        	listaVentanas=listaVentanas2;
        	
            setLayout(new GridBagLayout());
            int largoMax=8;
            int j=listaVentanas.size()/largoMax;
            GridBagConstraints gbc = new GridBagConstraints();
            for (int row = 0; row < j+1; row++) {
                for (int col = 0; col < largoMax; col++) {
                	if((row*largoMax+col)<listaVentanas.size()){
	                    gbc.gridx = col;
	                    gbc.gridy = row;
	                    PanelGrafica cellPanelGrafica;
	                    cellPanelGrafica = listaVentanas.get(row*largoMax+col);	
	                    Border border = null;
	                    if (row < 4) {
	                        if (col < 4) {
	                            border = new MatteBorder(1, 1, 0, 0, Color.GRAY);
	                        } else {
	                            border = new MatteBorder(1, 1, 0, 1, Color.GRAY);
	                        }
	                    } else {
	                        if (col < 4) {
	                            border = new MatteBorder(1, 1, 1, 0, Color.GRAY);
	                        } else {
	                            border = new MatteBorder(1, 1, 1, 1, Color.GRAY);
	                        }
	                    }
	                    cellPanelGrafica.setBorder(border);
	                    add(cellPanelGrafica, gbc);
	                    cellPanelGrafica.inicializeComponents("");
                }
                }
            }
        }

		public void dibuja() {
			for (int x = 0; x < listaVentanas.size() ; x++) {
				listaVentanas.get(x).createInputDataChart(listaVentanas.get(x).getName());
			}
			
		}   
   }

