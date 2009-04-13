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

import javax.vecmath.Vector2f;

import codeswarm.CodeSwarmConfig;
import codeswarm.code_swarm;

/**
 * A node is an abstraction for a File or a Person.
 */
public abstract class Node extends Drawable {
	protected String name;
	protected static float DEFAULT_NODE_SPEED = 7.0f;
	protected Vector2f mPosition;
	protected Vector2f mSpeed;
	protected float maxSpeed = DEFAULT_NODE_SPEED;
	protected float mass;
	protected static int HIGHLIGHT_PCT = 5;

	static{
		CodeSwarmConfig cfg = code_swarm.getConfig();

		DEFAULT_NODE_SPEED = cfg.getFloatProperty(CodeSwarmConfig.NODE_SPEED_KEY, 7.0f);

		HIGHLIGHT_PCT = cfg.getIntProperty(CodeSwarmConfig.HIGHLIGHT_PCT_KEY,5);
		if (HIGHLIGHT_PCT < 0 || HIGHLIGHT_PCT > 100) {
			HIGHLIGHT_PCT = 5;
		}
	}

	/**
	 * 1) constructor.
	 */
	Node(int lifeInit, int lifeDecrement, code_swarm drawableArea) {
		super(lifeInit, lifeDecrement, drawableArea);
		mPosition = new Vector2f();
		mSpeed = new Vector2f();
	}

	public Vector2f getMPosition(){
		return mPosition;
	}

	public float getMaxSpeed(){
		return maxSpeed;
	}

	public Vector2f getMSpeed(){
		return mSpeed;
	}

	public float getMass(){
		return mass;
	}
	
	public String getName(){
		return name;
	}

}