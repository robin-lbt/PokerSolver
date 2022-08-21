package com.libertrobin.logic.strat_reader;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Graphical user interface to display a strategy of Flop Hold'em
 * @author Robin Libert
 *
 */
public class GUI extends JFrame  implements ActionListener{
	private static final long serialVersionUID = 1L;
	private static final int WIDTH = 1600;
	private static final int HEIGHT = 1000;
	private static final int MATRIX_SIZE = 910;
	private HashMap<String, MultipleColorsButton> rangeButtons = new HashMap<>();
	private FlopHoldemStrategyReader reader;
	private String preflopActions = "";
	private String flop = "";
	private String postflopActions= "";
	private JTextField tf1, tf2, tf3;
	private JButton actionButton;
	private JPanel matrixPanel;
	private JPanel mainPanel;
	private JPanel nextActionsPanel;
	private JPanel infosPanel;
	private JLabel infosLabel;
	private JLabel infosLabel2;
	Map<String, Object> parameters;
	
	/**
	 * Loads a strategy and displays the strategy on the screen. 
	 * The GUI is initialized with the strategy of the dealer during the first betting round.
	 * @param strategiesFileName the path to the strategy file
	 * @param parametersFileName the path to the parameters file
	 */
	public GUI(String strategiesFileName, String parametersFileName){
		super("Flop Holdem Solver");
		this.setSize(WIDTH, HEIGHT);
		//loads the strategy
		this.reader = new FlopHoldemStrategyReader(strategiesFileName, parametersFileName);
		this.parameters = this.reader.getParameters();
		//Displays the parameters of the game and the simulation 
		@SuppressWarnings("unchecked")
		ArrayList<Double> gameValues = (ArrayList<Double>)parameters.get("gameValues");
		String infosString = "<html>Number of informations sets: " + parameters.get("numberOfInformationSets").toString() + 
				"<br/>Number of iterations: " + parameters.get("iterNumber").toString()+
				"<br/>Game value: " + gameValues.get(gameValues.size()-1).toString()+
				"<br/> Starting money: " + parameters.get("startingMoney").toString()+
				"<br/> Small blind: " + parameters.get("sb").toString()+
				"<br/> Big blind: " + parameters.get("bb").toString()+
				"<br/></html>";
		//Creates the GUI
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
		nextActionsPanel = new JPanel();
		nextActionsPanel.setLayout(new GridLayout(0,4));
		nextActionsPanel.setMaximumSize(new Dimension(200, 50));
		matrixPanel = new JPanel();
		matrixPanel.setLayout(new GridLayout(13, 13));
		matrixPanel.setMaximumSize(new Dimension(MATRIX_SIZE, MATRIX_SIZE));
		infosPanel = new JPanel();
		infosPanel.setLayout(new GridLayout(2,0) );
		infosLabel = new JLabel(infosString);
		infosPanel.add(infosLabel);
		infosLabel2 = new JLabel("");
		infosPanel.add(infosLabel2);
		//Creates and fills the matrix with the strategy of the dealer during the first betting round
		MultipleColorsButton btn;
		String btnText;
		for (int i=14; i>=2; i--) {
		    for (int j=14; j>=2; j--) {
		    	String firstCard;
		    	String secondCard;
		    	String suit;
		    	if(i == 14) {
		    		firstCard = "A";
		    	}else if(i == 13) {
		    		firstCard = "K";
		    	}else if(i == 12) {
		    		firstCard = "Q";
		    	}else if(i == 11) {
		    		firstCard = "J";
		    	}else if(i == 10) {
		    		firstCard = "T";
		    	}else {
		    		firstCard = Integer.toString(i);
		    	}
		    	if(j == 14) {
		    		secondCard = "A";
		    	}else if(j == 13) {
		    		secondCard = "K";
		    	}else if(j == 12) {
		    		secondCard = "Q";
		    	}else if(j == 11) {
		    		secondCard = "J";
		    	}else if(j == 10) {
		    		secondCard = "T";
		    	}else {
		    		secondCard = Integer.toString(j);
		    	}
		    	if(i == j) {
		    		suit = "";
		    	}else if(i > j) {
		    		suit = "s";
		    	}else {
		    		String temp = firstCard;
		    		firstCard = secondCard;
		    		secondCard = temp;
		    		suit = "o";
		    	}
		    	btnText = firstCard+secondCard+suit;
		    	//retrieves and displays the average strategy of the private cards associated to the corresponding case of the matrix
		    	btn = new MultipleColorsButton(firstCard+secondCard+suit, reader.getAverageStrategy(firstCard+secondCard+suit+" "));
		        btn.setPreferredSize(new Dimension(MATRIX_SIZE/13, MATRIX_SIZE/13));
		        btn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						displayInfos(e);
					}
		        });
		        rangeButtons.put(btnText, btn);
		        matrixPanel.add(rangeButtons.get(btnText));
		    }
		}
		tf1 = new JTextField();
		tf2 = new JTextField();
		tf3 = new JTextField();
		actionButton = new JButton("Run");
		actionButton.addActionListener(this);
		nextActionsPanel.add(tf1);
		nextActionsPanel.add(tf2);
		nextActionsPanel.add(tf3);
		nextActionsPanel.add(actionButton);
		mainPanel.add(nextActionsPanel);
		mainPanel.add(matrixPanel);
		mainPanel.add(infosPanel);
		this.setContentPane(mainPanel);
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	/**
	 * Displays informations about the average strategy of the corresponding button that had been clicked on.
	 * @param e
	 */
	private void displayInfos(ActionEvent e) {
		MultipleColorsButton btn = (MultipleColorsButton)e.getSource();
		String name = btn.getText();
		String strat = "";
		double[] strategy = btn.getStrategy();
		for(int i = 0; i < strategy.length;i++) {
			strat += strategy[i] + "<br/>";
		}
		this.infosLabel2.setText("<html>"+ name +"<br/>"+strat+"</html>" );
	}
	
	/**
	 * Updates the matrix when we enter an action
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		this.preflopActions = this.tf1.getText();
		this.flop = this.tf2.getText();
		this.postflopActions = this.tf3.getText();
		for (int i=14; i>=2; i--) {
		    for (int j=14; j>=2; j--) {
		    	String firstCard;
		    	String secondCard;
		    	String suit;
		    	if(i == 14) {
		    		firstCard = "A";
		    	}else if(i == 13) {
		    		firstCard = "K";
		    	}else if(i == 12) {
		    		firstCard = "Q";
		    	}else if(i == 11) {
		    		firstCard = "J";
		    	}else if(i == 10) {
		    		firstCard = "T";
		    	}else {
		    		firstCard = Integer.toString(i);
		    	}
		    	if(j == 14) {
		    		secondCard = "A";
		    	}else if(j == 13) {
		    		secondCard = "K";
		    	}else if(j == 12) {
		    		secondCard = "Q";
		    	}else if(j == 11) {
		    		secondCard = "J";
		    	}else if(j == 10) {
		    		secondCard = "T";
		    	}else {
		    		secondCard = Integer.toString(j);
		    	}
		    	if(i == j) {
		    		suit = "";
		    	}else if(i > j) {
		    		suit = "s";
		    	}else {
		    		String temp = firstCard;
		    		firstCard = secondCard;
		    		secondCard = temp;
		    		suit = "o";
		    	}
		    	if(flop.equals("") && postflopActions.equals("")) {
		    		rangeButtons.get(firstCard+secondCard+suit).setStrategy(reader.getAverageStrategy(firstCard+secondCard+suit+" "+preflopActions));
				}else if(!preflopActions.equals("") && !flop.equals("") && postflopActions.equals("")) {
					if(suit.equals("o")) {
						rangeButtons.get(firstCard+secondCard+suit).setStrategy(reader.getAverageStrategy(firstCard+secondCard+" "+preflopActions+" "+flop+" "));
					}
					if(suit.equals("s") && flop.length() == 3) {
						rangeButtons.get(firstCard+secondCard+suit).setStrategy(new double[] {1});
					}
					if(suit.equals("s") && flop.length() == 4) {
						rangeButtons.get(firstCard+secondCard+suit).setStrategy(reader.getAverageStrategy(firstCard+secondCard+suit+" "+preflopActions+" "+flop+" "));
					}
					if(suit.equals("")) {
						rangeButtons.get(firstCard+secondCard+suit).setStrategy(reader.getAverageStrategy(firstCard+secondCard+suit+" "+preflopActions+" "+flop+" "));
					}
				}else if(!preflopActions.equals("") && !flop.equals("") && !postflopActions.equals("")) {
					if(suit.equals("o")) {
						rangeButtons.get(firstCard+secondCard+suit).setStrategy(reader.getAverageStrategy(firstCard+secondCard+" "+preflopActions+" "+flop+" "+postflopActions));
					}
					if(suit.equals("s") && flop.length() == 3) {
						rangeButtons.get(firstCard+secondCard+suit).setStrategy(new double[] {1});
					}
					if(suit.equals("s") && flop.length() == 4) {
						rangeButtons.get(firstCard+secondCard+suit).setStrategy(reader.getAverageStrategy(firstCard+secondCard+suit+" "+preflopActions+" "+flop+" "+postflopActions));
					}
					if(suit.equals("")) {
						rangeButtons.get(firstCard+secondCard+suit).setStrategy(reader.getAverageStrategy(firstCard+secondCard+suit+" "+preflopActions+" "+flop+" "+postflopActions));
					}
				}
		    	this.setContentPane(mainPanel);
		    }
		}
	}

	public static void main(String[] args) {
		String strategiesFileName = "flopstrat_1_1_6_bet_pot_ai_raise_2x_ai";
		String parametersFileName = "flopparam_1_1_6_bet_pot_ai_raise_2x_ai";
		GUI gui = new GUI(strategiesFileName, parametersFileName);
	}
}
