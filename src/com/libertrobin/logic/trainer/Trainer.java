package com.libertrobin.logic.trainer;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import com.libertrobin.logic.filemanager.FileManager;
import com.libertrobin.logic.pokergame.NoLimitFlopHoldem;
import com.libertrobin.logic.pokergame.NoLimitLeduc;
import com.libertrobin.logic.pokergame.PokerGame;

/**
 * This class is our poker solver.
 * @author Robin Libert
 *
 */
public class Trainer implements Serializable{
	private static final long serialVersionUID = 1L;
	private PokerGame game;
	// nodeMap is the data structure where we store a poker strategy
	private TreeMap<String, Node> nodeMap = new TreeMap<String, Node>();
	private String strategiesFileName;
	private String parametersFileName;
	
	/**
	 * Constructor that initializes a PokerGame and the files name.
	 * @param game A variant of poker
	 * @param strategiesFileName path to store or load a strategy
	 * @param parametersFileName path to store or load the parameters of a poker game and the simulation
	 */
	public Trainer(PokerGame game, String strategiesFileName, String parametersFileName) {
		this.game = game;
		this.strategiesFileName = strategiesFileName;
		this.parametersFileName = parametersFileName;
	}
	
	/**
	 * The inner class Node comes from the tutorial of Todd W Neller and Marc Lanctot
	 * “An introduction to counterfactual regret minimization”. In: Proceedings of model AI assignments, the fourth
	 * symposium on educational advances in artificial intelligence (EAAI-2013). Vol. 11. 2013.
	 *
	 * We only added the numberOfActions parameter when creating a Node. A node represent an information set
	 * and is stored in nodeMap. A Node also have useful methods for the training process and to compute
	 * an optimal strategy.
	 */
	public class Node implements Serializable{
		private static final long serialVersionUID = 1L;
		int numberOfActions;//the number of possible actions in this information set
		String infoSet;//an information set name
		double[] regretSum;//cumulative regrets of this information set
		double[] strategy;//strategy at time t of this information set
		double[] strategySum;//cumulative strategy of this information set


		public Node(int numberOfActions) {
			this.numberOfActions = numberOfActions;	
			this.regretSum = new double[numberOfActions];
			this.strategy = new double[numberOfActions];
			this.strategySum = new double[numberOfActions];
		}
		
		/**
		 * Compute and return the strategy of the information set at time t.
		 * Also update the cumulative strategy.
		 * @param realizationWeight
		 * @return the strategy of the information set
		 */
		private double[] getStrategy(double realizationWeight) {
			double normalizingSum = 0;
			for (int a = 0; a < numberOfActions; a++) {
				strategy[a] = regretSum[a] > 0 ? regretSum[a] : 0;
				normalizingSum += strategy[a];
			}
			for (int a = 0; a < numberOfActions; a++) {
				if (normalizingSum > 0)
					strategy[a] /= normalizingSum;
				else
					strategy[a] = 1.0 / numberOfActions;
				strategySum[a] += realizationWeight * strategy[a];
			}
			return strategy;
		}
		
		/**
		 * Compute the average strategy of this information set.
		 * Important because with CFR, only the average strategy converges towards a Nash equilibrium.
		 * @return the average strategy of this information set
		 */
		public double[] getAverageStrategy() {
			double[] avgStrategy = new double[numberOfActions];
			double normalizingSum = 0;
			for (int a = 0; a < numberOfActions; a++)
				normalizingSum += strategySum[a];
			for (int a = 0; a < numberOfActions; a++)
				if (normalizingSum > 0)
					avgStrategy[a] = strategySum[a] / normalizingSum;
				else
					avgStrategy[a] = 1.0 / numberOfActions;
			return avgStrategy;
		}
		
		public String toString() {
			double[] a = getAverageStrategy();
			return String.format("%4s: %s", infoSet, Arrays.toString(a));
		}

	}
	
	/**
	 * Utility method.
	 * @param potSize The size of the pot
	 * @param p0Invested The amount of money player 0 invested
	 * @param p1Invested The amount of money player 1 invested
	 * @param player The current player
	 * @param value The value of a bet, raise or call
	 * @return an array with the updated value of the pot and the new amount of money invested by the players
	 */
	private int[] updateMoneyInvestedByPlayerAndPotSizeUtil(int potSize, int p0Invested, int p1Invested, int player, int value) {
		potSize += value;
		if(player == 0) {
			p0Invested += value;
		}else {
			p1Invested += value;
		}
	
		return new int[]{potSize, p0Invested, p1Invested};
	}

