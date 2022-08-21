package com.libertrobin.logic.strat_reader;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JButton;

/**
 * This class represents a case in the matrix of cards in GUI.
 * @author Robin Libert
 *
 */
public class MultipleColorsButton extends JButton{
	private static final long serialVersionUID = 1L;
	private String text;
	private double[] strategy;
	private Color[] myColors = new Color[] {new Color(134, 134, 134), new Color(100, 255, 150), new Color(255,0,0), new Color(200,0,0), new Color(145,0,0)};
	
	public MultipleColorsButton(String text, double[] strategy) {
		//super(text, null);
		this.text = text;
		this.strategy = strategy;
		this.setBorder(BorderFactory.createLineBorder(Color.black,1));
	}
	
	@Override
	public void paintComponent(Graphics g) {
		int sum = 0;
		int colorSize = 0;
		for(int i = 0; i < this.strategy.length; i++) {
			g.setColor(myColors[i]);
			colorSize = (int)(getWidth()*strategy[i]);
			//System.out.println("Width = "+getWidth()+ " : Color size = "+colorSize);
			g.fillRect(sum, 0, colorSize, getHeight());
			sum += colorSize;
		}
		g.setColor(Color.black);
		g.drawString(this.text, getWidth()/6, getHeight()/4);
		//g.dispose();
	}
	
	public void setStrategy(double[] strategy) {
		this.strategy = strategy;
	}
	
	public String getText() {
		return text;
	}
	
	public double[] getStrategy() {
		return this.strategy;
	}


}
