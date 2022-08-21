package com.libertrobin.logic.pokergame;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import com.libertrobin.logic.filemanager.FileManager;

/**
 * Abstract class with abstract methods useful to work with a general poker game.
 * A general poker game is mainly used by an instance of Trainer to solve a game.
 * @author Robin Libert
 *
 */
public abstract class PokerGame implements Serializable{

	private static final long serialVersionUID = 1L;
	private final int PASS = 0, CALL = 1, BET = 2, RAISE = 3, NUM_ACTIONS = 4;
	
	public abstract ArrayList<String> createListOfCards();
	
	/**
	 * Checks if the program goes to the next betting round or if the hand goes to showdown.
	 * @param actionsHistory The history of the previous actions without the bet/raise sizings.
	 * 		Example: rc AKQ brp
	 * @param roundNumber The current betting round.
	 * @return true if the players go to the next round or if the hands go to showdown.
	 */
	public abstract boolean checkNextRoundOrShowdown(String actionsHistory, int roundNumber);
	
	/**
	 * Checks if the program goes to a terminal state where the hands does not go to showdown.
	 * @param actionsHistory The history of the previous actions without the bet/raise sizings.
	 * @param roundNumber The current betting round.
	 * @return true If a player fold against a bet/raise
	 */
	public abstract boolean checkTerminalWithoutShowdown(String actionsHistory, int roundNumber);
	
	/**
	 * Get the valid actions a player can play in a particular game configuration.
	 * @param history The history of the previous actions with the bet/raise sizings.
	 * @param actionsHistory The history of the previous actions without the bet/raise sizings.
	 * @param remainingMoney The remaining stack of a player
	 * @param previousBet The previous bet/raise in the current betting round
	 * @param roundNumber The current betting round
	 * @param plays The number of play in the current betting round that lead to the current history
	 * @return A list of integers that represent an action which is allowed
	 */
	public abstract ArrayList<Integer> getValidActions(String history, String actionsHistory, int remainingMoney, int previousBet, int roundNumber, int plays);
	
	/**
	 * Get the sizes of the raises a player can use in a particular game configuration where the action raise is allowed.
	 * @param actionsHistory The history of the previous actions without the bet/raise sizings.
	 * @param previousBet The previous bet/raise in the current betting round
	 * @param remainingMoney The remaining stack of a player
	 * @param allowedRaiseSizings An array that contains the raise sizes allowed in a game abstraction
	 * @param sb The value of the small blind
	 * @param bb The value of the big blind
	 * @param roundNumber The current betting round
	 * @param plays The number of play in the current betting round that lead to the current history
	 * @return A list of integers that represent a raise size which is allowed
	 */
	public abstract ArrayList<Integer> getRaiseSizings(String actionsHistory, int previousBet, int remainingMoney, float[] allowedRaiseSizings, int sb, int bb, int roundNumber, int plays);
	
	/**
	 * Get the sizes of the bets a player can use in a particular game configuration where the action bet is allowed.
	 * @param potSize The size of the pot
	 * @param remainingMoney The remaining stack of a player
	 * @param allowedBetSizings An array that contains the bet sizes allowed in a game abstraction
	 * @return A list of integers that represent a bet size which is allowed
	 */
	public abstract ArrayList<Integer> getBetSizings(int potSize, int remainingMoney, float[] allowedBetSizings);
	
	/**
	 * Evaluate the strength of a hand of poker.
	 * @param hand a poker hand e.g. with Leduc poker "QJ", "KQ"
	 * 	e.g. with Flop Hold'em "AsTc8s8c3d"
	 * @return an integer that represents the strength of a poker hand (the lower the better)
	 */
	public abstract int evaluate(String hand);
	
	/**
	 * Get a hand in an appropriate format for the method evaluate.
	 * @param listOfCards the deck of card
	 * @param playerFirstCardIndexInList the index of the first private card of the player in the deck
	 * @return a hand in the format compatible with the method evaluate.
	 */
	public abstract String getHandInStringFormat(ArrayList<String> listOfCards, int playerFirstCardIndexInList);
	
	/**
	 * Get the number of private cards a player have in an instance of PokerGame.
	 * @return the number of hole cards per player in an instance of PokerGame.
	 */
	public abstract int getNumberOfPrivateCards();
	
	/**
	 * Get the flop in the format of an history in an instance of PokerGame. The cards are sorted in decreasing order.
	 * Useful to reduce the number of information sets.
	 * 	e.g for Flop Hold'em: "AJ2", "T54s"
	 * @param listOfCards the deck of cards.
	 * @return the flop in a format which is compatible with the format of an history in an instance of PokerGame.
	 */
	public abstract String getFormattedFlopFromDeck(ArrayList<String> listOfCards);
	
	/**
	 * Get the private cards of the player in the format of an history before the flop in an instance of PokerGame. 
	 * The cards are sorted in decreasing order. 
	 * 	e.g for Flop Hold'em: "AJo", "AJs"
	 * @param listOfCards the deck of cards.
	 * @param playerFirstCardIndexInList the index of the first private card of the player in the deck
	 * @return
	 */
	public abstract String getFormattedPreflopHoleCardsFromDeck(ArrayList<String> listOfCards, int playerFirstCardIndexInList);
	
	/**
	 * Get the private cards of the player in the format of an history after the flop in an instance of PokerGame. 
	 * The cards are sorted in decreasing order. 
	 * 	e.g for Flop Hold'em: "AJ", "AJs"
	 * @param listOfCards the deck of cards.
	 * @param playerFirstCardIndexInList the index of the first private card of the player in the deck
	 * @return
	 */
	public abstract String getFormattedPostflopHoleCardsFromDeck(ArrayList<String> listOfCards, int playerFirstCardIndexInList);
	
	
	/**
	 * Tells if the first player to act preflop is the also the first player to act postflop.
	 * In Flop Hold'em, the player at the position of the dealer act first preflop and second postflop.
	 * In Leduc, the first player preflop is also the first player postflop.
	 * @return true if the first player to act preflop is not the first player to act postflop
	 */
	public abstract boolean changeFirstPlayerToActPostflop();
	
