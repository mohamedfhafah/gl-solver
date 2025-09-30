package fr.univamu.solver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TestInterval {
	
	@Test
	void testEmpty() {
		var i = new Interval(20, 10);
		assertTrue(i.isEmpty());
	}

	@Test
	void testFixed() {
		var i = new Interval(20, 20);
		assertTrue(i.isOneValue());
	}

	@Test
	void testAdd() {
		var a = new Interval(20, 30);
		var b = new Interval(-33, -5);
		assertEquals("[-13,25]", a.add(b).toString());
	}

}
