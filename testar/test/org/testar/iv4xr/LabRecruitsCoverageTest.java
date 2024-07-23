package org.testar.iv4xr;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class LabRecruitsCoverageTest {
	private LabRecruitsCoverage labRecruitsCoverage;

	@Before
	public void setUp() {
		labRecruitsCoverage = new LabRecruitsCoverage(10, 10); // Example size, can be adjusted as needed
	}

	@Test
	public void testAddObservedPosition() {
		labRecruitsCoverage.addObservedPosition(1, 2);
		assertEquals(1, labRecruitsCoverage.getNumberObservedPositions());
		labRecruitsCoverage.addObservedPosition(1, 3);
		labRecruitsCoverage.addObservedPosition(1, 4);
		labRecruitsCoverage.addObservedPosition(1, 3); // Duplicated
		assertEquals(3, labRecruitsCoverage.getNumberObservedPositions());
	}

	@Test
	public void testAddWalkedPosition() {
		labRecruitsCoverage.addWalkedPosition(2, 5);
		assertEquals(1, labRecruitsCoverage.getNumberWalkedPositions());
		labRecruitsCoverage.addWalkedPosition(2, 4);
		labRecruitsCoverage.addWalkedPosition(2, 4); // Duplicated
		labRecruitsCoverage.addWalkedPosition(2, 4); // Duplicated
		assertEquals(2, labRecruitsCoverage.getNumberWalkedPositions());
	}

	@Test
	public void testAddObservedEntity() {
		labRecruitsCoverage.addObservedEntity("entity1");
		assertEquals(1, labRecruitsCoverage.getNumberObservedEntities());
		labRecruitsCoverage.addObservedEntity("entity3");
		labRecruitsCoverage.addObservedEntity("entity3"); // Duplicated
		assertEquals(2, labRecruitsCoverage.getNumberObservedEntities());
	}

	@Test
	public void testAddInteractedEntity() {
		labRecruitsCoverage.addInteractedEntity("entity2");
		assertEquals(1, labRecruitsCoverage.getNumberInteractedEntities());
		labRecruitsCoverage.addInteractedEntity("entity3");
		labRecruitsCoverage.addInteractedEntity("entity4");
		labRecruitsCoverage.addInteractedEntity("entity2"); // Duplicated
		assertEquals(3, labRecruitsCoverage.getNumberInteractedEntities());
	}

}