	/**
	 * The train method of our solver.
	 * @param iterations Number of iterations of the training
	 * @param save True if we want to save the strategy
	 * @param load True if we want to load an existing strategy
	 */
	@SuppressWarnings("unchecked")
	public void train(int iterations, boolean save, boolean load) {
		ArrayList<String> listOfCards = this.game.createListOfCards();
		long start = 0;
		long end = 0;
		long time = 0;
		double util = 0;
		int iterNumber = 0;
		ArrayList<Double> gameValues = new ArrayList<>();
		// Load a strategy
		if(load) {
			System.out.println("Load Strategy...");
			FileManager fileManager = game.getFileManager();
			fileManager.setStrategyFileName(this.strategiesFileName);
			fileManager.setParametersFileName(this.parametersFileName);
			Map<String, Object> parameters = fileManager.loadParameters();
			util = (double)parameters.get("util");
			iterNumber = (int)parameters.get("iterNumber");
			gameValues = (ArrayList<Double>)parameters.get("gameValues");
			nodeMap = fileManager.loadStrategy();
			System.out.println("Strategy loaded.");
		}
		//Main loop for the training
		for (int i = 0; i < iterations; i++) {
			start = System.nanoTime();//To compute the time of an iteration
			//call of cfr and update the cumulative util of the game
			util += cfr(this.game.shuffleListOfCards(listOfCards), "", "", 1, 1, this.game.getSmallBlind()+this.game.getBigBlind(), this.game.getSmallBlind(), this.game.getBigBlind(), 0, 0, game.initPreviousBet());
			//Nodelock used in the thesis
			//util += cfr(this.game.shuffleListOfCards(listOfCards), "pp", "pp", 1, 1, this.game.getSmallBlind()+this.game.getBigBlind(), this.game.getSmallBlind(), this.game.getBigBlind(), 0, 2, game.initPreviousBet());
			end =  System.nanoTime() - start;
			time += end;
			//Like a loading bar
			if(i % 10000 == 0 && i>0) {
				System.out.println(iterNumber + " : Average game value: " + util / iterNumber);
				gameValues.add(util / iterNumber);
			}
			iterNumber++;
		}
		// At the end of the training, we display information about the simulation
		int numberOfInformationSets = 0;
		for (Node n : nodeMap.values()) {
			System.out.println(n);
			numberOfInformationSets ++;
		}
		System.out.println("Average game value: " + util / iterations);
		System.out.println("Number of information sets: " + numberOfInformationSets);
		System.out.println("Each iteration took an average of "+ ((double)time/iterations)/1000000 + " ms");
		System.out.println("You can do about " +1000/(((double)time/iterations)/1000000) +" iterations/seconds");
		// To save a strategy
		if(save) {
			System.out.println("Save strategy...");
			FileManager fileManager = game.getFileManager();
			fileManager.setStrategyFileName(this.strategiesFileName);
			fileManager.setParametersFileName(this.parametersFileName);
			fileManager.saveStrategy(nodeMap);
			fileManager.saveParameters(util, iterNumber, numberOfInformationSets, gameValues, game.getStartingMoney(), game.getBetSizingsRelativeToPotSize(), game.getRaiseSizingsRelativeToPreviousBetOrRaise(), game.getSmallBlind(), game.getBigBlind());
			System.out.println("Strategy saved");
		}
	}
	