	/**
	 * Initialize a bet. Useful in Flop hold'em because we considered that posting the blinds is a bet.
	 * In Leduc, we don't consider the antes as a bet.
	 * @return
	 */
	public abstract int initPreviousBet();
	
	/**
	 * Returns a boolean that tells if the blinds are equal or not.
	 * In fact the blinds are never equal, but the antes are and we consider the antes as blinds.
	 * In Leduc return false because players posts the same ante.
	 * In Flop Hold'em return true because the big blind is always bigger than the small blind.
	 * @return true if the blinds don't have the same values
	 */
	public abstract boolean blindsNotEqual();
	
	/**
	 * Each instance of a PokerGame should return its associated instance of a FileManager.
	 * A file manager is use to store or load a strategy and parameters of a game and a simulation.
	 * @return a file manager
	 */
	public abstract FileManager getFileManager();
	
	/**
	 * Get the bet sizes allowed in an abstraction of a poker game.
	 * The bet sizes are always a percentage relative to the size of the pot.
	 * @return An array of bet sizes
	 */
	public abstract float[] getBetSizingsRelativeToPotSize();
	
	/**
	 * Get the raise sizes allowed in an abstraction of a poker game.
	 * The raise sizes are always a percentage relative to the size of the previous bet.
	 * @return An array of raise sizes
	 */
	public abstract float[] getRaiseSizingsRelativeToPreviousBetOrRaise();
	
	/**
	 * @return the value of the big blind
	 */
	public abstract int getBigBlind();
	
	/**
	 * @return the value of the small blind
	 */
	public abstract int getSmallBlind();
	
	/**
	 * @return the starting stack of the players
	 */
	public abstract int getStartingMoney();
	
	/**
	 * Set the bet sizes allowed in an abstraction of a poker game.
	 * The bet sizes are always a percentage relative to the size of the pot.
	 * @param betSizings an array of bet size
	 */
	public abstract void setBetSizingsRelativeToPotSize(float[] betSizings);
	
	/**
	 * Set the raise sizes allowed in an abstraction of a poker game.
	 * The raise sizes are always a percentage relative to the size of the previous bet.
	 * @param raiseSizings an array of raise sizes
	 */
	public abstract void setRaiseSizingsRelativeToPreviousBetOrRaise(float[] raiseSizings);
	
	/**
	 * Set the value of the big blind in a poker game.
	 * In leduc poker, the value of the big blind need to be the same as the value of the small blind.
	 * @param bb
	 */
	public abstract void setBigBlind(int bb);
	
	/**
	 * Set the value of the small blind in a poker game.
	 * In leduc poker, the value of the small blind need to be the same as the value of the big blind.
	 * @param sb
	 */
	public abstract void setSmallBlind(int sb);
	
	/**
	 * Set the starting stack of the players.
	 * @param startingStack The starting stack of the players.
	 */
	public abstract void setStartingMoney(int startingStack);
	
	/**
	 * Shuffle a deck of cards.
	 * @param listOfCards deck of cards
	 * @return A shuffled deck of cards
	 */
	public ArrayList<String> shuffleListOfCards(ArrayList<String> listOfCards) {
		Collections.shuffle(listOfCards);
		return listOfCards;
		
	}
	
	
	/**
	 * Print a deck of cards.
	 * @param listOfCards deck of cards
	 */
	public void printListOfCards(ArrayList<String> listOfCards) {
		for (Iterator<String> iterator = listOfCards.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			System.out.println(string);
		}
	}
	
	
	/**
	 * Check whether or not an action is valid.
	 * 	e.g A player cannot bet after a raise, call after a check, bet more than its remaining stack...
	 * @param action : 0=PASS, 1=CALL, 2=BET, 3=RAISE
	 * @param remainingMoney The remaining stack of the player
	 * @param previousBet The previous bet in the current betting round
	 * @param actionsHistory history without the sizes of the actions
	 * @return true if action is valid in a particular state of the game
	 */
	public boolean isActionValid(int action, int remainingMoney, int previousBet, String actionsHistory) {
		if(actionsHistory.equals("") || actionsHistory.substring(actionsHistory.length() - 1, actionsHistory.length()).equals(" ")) {
			if(action == getRAISE() || action == getCALL()) {
				return false;
			}
		}else if(actionsHistory.substring(actionsHistory.length() - 1, actionsHistory.length()).equals("p")) {
			if(action == getCALL() || action == getRAISE()) {
				return false;
			}
			if(actionsHistory.length() >= 2 && actionsHistory.substring(actionsHistory.length() - 2, actionsHistory.length()).equals("pp"))
				return false;
		}else if(actionsHistory.substring(actionsHistory.length() - 1, actionsHistory.length()).equals("b") || actionsHistory.substring(actionsHistory.length() - 1, actionsHistory.length()).equals("r")) {
			if(action == getBET()) {
				return false;
			}
		}
		if((action == getBET() || action == getRAISE()) && remainingMoney <= previousBet)
			return false;
		return true;
	}
	
	// Accessors
	public int getPASS() {
		return PASS;
	}

	public int getCALL() {
		return CALL;
	}

	public int getBET() {
		return BET;
	}

	public int getRAISE() {
		return RAISE;
	}

	public int getNUM_ACTIONS() {
		return NUM_ACTIONS;
	}

}
