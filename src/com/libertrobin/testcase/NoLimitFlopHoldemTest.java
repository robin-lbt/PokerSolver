package com.libertrobin.testcase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import com.libertrobin.logic.pokergame.NoLimitFlopHoldem;
import com.libertrobin.logic.pokergame.PokerGame;

public class NoLimitFlopHoldemTest {
	private PokerGame game = new NoLimitFlopHoldem();
	
	@Test
	public void checkNextRoundOrShowdownTest() {
		assertFalse(game.checkNextRoundOrShowdown("p", 0));
		assertFalse(game.checkNextRoundOrShowdown("pp", 0));
		assertFalse(game.checkNextRoundOrShowdown("c", 0));
		assertTrue(game.checkNextRoundOrShowdown("cp", 0));
		assertFalse(game.checkNextRoundOrShowdown("cr", 0));
		assertFalse(game.checkNextRoundOrShowdown("crp", 0));
		assertTrue(game.checkNextRoundOrShowdown("crc", 0));
		assertFalse(game.checkNextRoundOrShowdown("r", 0));
		assertFalse(game.checkNextRoundOrShowdown("rp", 0));
		assertTrue(game.checkNextRoundOrShowdown("rc", 0));
		assertFalse(game.checkNextRoundOrShowdown("crr", 0));
		assertFalse(game.checkNextRoundOrShowdown("crrp", 0));
		assertTrue(game.checkNextRoundOrShowdown("crrc", 0));
		assertFalse(game.checkNextRoundOrShowdown("cp AKQ p", 1));
		assertTrue(game.checkNextRoundOrShowdown("cp AKQ pp", 1));
		assertFalse(game.checkNextRoundOrShowdown("cp AKQ b", 1));
		assertFalse(game.checkNextRoundOrShowdown("cp AKQ bp", 1));
		assertTrue(game.checkNextRoundOrShowdown("cp AKQ bc", 1));
		assertFalse(game.checkNextRoundOrShowdown("cp AKQ brr", 1));
		assertFalse(game.checkNextRoundOrShowdown("cp AKQ brrp", 1));
		assertTrue(game.checkNextRoundOrShowdown("cp AKQ brrc", 1));
	}
	
	@Test
	public void checkTerminalWithoutShowdownTest() {
		assertFalse(game.checkTerminalWithoutShowdown("p", 0));
		assertTrue(game.checkTerminalWithoutShowdown("pp", 0));
		assertFalse(game.checkTerminalWithoutShowdown("c", 0));
		assertFalse(game.checkTerminalWithoutShowdown("cp", 0));
		assertFalse(game.checkTerminalWithoutShowdown("cr", 0));
		assertTrue(game.checkTerminalWithoutShowdown("crp", 0));
		assertFalse(game.checkTerminalWithoutShowdown("crc", 0));
		assertFalse(game.checkTerminalWithoutShowdown("r", 0));
		assertTrue(game.checkTerminalWithoutShowdown("rp", 0));
		assertFalse(game.checkTerminalWithoutShowdown("rc", 0));
		assertFalse(game.checkTerminalWithoutShowdown("crr", 0));
		assertTrue(game.checkTerminalWithoutShowdown("crrp", 0));
		assertFalse(game.checkTerminalWithoutShowdown("crrc", 0));
		assertFalse(game.checkTerminalWithoutShowdown("cp AKQ p", 1));
		assertFalse(game.checkTerminalWithoutShowdown("cp AKQ pp", 1));
		assertFalse(game.checkTerminalWithoutShowdown("cp AKQ b", 1));
		assertTrue(game.checkTerminalWithoutShowdown("cp AKQ bp", 1));
		assertFalse(game.checkTerminalWithoutShowdown("cp AKQ bc", 1));
		assertFalse(game.checkTerminalWithoutShowdown("cp AKQ brr", 1));
		assertTrue(game.checkTerminalWithoutShowdown("cp AKQ brrp", 1));
		assertFalse(game.checkTerminalWithoutShowdown("cp AKQ brrc", 1));
	}
	
	@Test
	public void getValidActionsTest() {
		ArrayList<Integer> l = game.getValidActions("", "", 6, 2, 0, 0);
		assertEquals(3, l.size());
		assertEquals(0, (int)l.get(0));
		assertEquals(1, (int)l.get(1));
		assertEquals(3, (int)l.get(2));
		
		l = game.getValidActions("c1", "c", 6, 2, 0, 1);
		assertEquals(2, l.size());
		assertEquals(0, (int)l.get(0));
		assertEquals(3, (int)l.get(1));
		
		l = game.getValidActions("r4", "r", 4, 4, 0, 1);
		assertEquals(2, l.size());
		assertEquals(0, (int)l.get(0));
		assertEquals(1, (int)l.get(1));
		
		l = game.getValidActions("r4", "r", 6, 4, 0, 1);
		assertEquals(3, l.size());
		assertEquals(0, (int)l.get(0));
		assertEquals(1, (int)l.get(1));
		assertEquals(3, (int)l.get(2));
	}
	
}
