package com.libertrobin.logic.strat_reader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import com.libertrobin.logic.filemanager.NoLimitLeducFileManager;
import com.libertrobin.logic.trainer.Trainer;

/**
 * This class contains the tools to read a No Limit Leduc poker strategy.
 * @author Robin Libert
 *
 */
public class NoLimitLeducStrategyReader {

	private static final Pattern NUMBER_PATTERN = Pattern.compile("[0-9]");
	private static final Pattern NOT_NUMBER_PATTERN = Pattern.compile("[^-?0-9]+");
	private TreeMap<String, Trainer.Node> gameTree;
	private Map<String, Object> parameters;
	private NoLimitLeducFileManager fileManager;
	
	/**
	 * Constructor: loads a Leduc poker strategy according to the paths
	 * @param strategiesFileName path to the strategy file
	 * @param parametersFileName path to the parameters file
	 */
	public NoLimitLeducStrategyReader(String strategiesFileName, String parametersFileName) {
		this.fileManager = new NoLimitLeducFileManager();
		this.fileManager.setStrategyFileName(strategiesFileName);
		this.fileManager.setParametersFileName(parametersFileName);
		System.out.println("Load strategy...");
		this.gameTree = this.fileManager.loadStrategy();
		this.parameters = this.fileManager.loadParameters();
		System.out.println("Strategy loaded.");
	}
	
	/**
	 * Displays the whole game tree in the console.
	 */
	public void printGameTree() {
		for (Trainer.Node n : this.gameTree.values()) {
			System.out.println(n);
		}
	}
	
	/**
	 * Displays an information set in the console (e.g. "K pp Q b9", "J p")
	 * @param node an information set
	 */
	public void printNode(String node) {
		System.out.println(this.gameTree.get(node));
	}
	
	/**
	 * Displays the strategy of in a public history for the whole range (the set of private cards a player can have)
	 * @param history a public history (e.g. "pp Q b9", "", "p", "r9c9 K ")
	 */
	public void printRange(String history) {
		String[] range = new String[] {"K", "Q", "J"};
		for(int i = 0; i < range.length; i++) {
			printNode(range[i] + " " + history);
		}
	}
	
	/**
	 * Displays the next valid actions for a given public history.
	 * E.g. history="pp Q b9" -> p, c9
	 * @param history a public history
	 */
	public void printNextValidActions(String history) {
		ArrayList<String> actions = getNextPossibleActions(history);
		for(String action: actions) {
			System.out.println(action);	
		}
	}
	
	private ArrayList<String> getNextPossibleActions(String history) {
		ArrayList<String> actions = new ArrayList<>();
		String previousActionsWithoutSizings = history.replaceAll("[0-9]", "");
		if(previousActionsWithoutSizings.length() == 0) {
			actions.add("p");
			for(int i = 1; i <= (int)this.parameters.get("startingMoney"); i++) {
				if(this.gameTree.get("J b"+i) != null) {
					actions.add("b" + i);
				}
			}
		}else {
			if(previousActionsWithoutSizings.substring(previousActionsWithoutSizings.length()-1, previousActionsWithoutSizings.length()).equals(" ") ) {
				actions.add("p");
				for(int i = 1; i <= (int)this.parameters.get("startingMoney"); i++) {
					if(this.gameTree.get("J "+ history + "b"+i) != null) {
						actions.add("b" + i);
					}
				}
			}else if(previousActionsWithoutSizings.substring(previousActionsWithoutSizings.length()-1, previousActionsWithoutSizings.length()).equals("p") ) {
				actions.add("p");
				for(int i = 1; i <= (int)this.parameters.get("startingMoney"); i++) {
					if(this.gameTree.get("J "+ history + "b"+i) != null) {
						actions.add("b"+i);
					}
				}
			}else if(previousActionsWithoutSizings.substring(previousActionsWithoutSizings.length()-1, previousActionsWithoutSizings.length()).equals("b") || 
					previousActionsWithoutSizings.substring(previousActionsWithoutSizings.length()-1, previousActionsWithoutSizings.length()).equals("r")) {
				actions.add("p");
				if(previousActionsWithoutSizings.substring(previousActionsWithoutSizings.length()-1, previousActionsWithoutSizings.length()).equals("b")) {
					int lastIndex = history.lastIndexOf("b");
					String value = history.substring(lastIndex+1, history.length());
					actions.add("c"+value);
				}else if(previousActionsWithoutSizings.substring(previousActionsWithoutSizings.length()-1, previousActionsWithoutSizings.length()).equals("r")) {
					int lastIndex = history.lastIndexOf("r");
					String value = history.substring(lastIndex+1, history.length());
					actions.add("c"+value);
				}
				for(int j = 1; j <= (int)this.parameters.get("startingMoney"); j++) {
					if(this.gameTree.get("J "+ history + "r"+j) != null) {
						actions.add("r"+j);
					}
				}
			}
		}	
		return actions;
	}
	
	
	
