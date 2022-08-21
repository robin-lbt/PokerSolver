package com.libertrobin.logic.strat_reader;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Pattern;

import com.libertrobin.logic.filemanager.FlopHoldemFileManager;
import com.libertrobin.logic.trainer.Trainer;

/**
 * This class contains the tools to read a Flop Hold'em strategy.
 * @author Robin Libert
 *
 */
public class FlopHoldemStrategyReader {
	
	private static final Pattern NUMBER_PATTERN = Pattern.compile("[0-9]");
	private static final Pattern NOT_NUMBER_PATTERN = Pattern.compile("[^-?0-9]+");
	private TreeMap<String, Trainer.Node> gameTree;// The data structure where we put the Flop Hold'em strategy
	private Map<String, Object> parameters;// The data structure where we put parameters of the game and the simulation
	private FlopHoldemFileManager fileManager;
	
	/**
	 * Constructor: loads a strategy according to the access paths given in parameters.
	 * @param strategiesFileName
	 * @param parametersFileName
	 */
	public FlopHoldemStrategyReader(String strategiesFileName, String parametersFileName) {
		this.fileManager = new FlopHoldemFileManager();
		this.fileManager.setStrategyFileName(strategiesFileName);
		this.fileManager.setParametersFileName(parametersFileName);
		System.out.println("Load strategy...");
		this.gameTree = this.fileManager.loadStrategy();
		this.parameters = this.fileManager.loadParameters();
		System.out.println("Strategy loaded.");
	}
	
	/**
	 * Displays the average strategy of an information set in the console.
	 * Loop: Type an information set in the console (e.g. "AKo c1", "Q2 c1p AKQ p", "K6s r4c4 T89s ",...)
	 * 			to display the average strategy, print null if the information set doesn't exist
	 * 		 Type "end" to stop the program
	 */
	public void printNodeLoop() {
		try (Scanner scanner = new Scanner(System.in)) {
			String history;
			while(true) {
				System.out.println("Enter history or end to end the program: ");
				history = scanner.nextLine();
				if(history.equals("end"))
					break;
				System.out.println(this.gameTree.get(history));
			}
		}
	}
	
	/**
	 * Get the average strategy of an information set.
	 * This method is used in the graphical user interface (class GUI.java), if the information set doesn't exist
	 * we return an unique action with a probability of 1, which means PASS 100%.
	 * @param node an information set
	 * @return the average strategy of the information set node
	 */
	public double[] getAverageStrategy(String node) {
		if(this.gameTree.get(node) != null)
			return this.gameTree.get(node).getAverageStrategy();
		return new double[] {1.0};
	}
	
	private static int getPotSizeUtil(String[] splited, int potSize, int sb, int bb, int index) {
		String[] historyWithoutSizings = NUMBER_PATTERN.matcher(splited[index]).replaceAll("").trim().split("");
		String[] sizings = NOT_NUMBER_PATTERN.matcher(splited[index]).replaceAll(" ").trim().split(" ");
		int betIndex = 0;
		int previousBet = 0;
		for(int i=0; i < historyWithoutSizings.length; i++) {
			if(historyWithoutSizings[i].equals("b")) {
				previousBet = Integer.parseInt(sizings[betIndex]);
				betIndex++;
				potSize+=previousBet;
			}else if(historyWithoutSizings[i].equals("c")) {
				if(index == 0 && i == 0) {
					previousBet = bb;
				}else {
					previousBet = 0;
				}
				potSize+=Integer.parseInt(sizings[betIndex]);
				betIndex++;
			}else if(historyWithoutSizings[i].equals("r")) {
				if(index == 0 && i == 0) {
					potSize+=((bb-sb) + Integer.parseInt(sizings[betIndex]));
				}else {
					potSize+=previousBet + Integer.parseInt(sizings[betIndex]);
				}
				previousBet = Integer.parseInt(sizings[betIndex]);
				betIndex++;
			}
		}
		return potSize;
	}
	
	/**
	 * Get the size of the pot of a public history.
	 * @param history a public history (e.g. "c1p AKQ b4" return 8 if the blind are 1/2)
	 * @return the size of the pot for a given public history
	 */
	public int getPotSize(String history) {
		int sb = (int) this.parameters.get("sb");
		int bb = (int) this.parameters.get("bb");
		int potSize = sb+bb;
		String[] splited = history.split(" ");
		if(splited.length >= 1) {
			potSize = getPotSizeUtil( splited, potSize, sb, bb, 0);
		}
		if(splited.length == 3) {
			potSize = getPotSizeUtil( splited, potSize, sb, bb, 2);
		}
		return potSize;
	}
	
	/**
	 * Get the player to act.
	 * @param history a public history
	 * @return the player that act at a given history
	 */
	public int getCurrentPlayer(String history) {
		String historyWithoutSizings = NUMBER_PATTERN.matcher(history).replaceAll("");
		String[] splited = historyWithoutSizings.split(" ");
		if(splited.length == 3) {
			return (splited[2].length()+1) % 2;
		}else {
			return splited[0].length() % 2;
		}
	}
	
	/**
	 * Get the number of the betting round according to a public history
	 * @param history a public history
	 * @return the number of the betting round
	 */
	public int getRoundNumber(String history) {
		String[] splited = history.split(" ");
		if(splited.length == 3) {
			return 1;
		}else {
			return 0;
		}
	}
	
	//accessor
	public Map<String, Object> getParameters() {
		return this.parameters;
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
		System.out.println(((ArrayList<Double>)this.parameters.get("gameValues")).size());
	}

	public static void main(String[] args) {
		String strategiesFileName = "flopstrat_1_1_6_bet_pot_ai_raise_2x_ai";
		String parametersFileName = "flopparam_1_1_6_bet_pot_ai_raise_2x_ai";
		FlopHoldemStrategyReader reader = new FlopHoldemStrategyReader(strategiesFileName, parametersFileName);
		reader.printNodeLoop();
	}

}
