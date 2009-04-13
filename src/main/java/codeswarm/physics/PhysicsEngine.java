package codeswarm.physics;

/**
 * Copyright 2008 code_swarm project team
 *
 * This file is part of code_swarm.
 *
 * code_swarm is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * code_swarm is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with code_swarm.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.Properties;

import javax.vecmath.Vector2f;

import codeswarm.Edge;
import codeswarm.FileNode;
import codeswarm.PersonNode;
import codeswarm.code_swarm;

/**
 * Abstract interface of any code_swarm physical engine.
 *
 * @note Need to be derived to define force calculation algorithms between Nodes
 * @note Need to use the constructor to apply some configuration options
 *
 * @note For portability, no Processing library should be use there, only standard Java packages
 */
public abstract class PhysicsEngine
{

	private String name;
	protected code_swarm drawableArea;
	
	public PhysicsEngine(code_swarm drawable){
		drawableArea = drawable;
	}

	/**
	 * Initialize the Physical Engine
	 * @param p Properties file
	 */
	public abstract void setup (Properties p);

	/**
	 * Method that allows Physics Engine to initialize the Frame
	 *
	 */
	public abstract void initializeFrame();

	/**
	 * Method that allows Physics Engine to finalize the Frame
	 *
	 */
	public abstract void finalizeFrame();

	/**
	 * Method that allows Physics Engine to modify Speed / Position during the relax phase.
	 *
	 * @param edge the node to which the force apply
	 *
	 * @Note Standard physics is "Position Variation = Speed x Duration" with a convention of "Duration=1" between to frames
	 */
	public abstract void onRelaxEdge(Edge edge);

	/**
	 * Method that allows Physics Engine to modify Speed / Position during the relax phase.
	 *
	 * @param fNode the node to which the force apply
	 *
	 * @Note Standard physics is "Position Variation = Speed x Duration" with a convention of "Duration=1" between to frames
	 */
	public abstract void onRelaxNode(FileNode fNode);

	/**
	 * Method that allows Physics Engine to modify Speed / Position during the relax phase.
	 *
	 * @param pNode the node to which the force apply
	 *
	 * @Note Standard physics is "Position Variation = Speed x Duration" with a convention of "Duration=1" between to frames
	 */
	public abstract void onRelaxPerson(PersonNode pNode);

	/**
	 * Method that allows Physics Engine to modify Speed / Position during the update phase.
	 *
	 * @param edge the node to which the force apply
	 *
	 * @Note Standard physics is "Position Variation = Speed x Duration" with a convention of "Duration=1" between to frames
	 */
	public abstract void onUpdateEdge(Edge edge);

	/**
	 * Method that allows Physics Engine to modify Speed / Position during the update phase.
	 *
	 * @param fNode the node to which the force apply
	 *
	 * @Note Standard physics is "Position Variation = Speed x Duration" with a convention of "Duration=1" between to frames
	 */
	public abstract void onUpdateNode(FileNode fNode);

	/**
	 * Method that allows Physics Engine to modify Speed / Position during the update phase.
	 *
	 * @param pNode the node to which the force apply
	 *
	 * @Note Standard physics is "Position Variation = Speed x Duration" with a convention of "Duration=1" between to frames
	 */
	public abstract void onUpdatePerson(PersonNode pNode);

	/**
	 *
	 * @return Vector2f vector holding the starting location for a Person Node
	 */
	public abstract Vector2f pStartLocation();

	/**
	 *
	 * @return Vector2f vector holding the starting location for a File Node
	 */
	public abstract Vector2f fStartLocation();

	/**
	 *
	 * @return Vector2f vector holding the starting velocity for a Person Node
	 */
	public abstract Vector2f pStartVelocity(float mass);

	/**
	 *
	 * @return Vector2f vector holding the starting velocity for a File Node
	 */
	public abstract Vector2f fStartVelocity(float mass);

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;	
	}

}

