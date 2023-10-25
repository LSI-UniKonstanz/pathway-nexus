package org.vanted.addons.matrix.utils;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

/**
 * Code taken from https://coderanch.com/t/341737/java/Expand-Collapse-Panels and modified
 * @author Philipp Eberhard
 *
 */
public class JCollapsablePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private boolean isCollapsed;
	private JPanel contentPanel_;
	private HeaderPanel headerPanel_;
 
    private class HeaderPanel extends JPanel implements MouseListener {

		private static final long serialVersionUID = 1L;
		
		String text_;
        Font font;
        BufferedImage open, closed;
        final int OFFSET = 30, PAD = 5;
 
        public HeaderPanel(String text) {
            addMouseListener(this);
            text_ = text;
            font = new Font("sans-serif", Font.PLAIN, 12);
            // setRequestFocusEnabled(true);
            setPreferredSize(new Dimension(150, 20));
            int w = getWidth();
            int h = getHeight();
 
            /*try {
                open = ImageIO.read(new File("images/arrow_down_mini.png"));
                closed = ImageIO.read(new File("images/arrow_right_mini.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }*/
 
        }
 
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int h = getHeight();
            /*if (selected)
                g2.drawImage(open, PAD, 0, h, h, this);
            else
                g2.drawImage(closed, PAD, 0, h, h, this);
                        */ // Uncomment once you have your own images
            g2.setFont(font);
            FontRenderContext frc = g2.getFontRenderContext();
            LineMetrics lm = font.getLineMetrics(text_, frc);
            float height = lm.getAscent() + lm.getDescent();
            float x = OFFSET;
            float y = (h + height) / 2 - lm.getDescent();
            g2.drawString(text_, x, y);
        }
 
        public void mouseClicked(MouseEvent e) {
            toggleSelection();
        }
 
        public void mouseEntered(MouseEvent e) {
        }
 
        public void mouseExited(MouseEvent e) {
        }
 
        public void mousePressed(MouseEvent e) {
        }
 
        public void mouseReleased(MouseEvent e) {
        }
 
    }
 
    /**
     * is not used. serves as template
     * @param text
     * @param panel
     */
    public JCollapsablePanel(String text, JPanel panel) {
        super(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(1, 3, 0, 3);
        gbc.weightx = 0.8;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
 
        isCollapsed = true;
        headerPanel_ = new HeaderPanel(text);					//length of the label can be set in constructor of HeaderPanel
 
        setBackground(new Color(200, 200, 220));
        contentPanel_ = panel;
        
        add(headerPanel_, gbc);
        gbc.weightx = 1.0;
        add(contentPanel_, gbc);
        contentPanel_.setVisible(true);
 
        JLabel padding = new JLabel();
        gbc.weighty = 1.0;
        add(padding, gbc);
 
    }
 
    /**
     * is used to create the deletedButtons frame in SideBar
     * @param text
     * @param panel
     * @param addFunction
     */
    public JCollapsablePanel(String text, JPanel panel, JButton addFunction) {
    	super(new GridBagLayout());
                
        isCollapsed = true;
 
        setBackground(new Color(200, 200, 220));
        
        headerPanel_ = new HeaderPanel(text);							//length of the label can be set in constructor of HeaderPanel
	        GridBagConstraints headerGBC = new GridBagConstraints();
	        headerGBC.insets = new Insets(1, 3, 0, 0);
	        headerGBC.weightx = 0.8;
	        headerGBC.gridx = 0;
	        headerGBC.gridy = 0;
	        headerGBC.fill = GridBagConstraints.HORIZONTAL;
	        add(headerPanel_, headerGBC);
	        
        contentPanel_ = panel;
        	GridBagConstraints contentGBC = new GridBagConstraints();
        	contentGBC.insets = new Insets(1, 3, 0, 3);
        	contentGBC.weightx = 1.0;
        	contentGBC.fill = GridBagConstraints.HORIZONTAL;
        	contentGBC.gridwidth = GridBagConstraints.REMAINDER;
        	contentGBC.gridx = 0;
        	contentGBC.gridy = 1;
        	contentGBC.anchor = GridBagConstraints.NORTH;
        	add(contentPanel_, contentGBC);
        	contentPanel_.setVisible(true);
 
        JLabel padding = new JLabel();
	        contentGBC.weighty = 1.0;
	        add(padding, contentGBC);
	    	
        GridBagConstraints buttongbc = new GridBagConstraints();
        buttongbc.gridx = 1;
        buttongbc.gridy = 0;
        buttongbc.weightx = 0.2;
        buttongbc.gridwidth = GridBagConstraints.REMAINDER;
        
        add(addFunction, buttongbc);
        
    }
    
    public void toggleSelection() {
        isCollapsed = !isCollapsed;
 
        if (contentPanel_.isShowing())
            contentPanel_.setVisible(false);
        else
            contentPanel_.setVisible(true);
 
        validate();
 
        this.firePropertyChange("selected", !isCollapsed, isCollapsed);
        
        headerPanel_.repaint();
    }
 
    public void setCollapsed(boolean collapsed) {
    	isCollapsed = collapsed;
        contentPanel_.setVisible(collapsed);
    
        validate();
        
        headerPanel_.repaint();
    }
}