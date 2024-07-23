/***************************************************************************************************
 *
 * Copyright (c) 2019 - 2024 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2019 - 2024 Open Universiteit - www.ou.nl
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package org.testar.iv4xr;

import java.util.HashSet;
import java.util.Set;

public class LabRecruitsCoverage {

	private int [][] total_observed_positions = {{0,0,0}};
	private int [][] total_walked_positions = {{0,0,0}};

	private Set <String> total_entity_observed = new HashSet<>();
	private Set <String> total_entity_interacted = new HashSet<>();

	public LabRecruitsCoverage(int width, int height) {
		total_observed_positions = new int[ width ][ height ];
		total_walked_positions = new int[ width ][ height ];

		total_entity_observed = new HashSet<>();
		total_entity_interacted = new HashSet<>();
	}

	public void addObservedPosition(int x, int y) {
		total_observed_positions[x][y] = 1;
	}

	public void addWalkedPosition(int x, int y) {
		total_walked_positions[x][y] = 1;
	}

	public void addObservedEntity(String entityId) {
		total_entity_observed.add(entityId);
	}

	public void addInteractedEntity(String entityId) {
		total_entity_interacted.add(entityId);
	}

	public int getNumberObservedPositions() {
		int count = 0;
		for (int i = 0; i < total_observed_positions.length; i++) {
			for (int j = 0; j < total_observed_positions[i].length; j++) {
				if (total_observed_positions[i][j] == 1) {
					count++;
				}
			}
		}
		return count;
	}

	public int getNumberWalkedPositions() {
		int count = 0;
		for (int i = 0; i < total_walked_positions.length; i++) {
			for (int j = 0; j < total_walked_positions[i].length; j++) {
				if (total_walked_positions[i][j] == 1) {
					count++;
				}
			}
		}
		return count;
	}

	public int getNumberObservedEntities() {
		return total_entity_observed.size();
	}

	public int getNumberInteractedEntities() {
		return total_entity_interacted.size();
	}
}