	/**
	 * Implementation of the algorithm CFR. Compute an approximation of a Nash Equilibrium.
	 * @param cards The deck of cards
	 * @param history the history in the current node. Initially empty unless you want to force actions.
	 * @param actionsHistory the history without the sizes of the actions. (e.g. history="b2c2" -> actionsHistory="bc")
	 * @param p0 Reaching probability of the first player. Initially 1.
	 * @param p1 Reaching probability of the second player. Initially 1.
	 * @param potSize The size of the pot.
	 * @param p0Invested The amount of money invested by the first player.
	 * @param p1Invested The amount of money invested by the second player.
	 * @param roundNumber The number of the current betting round. Initially 0.
	 * @param plays The number of the plays. Initially 0 unless your initial history is not empty.
	 * @param previousBet The previous bet in the current betting round.
	 * @return The utility of the game for the first player.
	 */
	private double cfr(ArrayList<String> cards, String history, String actionsHistory, double p0, double p1, int potSize, int p0Invested, int p1Invested, int roundNumber, int plays, int previousBet) {
		int potSizeNext = potSize;
		int p0InvestedNext = p0Invested;
		int p1InvestedNext = p1Invested;
		int roundNumberNext = roundNumber;
		int player = plays % 2;
		// In leduc poker, the first player to act preflop is also the first player to act postflop
		// It is not the case in flop holdem where the small blind acts first preflop and last postflop
		if(game.changeFirstPlayerToActPostflop())
			player = roundNumber == 0 ? plays % 2 : (plays+1) % 2;
		int opponent = 1 - player;
		int previousBetNext = previousBet;
		int playerFirstCardIndex = player == 0 ? 0 : game.getNumberOfPrivateCards();
		int opponentFirstCardIndex = opponent == 0 ? 0 : game.getNumberOfPrivateCards();
		// Terminal state or next betting round
		if (plays > 1) {
			// Special case of terminal state where the blinds are different
			if(game.blindsNotEqual() && history.length() == 2 && history.substring(history.length() - 2, history.length()).equals("pp")) {
				return -game.getSmallBlind();
			}
			int winAmount = player == 0 ? potSize - p0Invested : potSize - p1Invested;
			boolean nextRoundOrShowdownPass = game.checkNextRoundOrShowdown(actionsHistory, roundNumber);
			boolean terminalWithoutShowdownPass = game.checkTerminalWithoutShowdown(actionsHistory, roundNumber);
			// Terminal state without showdown (e.g. player 0 bet and player 1 pass)
			if (terminalWithoutShowdownPass) {
				return winAmount;
			}else if (nextRoundOrShowdownPass) {
				if(roundNumber == 1) { // Terminal state with showdown
					// Hand evaluation and reward
					int playerScore = game.evaluate(game.getHandInStringFormat(cards, playerFirstCardIndex));
					int opponentScore = game.evaluate(game.getHandInStringFormat(cards, opponentFirstCardIndex));
					if(playerScore == opponentScore) {
						return 0;
					}
					return playerScore < opponentScore ? winAmount : -winAmount;
				}
				// Initialize the next betting round
				roundNumber = 1;
				roundNumberNext = 1;
				previousBet = 0;
				previousBetNext = 0;
				plays = 0;
				player = game.changeFirstPlayerToActPostflop() ? 1 : 0;
				opponent = 1-player;
				history = history + " " + game.getFormattedFlopFromDeck(cards) + " ";
				actionsHistory = actionsHistory + " " + game.getFormattedFlopFromDeck(cards) + " ";
			}
		}
		int remainingMoney = player == 0 ? game.getStartingMoney() - p0Invested : game.getStartingMoney() - p1Invested;
		playerFirstCardIndex = player == 0 ? 0 : game.getNumberOfPrivateCards();
		opponentFirstCardIndex = opponent == 0 ? 0 : game.getNumberOfPrivateCards();
		String infoSet = (roundNumber == 0 ? game.getFormattedPreflopHoleCardsFromDeck(cards, playerFirstCardIndex) 
										   : game.getFormattedPostflopHoleCardsFromDeck(cards, playerFirstCardIndex))
										   + " " + history;
		ArrayList<Integer> validActions = game.getValidActions(history, actionsHistory, remainingMoney, previousBet, roundNumber, plays);
		ArrayList<Integer> raiseSizings = game.getRaiseSizings(actionsHistory, previousBetNext, remainingMoney, game.getRaiseSizingsRelativeToPreviousBetOrRaise(), game.getSmallBlind(), game.getBigBlind(), roundNumber, plays);
		ArrayList<Integer> betSizings = game.getBetSizings(potSize, remainingMoney, game.getBetSizingsRelativeToPotSize());
		
		int numberOfBets = 0;
		int numberOfRaises = 0;
		for (int a = 0; a < validActions.size(); a++) {
			if (validActions.get(a) == game.getBET()) {
				numberOfBets = betSizings.size()-1;
			}else if(validActions.get(a) == game.getRAISE()) {
				numberOfRaises = raiseSizings.size()-1;
			}
		}
		int numberOfActions = validActions.size();
		// Load or create the node corresponding to the current information set
		Node node = nodeMap.get(infoSet);
		if (node == null) {
			node = new Node(numberOfActions+numberOfBets+numberOfRaises);
			node.infoSet = infoSet;
			nodeMap.put(infoSet, node);
		}
		double[] strategy = node.getStrategy(player == 0 ? p0 : p1);//Load the strategy of this information set at time t-1
		//Initialize the utility of each action of this information set at time t
		double[] util = new double[numberOfActions+numberOfBets+numberOfRaises];
		//Initialize the total utility of this information set at time t
		double nodeUtil = 0;
		// Main recursion loop to traverse the game tree
		for (int a = 0; a < numberOfActions; a++) {
			String nextHistory = null;
			String nextActionsHistory = null;
			if(validActions.get(a) == game.getBET()) {//BET action
				for(int b = 0; b<betSizings.size();b++) {//Multiple bet sizes
					previousBetNext = betSizings.get(b);
					nextHistory = history + "b"+previousBetNext;
					nextActionsHistory = actionsHistory + "b";
					int[] updatedValues = updateMoneyInvestedByPlayerAndPotSizeUtil(potSize, p0Invested, p1Invested, player, previousBetNext);
					potSizeNext = updatedValues[0];
					p0InvestedNext = updatedValues[1];
					p1InvestedNext = updatedValues[2];
					//Go to the child node and update the utility of this action
					util[a+b] = player == 0
							?  -cfr(cards, nextHistory, nextActionsHistory, p0 * strategy[a+b], p1, potSizeNext, p0InvestedNext, p1InvestedNext, roundNumberNext, plays+1, previousBetNext)
							:  -cfr(cards, nextHistory, nextActionsHistory, p0, p1 * strategy[a+b], potSizeNext, p0InvestedNext, p1InvestedNext, roundNumberNext, plays+1, previousBetNext);
					nodeUtil += strategy[a+b] * util[a+b];
				}
			}else if(validActions.get(a) == game.getCALL()) {//CALL action
				if(plays == 0 && roundNumber == 0 && player == 0) { // SB call BB special case
					nextHistory = history + "c"+ (game.getBigBlind() - game.getSmallBlind());//SB ante = 1 and BB ante = 2 thus SB call 1
					nextActionsHistory = actionsHistory + "c";
					int[] updatedValues = updateMoneyInvestedByPlayerAndPotSizeUtil(potSize, p0Invested, p1Invested, player, game.getBigBlind() - game.getSmallBlind());
					potSizeNext = updatedValues[0];
					p0InvestedNext = updatedValues[1];
					p1InvestedNext = updatedValues[2];
					previousBetNext = game.getBigBlind();// BB ante
				}else {
					nextHistory = history + "c"+previousBet;
					nextActionsHistory = actionsHistory + "c";
					int[] updatedValues = updateMoneyInvestedByPlayerAndPotSizeUtil(potSize, p0Invested, p1Invested, player, previousBet);
					potSizeNext = updatedValues[0];
					p0InvestedNext = updatedValues[1];
					p1InvestedNext = updatedValues[2];
					previousBetNext = 0;
				}
				//Need to check if the last player who call on this betting round is the first player to act on the next betting round
				if(game.changeFirstPlayerToActPostflop() && plays == 1 && game.checkNextRoundOrShowdown(nextActionsHistory, roundNumber) && roundNumber == 0 && player == 1) {
					util[a] = cfr(cards, nextHistory, nextActionsHistory, p0, p1 * strategy[a], potSizeNext, p0InvestedNext, p1InvestedNext, roundNumberNext, plays+1, previousBetNext);
				}else if(!game.changeFirstPlayerToActPostflop() && plays > 1 && game.checkNextRoundOrShowdown(nextActionsHistory, roundNumber) && roundNumber == 0 && player == 0){
					util[a] = cfr(cards, nextHistory, nextActionsHistory, p0 * strategy[a], p1, potSizeNext, p0InvestedNext, p1InvestedNext, roundNumberNext, plays+1, previousBetNext);
				}else {
					util[a] = player == 0
							?  -cfr(cards, nextHistory, nextActionsHistory, p0 * strategy[a], p1, potSizeNext, p0InvestedNext, p1InvestedNext, roundNumberNext, plays+1, previousBetNext)
							:  -cfr(cards, nextHistory, nextActionsHistory, p0, p1 * strategy[a], potSizeNext, p0InvestedNext, p1InvestedNext, roundNumberNext, plays+1, previousBetNext);
				}
				nodeUtil += strategy[a] * util[a];
			}else if (validActions.get(a) == game.getRAISE()) {//Raise action
				for(int r = 0; r<raiseSizings.size();r++) {//Multiple raise sizes
					previousBetNext = raiseSizings.get(r);
					nextHistory = history + "r"+previousBetNext;
					nextActionsHistory = actionsHistory + "r";
					int[] updatedValues = updateMoneyInvestedByPlayerAndPotSizeUtil(potSize, p0Invested, p1Invested, player, previousBetNext+previousBet);
					if(plays == 1 && roundNumber == 0 && player == 1 && actionsHistory.substring(0, 1).equals("c"))
						updatedValues = updateMoneyInvestedByPlayerAndPotSizeUtil(potSize, p0Invested, p1Invested, player, previousBetNext);
					potSizeNext = updatedValues[0];
					p0InvestedNext = updatedValues[1];
					p1InvestedNext = updatedValues[2];
					if(plays == 0 && roundNumber == 0 && player == 0) {
						potSizeNext -= game.getSmallBlind();
						p0InvestedNext -= game.getSmallBlind();
					}
					util[a+r] = player == 0
							?  -cfr(cards, nextHistory, nextActionsHistory, p0 * strategy[a+r], p1, potSizeNext, p0InvestedNext, p1InvestedNext, roundNumberNext, plays+1, previousBetNext)
							:  -cfr(cards, nextHistory, nextActionsHistory, p0, p1 * strategy[a+r], potSizeNext, p0InvestedNext, p1InvestedNext, roundNumberNext, plays+1, previousBetNext);
					nodeUtil += strategy[a+r] * util[a+r];
				}
			}else { // PASS action
				previousBetNext = 0;
				nextHistory = history + "p";
				nextActionsHistory = actionsHistory + "p";
				//Need to check if the last player who call on this betting round is the first player to act on the next betting round
				if(game.changeFirstPlayerToActPostflop() && plays == 1 && game.checkNextRoundOrShowdown(nextActionsHistory, roundNumber) && roundNumber == 0 && player == 1) {
					util[a] = cfr(cards, nextHistory, nextActionsHistory, p0, p1 * strategy[a], potSizeNext, p0InvestedNext, p1InvestedNext, roundNumberNext, plays+1, previousBetNext);
				}else {
					util[a] = player == 0
							? -cfr(cards, nextHistory, nextActionsHistory, p0 * strategy[a], p1, potSizeNext, p0InvestedNext, p1InvestedNext, roundNumberNext, plays+1, previousBetNext)
							: -cfr(cards, nextHistory, nextActionsHistory, p0, p1 * strategy[a], potSizeNext, p0InvestedNext, p1InvestedNext, roundNumberNext, plays+1, previousBetNext);
				}
				nodeUtil += strategy[a] * util[a];
			}
		}
		//Update the cumulative regrets of this information set
		for (int a = 0; a < numberOfActions+numberOfBets+numberOfRaises; a++) {
			double regret = util[a] - nodeUtil;
			node.regretSum[a] += (player == 0 ? p1 : p0) * regret;
		}
		//Return the utility of the current node to the parent node
		return nodeUtil;
	}

