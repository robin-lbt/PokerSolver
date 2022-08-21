package com.libertrobin.logic.pokergame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.libertrobin.logic.filemanager.FileManager;
import com.libertrobin.logic.filemanager.NoLimitLeducFileManager;

/**
 * Class with methods useful to work with a No Limit Leduc poker game.
 * @author Robin Libert
 *
 */
public class NoLimitLeduc extends PokerGame{
	
	private static final long serialVersionUID = 1L;
	private Map<String, Integer> cardValueMapping = createLeducCardValueMapping();
	private float[] betSizings;
	private float[] raiseSizings;
	private int sb;
	private int bb;
	private int startingStack;
	
	public NoLimitLeduc() {
		this.setBetSizingsRelativeToPotSize(new float[] {1.0f});
		this.setRaiseSizingsRelativeToPreviousBetOrRaise(new float[] {2.0f});
		this.setSmallBlind(1);
		this.setBigBlind(1);
		this.setStartingMoney(6);
	}
	
	private Map<String, Integer> createLeducCardValueMapping() {
		Map<String, Integer> cards = new HashMap<>();
		cards.put("J", 3);
		cards.put("Q", 2);
		cards.put("K", 1);
		return cards;
	}

	@Override
	public ArrayList<String> createListOfCards() {
		ArrayList<String> cards = new ArrayList<String>();
		cards.add("J");
		cards.add("J");
		cards.add("Q");
		cards.add("Q");
		cards.add("K");
		cards.add("K");
		System.out.println("Leduc create card list method");
		return cards;
	}

	@Override
	public boolean checkNextRoundOrShowdown(String actionsHistory, int roundNumber) {
		return actionsHistory.substring(actionsHistory.length() - 2, actionsHistory.length()).equals("pp") || 
				actionsHistory.substring(actionsHistory.length() - 2, actionsHistory.length()).equals("bc") || 
				actionsHistory.substring(actionsHistory.length() - 2, actionsHistory.length()).equals("rc");
	}

	@Override
	public boolean checkTerminalWithoutShowdown(String actionsHistory, int roundNumber) {
		return actionsHistory.substring(actionsHistory.length() - 2, actionsHistory.length()).equals("bp") || 
				actionsHistory.substring(actionsHistory.length() - 2, actionsHistory.length()).equals("rp");
	}

	@Override
	public ArrayList<Integer> getValidActions(String history, String actionsHistory, int remainingMoney, int previousBet,
			int roundNumber, int plays) {
		ArrayList<Integer> validActions = new ArrayList<>();
		for (int a = 0; a < getNUM_ACTIONS(); a++) {
			if(isActionValid(a, remainingMoney, previousBet, actionsHistory)) {
				validActions.add(a);
			}
		}
		return validActions;
	}

	@Override
	public int evaluate(String hand) {
		if (hand.length() != 3) 
            throw new IllegalArgumentException("The hand string should have a length of 3. Example: \"Q J\"");
		if(!(hand.substring(0, 1).equals("K") || hand.substring(0, 1).equals("Q") || hand.substring(0, 1).equals("J")))
			throw new IllegalArgumentException("The first character of the string must be K, Q or J");
		if(!(hand.substring(2, 3).equals("K") || hand.substring(2, 3).equals("Q") || hand.substring(2, 3).equals("J")))
			throw new IllegalArgumentException("The last character of the string must be K, Q or J");
		if(!hand.substring(1, 2).equals(" "))
			throw new IllegalArgumentException("The first and last characters must be separate by a blank space");
		if(hand.substring(0, 1).equals(hand.substring(2, 3)))
			return 0;
		return cardValueMapping.get(hand.substring(0, 1));
	}
	
	@Override
	public String getHandInStringFormat(ArrayList<String> listOfCards, int playerFirstCardIndexInList) {
		return listOfCards.get(playerFirstCardIndexInList) + " " + listOfCards.get(2);
	}
	
	@Override
	public int getNumberOfPrivateCards() {
		return 1;
	}

	@Override
	public String getFormattedFlopFromDeck(ArrayList<String> listOfCards) {
		return listOfCards.get(2);
	}

	@Override
	public String getFormattedPreflopHoleCardsFromDeck(ArrayList<String> listOfCards, int playerFirstCardIndexInList) {
		return listOfCards.get(playerFirstCardIndexInList);
	}

	@Override
	public String getFormattedPostflopHoleCardsFromDeck(ArrayList<String> listOfCards, int playerFirstCardIndexInList) {
		return listOfCards.get(playerFirstCardIndexInList);
	}


	@Override
	public ArrayList<Integer> getRaiseSizings(String actionsHistory, int previousBet, int remainingMoney,
			float[] allowedRaiseSizings, int sb, int bb, int roundNumber, int plays) {
		ArrayList<Integer> raiseSizings = new ArrayList<>();
		if(remainingMoney > 0) {
			for(int i=0; i<allowedRaiseSizings.length;i++) { // check that the bet size is not bigger than the remaining stake
				int current = (int) (previousBet*allowedRaiseSizings[i]);
				if(current + previousBet < remainingMoney) {
					if(current+previousBet > 0)
						raiseSizings.add(current);
				}else if(remainingMoney-previousBet >= 0){
					raiseSizings.add(remainingMoney-previousBet);
					break; // break the loop because the betSizingTemp array is sorted in increasing order
				}
			}
		}
		return raiseSizings;
	}

	@Override
	public ArrayList<Integer> getBetSizings(int potSize, int remainingMoney, float[] allowedBetSizings) {
		ArrayList<Integer> betSizings = new ArrayList<>();
		if(remainingMoney > 0) {
			for(int i=0; i<allowedBetSizings.length;i++) { // check that the bet size is not bigger than the remaining stake
				int current = (int) (potSize*allowedBetSizings[i]);
				if(current < remainingMoney) {
					if(current > 0)
						betSizings.add(current);
				}else if(remainingMoney >= 0){
					betSizings.add(remainingMoney);
					break; // break the loop because the betSizingTemp array is sorted in increasing order
				}
			}
		}
		
		return betSizings;
	}

	@Override
	public float[] getBetSizingsRelativeToPotSize() {
		return this.betSizings;
	}

	@Override
	public float[] getRaiseSizingsRelativeToPreviousBetOrRaise() {
		return this.raiseSizings;
	}

	@Override
	public int getBigBlind() {
		return this.bb;
	}

	@Override
	public int getSmallBlind() {
		return this.sb;
	}

	@Override
	public int getStartingMoney() {
		return this.startingStack;
	}

	@Override
	public boolean changeFirstPlayerToActPostflop() {
		return false;
	}

	@Override
	public int initPreviousBet() {
		return 0;
	}

	@Override
	public boolean blindsNotEqual() {
		return false;
	}

	@Override
	public FileManager getFileManager() {
		return new NoLimitLeducFileManager();
	}

	@Override
	public void setBetSizingsRelativeToPotSize(float[] betSizings) {
		this.betSizings = betSizings;
		
	}

	@Override
	public void setRaiseSizingsRelativeToPreviousBetOrRaise(float[] raiseSizings) {
		this.raiseSizings = raiseSizings;
		
	}

	@Override
	public void setBigBlind(int bb) {
		this.bb = bb;
		
	}

	@Override
	public void setSmallBlind(int sb) {
		this.sb = sb;
	}

	@Override
	public void setStartingMoney(int startingStack) {
		this.startingStack = startingStack;
		
	}

}
