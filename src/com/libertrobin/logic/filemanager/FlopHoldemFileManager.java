package com.libertrobin.logic.filemanager;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.libertrobin.logic.trainer.Trainer;
import com.libertrobin.logic.trainer.Trainer.Node;

/**
 * Class which is used to save and load the files necessary for a simulation of Flop Hold'em.
 * @author Robin Libert
 *
 */
public class FlopHoldemFileManager extends FileManager{
	
	private String strategiesFileName;
	private String parametersFileName;
	
	public FlopHoldemFileManager() {
		this.setStrategyFileName("flop_holdem_strategies.dat");
		this.setStrategyFileName("flop_holdem_parameters.dat");
	}

	@Override
	public void saveStrategy(TreeMap<String, Trainer.Node> nodeMap) {
		try {
			ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(getStrategyFileName()));
			output.writeObject(nodeMap);
			output.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public TreeMap<String, Trainer.Node> loadStrategy() {
		TreeMap<String, Trainer.Node> gameTree = null;
		try {
			ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(getStrategyFileName())));
			gameTree = (TreeMap<String, Node>) input.readObject();
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return gameTree;
	}

	@Override
	public void saveParameters(double util, int iterNumber, int numberOfInformationSets, ArrayList<Double> gameValues,
			int startingMoney, float[] betSizings, float[] raiseSizings, int sb, int bb) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("util", util);
		parameters.put("iterNumber", iterNumber);
		parameters.put("numberOfInformationSets", numberOfInformationSets);
		parameters.put("gameValues", gameValues);
		parameters.put("startingMoney", startingMoney);
		parameters.put("betSizings", betSizings);
		parameters.put("raiseSizings", raiseSizings);
		parameters.put("sb", sb);
		parameters.put("bb", bb);
		try {
			ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(getParametersFileName()));
			output.writeObject(parameters);
			output.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public Map<String, Object> loadParameters() {
		Map<String, Object> parameters = null;
		try {
			ObjectInputStream input = new ObjectInputStream(new FileInputStream(getParametersFileName()));
			parameters = (Map<String, Object>) input.readObject();
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return parameters;
	}

	@Override
	public String getStrategyFileName() {
		return this.strategiesFileName;
	}

	@Override
	public String getParametersFileName() {
		return this.parametersFileName;
	}

	@Override
	public void setStrategyFileName(String strategiesFileName) {
		this.strategiesFileName = strategiesFileName;
		
	}

	@Override
	public void setParametersFileName(String parametersFileName) {
		this.parametersFileName = parametersFileName;
		
	}

}