	public static void main(String[] args) {
		// Simulation parameters
		boolean save = true; // If you want to save your simulation
		boolean load = false; // If you want to load and train more a previous simulation
		// You must define the paths of your simulation if save or load is true
		String strategiesFileName = "leducstrat_1_1_10_bet_pot_ai_raise_2x_ai";
		String parametersFileName = "leducparam_1_1_10_bet_pot_ai_raise_2x_ai";
		// The number of iterations. (The bigger the better for the accuracy of a strategy)
		int iterations = 1000000;
		// Game parameters
		//bet sizes allowed. A bet size is in relation with the size of the pot.
		// e.g. (0.5f means 50% of the pot, 100.0f means 100 times the size of the pot) 
		float[] betSizings = new float[]{1.0f, 100.0f};
		//raise sizes allowed. A raise size is in relation with the size of the previous bet.
		float[] raiseSizings = new float[]{2.0f, 100.0f};
		// For Leduc poker, the value of sb should be the same as the value of bb.
		int sb = 1;
		int bb = 1;
		// The starting stack of the players
		int startingStack = 10;
		
		
		// Create your game object according to the poker variant you want to solve
		PokerGame game = new NoLimitLeduc();
		//PokerGame game = new NoLimitFlopHoldem();
		
		// Don't Touch
		game.setBetSizingsRelativeToPotSize(betSizings);
		game.setRaiseSizingsRelativeToPreviousBetOrRaise(raiseSizings);
		game.setSmallBlind(sb);
		game.setBigBlind(bb);
		game.setStartingMoney(startingStack);
		
		Trainer trainer = new Trainer(game, strategiesFileName, parametersFileName);
		trainer.train(iterations, save, load);// Begin the training
	}
}
