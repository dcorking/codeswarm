package codeswarm.processing;

/*
Copyright 2008-2009 code_swarm project team

This file is part of code_swarm.

code_swarm is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

code_swarm is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with code_swarm.  If not, see <http://www.gnu.org/licenses/>.
 */

import codeswarm.CodeSwarmConfig;
import codeswarm.code_swarm;

/**
 * An Edge link two nodes together : a File to a Person.
 */
public class Edge extends Drawable {
	private FileNode nodeFrom;
	private PersonNode nodeTo;
	private float len;
	private static int EDGE_LIFE_INIT = 255;
	private static int EDGE_LIFE_DECREMENT = -1;
	private static int EDGE_LEN = 25;

	static{
		CodeSwarmConfig cfg = code_swarm.getConfig();

		EDGE_LIFE_INIT = cfg.getIntProperty(CodeSwarmConfig.EDGE_LIFE_KEY,255);
		if (EDGE_LIFE_INIT <= 0) {
			EDGE_LIFE_INIT = 255;
		}

		EDGE_LIFE_DECREMENT = cfg.getIntProperty(CodeSwarmConfig.EDGE_DECREMENT_KEY,-2);
		if (EDGE_LIFE_DECREMENT >= 0) {
			EDGE_LIFE_DECREMENT = -2;
		}

		EDGE_LEN = cfg.getIntProperty(CodeSwarmConfig.EDGE_LENGTH_KEY);
		if (EDGE_LEN <= 0) {
			EDGE_LEN = 25;
		}
	}

	/**
	 * 1) constructor.
	 * @param from FileNode
	 * @param to PersonNode
	 */
	public Edge(FileNode from, PersonNode to, code_swarm drawableArea) {
		super(EDGE_LIFE_INIT, EDGE_LIFE_DECREMENT, drawableArea);
		this.nodeFrom = from;
		this.nodeTo   = to;
		this.len      = EDGE_LEN;  // 25
	}

	public FileNode getNodeFrom(){
		return nodeFrom;
	}

	public PersonNode getNodeTo(){
		return nodeTo;
	}

	public float getLen(){
		return len;
	}

	/**
	 * 5) drawing the new state.
	 */
	public void draw() {
		if (life > 240) {
			drawableArea.stroke(255, life);
			drawableArea.strokeWeight(0.35f);
			drawableArea.line(nodeFrom.mPosition.x, nodeFrom.mPosition.y, nodeTo.mPosition.x, nodeTo.mPosition.y);
		}
	}

	public void freshen() {
		life = EDGE_LIFE_INIT;
	}
}