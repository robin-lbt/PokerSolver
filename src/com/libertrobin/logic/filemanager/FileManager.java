package com.libertrobin.logic.filemanager;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import com.libertrobin.logic.trainer.Trainer;

/**
 * Abstract class which defines the abstract methods to save and load the files necessary for a simulation of a general poker game.
 * @author Robin Libert
 *
 */
public abstract class FileManager {
	
	/**
	 * Saves a poker strategy.
	 * @param nodeMap the strategy to save.
	 * 	A poker strategy calculated by an instance of the Trainer class.
	 */
	public abstract void saveStrategy(TreeMap<String, Trainer.Node> nodeMap);
	
	/**
	 * Loads a poker strategy. 
	 * @return a poker strategy calculated by an instance of the Trainer class.
	 */
	public abstract TreeMap<String, Trainer.Node> loadStrategy();
	
	/**
	 * Saves the parameters of a poker game and a simulation.
	 * @param util the output of the cfr method of an instance of Trainer.
	 * @param iterNumber the current number of iterations performed by an instance of Trainer.
	 * @param numberOfInformationSets the number of information sets in a strategy.
	 * @param gameValues list of game values associated to the first player of a simulation.
	 * @param startingMoney the starting stack of both player.
	 * @param betSizings array of the allowed bet sizes in an abstraction of a poker game.
	 * @param raiseSizings array of the allowed raise sizes in an abstraction of a poker game.
	 * @param sb the value of the small blind in a poker game.
	 * @param bb the value of the big blind in a poker game.
	 */
	public abstract void saveParameters(double util, int iterNumber, int numberOfInformationSets, ArrayList<Double> gameValues, int startingMoney, float[] betSizings, float[] raiseSizings, int sb, int bb);
	
	/**
	 * Loads the parameters of a poker game and a simulation.
	 * @return a Map with the parameters of a poker game and a simulation.
	 */
	public abstract Map<String, Object> loadParameters();
	
	// Accessors
	public abstract String getStrategyFileName();
	public abstract String getParametersFileName();
	public abstract void setStrategyFileName(String strategiesFileName);
	public abstract void setParametersFileName(String parametersFileName);
}
