package charts;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;

/**
 * 
 * @author micelab1
 *
 */
public class PlotsJFrame {

	private GridJPanels panelRoot;
	private JFrame   frameRoot;
	
    public PlotsJFrame(String wtitle) {
    	frameRoot=new JFrame(wtitle);
   // 	frameRoot.setState(Frame.ICONIFIED);
    }

	public void draw(ArrayList<PanelGrafica> listaVentanas2) {
	    frameRoot.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frameRoot.setLayout(new BorderLayout());
        panelRoot=new GridJPanels(listaVentanas2);
        frameRoot.add(panelRoot);
        frameRoot.pack();
        frameRoot.setLocationRelativeTo(null);
        frameRoot.setVisible(true);
		panelRoot.dibuja();

	}
    
	public JFrame	getFrameRoot(){
		return frameRoot;
	}
}