	private int getPotSizeUtil(String[] splited, int potSize, int index) {
		//String[] historyWithoutSizings = splited[index].replaceAll("[0-9]", "").trim().split("");
		String[] historyWithoutSizings = NUMBER_PATTERN.matcher(splited[index]).replaceAll("").trim().split("");
		//String[] sizings = splited[index].replaceAll("[^-?0-9]+", " ").trim().split(" ");
		String[] sizings = NOT_NUMBER_PATTERN.matcher(splited[index]).replaceAll(" ").trim().split(" ");
		int betIndex = 0;
		int previousBet = 0;
		for(int i=0; i < historyWithoutSizings.length; i++) {
			if(historyWithoutSizings[i].equals("b")) {
				previousBet = Integer.parseInt(sizings[betIndex]);
				betIndex++;
				potSize+=previousBet;
			}else if(historyWithoutSizings[i].equals("c")) {
				previousBet = 0;
				potSize+=Integer.parseInt(sizings[betIndex]);
				betIndex++;
			}else if(historyWithoutSizings[i].equals("r")) {
				potSize+=previousBet + Integer.parseInt(sizings[betIndex]);
				previousBet = Integer.parseInt(sizings[betIndex]);
				betIndex++;
			}
		}
		return potSize;
	}
	
	/**
	 * Get the size of the pot for a given public history.
	 * @param history a public history
	 * @return the size of the pot
	 */
	public int getPotSize(String history) {
		int potSize = (int)this.parameters.get("bb") + (int)this.parameters.get("sb"); // each player post a blind
		String[] splited = history.split(" ");
		if(splited.length >= 1) {
			potSize = getPotSizeUtil( splited, potSize, 0);
		}
		if(splited.length == 3) {
			potSize = getPotSizeUtil( splited, potSize, 2);
		}
		return potSize;
	}
	
	private int getAmountInvestedByPlayerUtil(String[] splited, int investedAmount, int index, int player) {
		String[] historyWithoutSizings = NUMBER_PATTERN.matcher(splited[index]).replaceAll("").trim().split("");
		String[] sizings = NOT_NUMBER_PATTERN.matcher(splited[index]).replaceAll(" ").trim().split(" ");
		int betIndex = 0;
		int previousBet = 0;
		for(int i=0; i < historyWithoutSizings.length; i++) {
			if(historyWithoutSizings[i].equals("b")) {
				previousBet = Integer.parseInt(sizings[betIndex]);
				betIndex++;
				if(i % 2 == player)
					investedAmount+=previousBet;
			}else if(historyWithoutSizings[i].equals("c")) {
				previousBet = 0;
				if(i % 2 == player)
					investedAmount+=Integer.parseInt(sizings[betIndex]);
				betIndex++;
			}else if(historyWithoutSizings[i].equals("r")) {
				if(i % 2 == player)
					investedAmount+=previousBet + Integer.parseInt(sizings[betIndex]);
				previousBet = Integer.parseInt(sizings[betIndex]);
				betIndex++;
			}
		}
		return investedAmount;
	}
	
	/**
	 * Get the amount of money invested by a player in a given public history.
	 * @param history a public history
	 * @param player the player (0 or 1)
	 * @return the amount of money invested by player in history
	 */
	public int getAmountInvestedByPlayer(String history, int player) {
		int investedAmount = 1; // 1bb ante
		String[] splited = history.split(" ");
		if(splited.length >= 1) {
			investedAmount = getAmountInvestedByPlayerUtil( splited, investedAmount, 0, player);
		}
		if(splited.length == 3) {
			investedAmount = getAmountInvestedByPlayerUtil( splited, investedAmount, 2, player);
		}
		return investedAmount;
	}
	
	/**
	 * Method to save a CSV file of the game values.
	 * @param filename path to store the file
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void convertGameValuesToCSV(String filename) throws IOException {
		File file = new File(filename);
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);
        int i = 1;
		for(double value : (ArrayList<Double>)this.parameters.get("gameValues")) {
			bw.write(i+","+value);
            bw.newLine();
            i+=1;
		}
		bw.close();
        fw.close();
	}

	public static void main(String[] args) {
		String strategiesFileName = "leducstrat_1_1_10_bet_pot_ai_raise_2x_ai";
		String parametersFileName = "leducparam_1_1_10_bet_pot_ai_raise_2x_ai";
		NoLimitLeducStrategyReader reader = new NoLimitLeducStrategyReader(strategiesFileName, parametersFileName);
		String history = "pp Q b9";
		reader.printRange(history);
		reader.printNextValidActions(history);
	}
}
