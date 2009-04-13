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

import codeswarm.code_swarm;

/**
 * Base class for all drawable objects
 *
 *        Lists and implements features common to all drawable objects
 *        Edge and Node, FileNode and PersonNode
 */
public abstract class Drawable {
	
	protected int life;
	final private int LIFE_INIT;
	final private int LIFE_DECREMENT;
	protected code_swarm drawableArea;

	/**
	 * 1) constructor(s)
	 *
	 * Init jobs common to all objects
	 */
	public Drawable(int lifeInit, int lifeDecrement, code_swarm drawableArea) {
		// save config vars
		LIFE_INIT      = lifeInit;
		LIFE_DECREMENT = lifeDecrement;
		// init life relative vars
		life           = LIFE_INIT;
		this.drawableArea = drawableArea;
	}

	/**
	 *  4) shortening life.
	 */
	public void decay() {
		if (isAlive()) {
			life += LIFE_DECREMENT;
			if (life < 0) {
				life = 0;
			}
		}
	}

	/**
	 * 5) drawing the new state => done in derived class.
	 */
	public abstract void draw();

	/**
	 * 6) reseting life as if new.
	 */
	public abstract void freshen();

	/**
	 * @return true if life > 0
	 */
	public boolean isAlive() {
		return life > 0;
	}

	public int getLife(){
		return life;
	}

	public int getLifeInit(){
		return LIFE_INIT;
	}

}