package codeswarm.physics;

/*
 * Copyright 2008-2009 code_swarm project team
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

import codeswarm.code_swarm;
import codeswarm.processing.Edge;
import codeswarm.processing.FileNode;
import codeswarm.processing.Node;
import codeswarm.processing.PersonNode;

/**
 * @brief Physics Engine implementation.  In essence, people bounce around.  Nodes are attracted to the people.
 *
 * @see PhysicsEngine for interface information
 * @author Desmond Daignault  <nawglan at gmail>
 */
public class PhysicsEngineMaxwellsDemon extends PhysicsEngine
{
	private static final long serialVersionUID = 1L;
	private Properties cfg;
	private float DRAG;
	private Integer doorSize;
	private boolean doorOpen;
	private int midWayX;
	private int midWayY;
	private int startDoorY;
//	private Vector2f doorCenter; //not used
	private int doorWayLeft;
	private int doorWayRight;

	public PhysicsEngineMaxwellsDemon(code_swarm drawable) {
		super(drawable);
	}

	/**
	 * Method for initialising parameters.
	 * @param p Properties from the config file.
	 */
	public void setup (Properties p)
	{
		cfg = p;
		DRAG = Float.parseFloat(cfg.getProperty("drag","0.00001"));
		doorSize = Integer.parseInt(cfg.getProperty("doorSize","100"));
		doorOpen = false;
		midWayX = code_swarm.getCodeSwarmWidth() / 2;
		midWayY = code_swarm.getCodeSwarmHeight() / 2;
		startDoorY = midWayY - doorSize;
//		doorCenter = new Vector2f(midWayX, midWayY); //not used
		doorWayLeft = Integer.parseInt(cfg.getProperty("doorWayLeft","35"));
		doorWayRight = Integer.parseInt(cfg.getProperty("doorWayRight","50"));
	}

	/**
	 *
	 * @param opened Is door open or closed?
	 */
	private void drawWall() {
		// Draw the wall.
		int midWayX = code_swarm.getCodeSwarmWidth() / 2;
		int midWayY = code_swarm.getCodeSwarmHeight() / 2;
		int startDoorY = midWayY - doorSize;

		// draw top of wall
		drawableArea.drawLine(midWayX, 0, midWayX, midWayY, 255, 255, 255);

		// draw door
		if (doorOpen) {
			drawableArea.drawLine(midWayX, startDoorY, midWayX, midWayY, 0, 255, 0);
		} else {
			drawableArea.drawLine(midWayX, startDoorY, midWayX, midWayY, 255, 0, 0);
		}
		// draw bottom of wall
		drawableArea.drawLine(midWayX, midWayY, midWayX, code_swarm.getCodeSwarmHeight(), 255, 255, 255);
	}

	/**
	 * Method to ensure upper and lower bounds
	 * @param value Value to check
	 * @param min Floor value
	 * @param max Ceiling value
	 * @return value if between min and max, min if < max if >
	 */
	private float constrain(float value, float min, float max) {
		if (value < min) {
			return min;
		} else if (value > max) {
			return max;
		}

		return value;
	}

	/**
	 * Calculate the attractive/repulsive force between a person and one of its file
	 * along their link (the edge).
	 *
	 * @param edge the link between a person and one of its file
	 * @return Vector2f force calculated between those two nodes
	 */
	private Vector2f calculateForceAlongAnEdge(Edge edge)
	{
		float distance;
		float deltaDistance;
		Vector2f force = new Vector2f();
		Vector2f tforce = new Vector2f();

		// distance calculation
		tforce.sub(edge.getNodeTo().getMPosition(), edge.getNodeFrom().getMPosition());
		distance = tforce.length();
		if (distance > 0) {
			// force calculation (increase when distance is different from targeted len")
			deltaDistance = (edge.getLen() - distance) / (distance * 3);
			// force ponderation using a re-mapping life from 0-255 scale to 0-1.0 range
			// This allows nodes to drift apart as their life decreases.
			deltaDistance *= ((float)edge.getLife() / edge.getLifeInit());

			// force projection onto x and y axis
			tforce.scale(deltaDistance);

			force.set(tforce);
		}

		return force;
	}

	/**
	 * Calculate the repulsive force between two similar file nodes.
	 *
	 * @param nodeA
	 * @param nodeB
	 * @return Vector2f force calculated between those two nodes
	 */
	private Vector2f calculateForceBetweenfNodes(FileNode nodeA, FileNode nodeB)
	{
		float distance;
		Vector2f force = new Vector2f();
		Vector2f normVec = new Vector2f();

		/**
		 * Get the distance between nodeA and nodeB
		 */
		normVec.sub(nodeA.getMPosition(), nodeB.getMPosition());
		distance = normVec.lengthSquared();
		/**
		 * If there is a Collision.  This is assuming a radius of zero.
		 * if (lensq == (radius1 + radius2)) is what to use if we have radius
		 * could use touches for files and edge_length for people?
		 */
		if (distance == (nodeA.getTouches() + nodeB.getTouches())) {
			force.set(0.01f* (((float)Math.random()*2)-1), (0.01f* ((float)Math.random()*2)-1));
		} else if (distance < 10000) {
			/**
			 * No collision and distance is close enough to actually matter.
			 */
			normVec.scale(1/distance);
			force.set(normVec);
		}

		return force;
	}

	/**
	 * Calculate the repulsive force between two similar person nodes
	 * People ricochet off of each other and walls.
	 *
	 * @param nodeA
	 * @param nodeB
	 * @return Vector2f force calculated between those two nodes
	 */
	private Vector2f calculateForceBetweenpNodes(PersonNode nodeA, PersonNode nodeB)
	{
		Vector2f force = new Vector2f();
		Vector2f tmp = new Vector2f();

		tmp.sub(nodeA.getMPosition(), nodeB.getMPosition());
		double distance = Math.sqrt(tmp.lengthSquared());
		if (distance <= (nodeA.getMass() + nodeB.getMass())) {
			if (nodeA.getMSpeed().x > 0 && nodeA.getMSpeed().y > 0) {          // Node A down and right
				if (nodeB.getMSpeed().x < 0 && nodeB.getMSpeed().y > 0) {        // Node B down and left
					nodeA.getMSpeed().x *= -1;
					nodeB.getMSpeed().x *= -1;
				} else if (nodeB.getMSpeed().x > 0 && nodeB.getMSpeed().y < 0) { // Node B up and right
					nodeA.getMSpeed().y *= -1;
					nodeB.getMSpeed().y *= -1;
				} else if (nodeB.getMSpeed().x < 0 && nodeB.getMSpeed().y < 0) { // Node B up and left
					nodeA.getMSpeed().negate();
					nodeB.getMSpeed().negate();
				} else {                                               // Node B down and right
					nodeB.getMSpeed().x *= -1;
					nodeA.getMSpeed().x *= 2;
				}
			} else if (nodeA.getMSpeed().x > 0 && nodeA.getMSpeed().y < 0) {   // Node A up and right
				if (nodeB.getMSpeed().x < 0 && nodeB.getMSpeed().y > 0) {        // Node B down and left
					nodeA.getMSpeed().negate();
					nodeB.getMSpeed().negate();
				} else if (nodeB.getMSpeed().x > 0 && nodeB.getMSpeed().y < 0) { // Node B up and right
					nodeA.getMSpeed().x *= -1;
					nodeB.getMSpeed().x *= 2;
				} else if (nodeB.getMSpeed().x < 0 && nodeB.getMSpeed().y < 0) { // Node B up and left
					nodeA.getMSpeed().x *= -1;
					nodeB.getMSpeed().x *= -1;
				} else {                                               // Node B down and right
					nodeA.getMSpeed().y *= -1;
					nodeB.getMSpeed().y *= -1;
				}
			} else if (nodeA.getMSpeed().x < 0 && nodeA.getMSpeed().y > 0) {   // Node A down and left
				if (nodeB.getMSpeed().x < 0 && nodeB.getMSpeed().y > 0) {        // Node B down and left
					nodeB.getMSpeed().x *= -1;
					nodeA.getMSpeed().x *= 2;
				} else if (nodeB.getMSpeed().x > 0 && nodeB.getMSpeed().y < 0) { // Node B up and right
					nodeA.getMSpeed().negate();
					nodeB.getMSpeed().negate();
				} else if (nodeB.getMSpeed().x < 0 && nodeB.getMSpeed().y < 0) { // Node B up and left
					nodeA.getMSpeed().y *= -1;
					nodeB.getMSpeed().y *= -1;
				} else {                                               // Node B down and right
					nodeA.getMSpeed().x *= -1;
					nodeB.getMSpeed().x *= -1;
				}
			} else {                                                 // Node A up and left
				if (nodeB.getMSpeed().x < 0 && nodeB.getMSpeed().y > 0) {        // Node B down and left
					nodeA.getMSpeed().y *= -1;
					nodeB.getMSpeed().y *= -1;
				} else if (nodeB.getMSpeed().x > 0 && nodeB.getMSpeed().y < 0) { // Node B up and right
					nodeA.getMSpeed().x *= -1;
					nodeB.getMSpeed().x *= -1;
				} else if (nodeB.getMSpeed().x < 0 && nodeB.getMSpeed().y < 0) { // Node B up and left
					nodeA.getMSpeed().x *= -1;
					nodeB.getMSpeed().x *= 2;
				} else {                                               // Node B down and right
					nodeA.getMSpeed().negate();
					nodeB.getMSpeed().negate();
				}
			}
			while (distance <= (nodeA.getMass() + nodeB.getMass())) {
				applySpeedTo(nodeA);
				constrainNode(nodeA, whichSide(nodeA));
				applySpeedTo(nodeB);
				constrainNode(nodeB, whichSide(nodeB));
				tmp.sub(nodeA.getMPosition(), nodeB.getMPosition());
				distance = Math.sqrt(tmp.lengthSquared());
			}
		}
		/**
		 * No collision
		 */
		return force;
	}

	/**
	 * Apply force to a node, converting acceleration to speed.
	 *
	 * @param node Node the node to which the force apply
	 * @param force force a force Vector representing the force on a node
	 *
	 * TODO: does force should be a property of the node (or not?)
	 */
	private void applyForceTo(Node node, Vector2f force)
	{
		double dlen;
		Vector2f mod = new Vector2f(force);

		/**
		 * Taken from Newton's 2nd law.  F=ma
		 */
		dlen = mod.length();
		if (dlen > 0) {
			mod.scale(node.getMass());
			node.getMSpeed().add(mod);
		}
	}

	/**
	 * Apply force to a node, converting speed to position.
	 *
	 * @param node the node to which the force apply
	 */
	private void applySpeedTo(Node node)
	{
		// This block enforces a maximum absolute velocity.
		if (node.getMSpeed().length() > node.getMaxSpeed()) {
			Vector2f mag = new Vector2f(node.getMSpeed().x / node.getMaxSpeed(), node.getMSpeed().y / node.getMaxSpeed());
			node.getMSpeed().scale(1/mag.lengthSquared());
		}

		// This block convert Speed to Position
		node.getMPosition().add(node.getMSpeed());
	}

	private boolean nearDoor(Node node) {
		if (node.getMPosition().x > (midWayX - doorWayLeft) && node.getMPosition().x < (midWayX + doorWayRight) && node.getMPosition().y >= startDoorY && node.getMPosition().y <= midWayY) {
			return true;
		}
		return false;
	}

	private void constrainNode(Node node, boolean rightSide) {
		if (nearDoor(node)) {
			if (doorOpen) {
				node.getMPosition().set(constrain(node.getMPosition().x, 0.0f, (float)code_swarm.getCodeSwarmWidth()),constrain(node.getMPosition().y, 0.0f, (float)code_swarm.getCodeSwarmHeight()));
			} else {
				if (rightSide) {
					node.getMPosition().set(constrain(node.getMPosition().x, (float)(midWayX + 8), (float)code_swarm.getCodeSwarmWidth()),constrain(node.getMPosition().y, 0.0f, (float)code_swarm.getCodeSwarmHeight()));
				} else {
					node.getMPosition().set(constrain(node.getMPosition().x, 0.0f, (float)(midWayX - 8)),constrain(node.getMPosition().y, 0.0f, (float)code_swarm.getCodeSwarmHeight()));
				}
			}
		} else { // not near the door.
			if (rightSide) {
				node.getMPosition().set(constrain(node.getMPosition().x, (float)(midWayX + 8), (float)code_swarm.getCodeSwarmWidth()),constrain(node.getMPosition().y, 0.0f, (float)code_swarm.getCodeSwarmHeight()));
			} else {
				node.getMPosition().set(constrain(node.getMPosition().x, 0.0f, (float)(midWayX - 8)),constrain(node.getMPosition().y, 0.0f, (float)code_swarm.getCodeSwarmHeight()));
			}
		}
	}

	private boolean whichSide(Node node) {
		// which half of the screen are we on?
		// true = right side
		return (node.getMPosition().x >= midWayX);
	}

	/**
	 *  Interface methods below.
	 */

	/**
	 * draw the wall opened or closed, depending on closeness of people.
	 */
	public void initializeFrame() {
		doorOpen = false;

		for (PersonNode p : code_swarm.getLivingPeople()) {
			if (p.getMSpeed().x < 0.0f && nearDoor(p)) {
				doorOpen = true;
				break;
			}
		}

		drawWall();
	}

	/**
	 * close the door until next iteration
	 */
	public void finalizeFrame() {
	}


	/**
	 * Method that allows Physics Engine to modify forces between files and people during the relax stage
	 *
	 * @param edge the edge to which the force apply (both ends)
	 *
	 * @Note Standard physics is "Position Variation = Speed x Duration" with a convention of "Duration=1" between to frames
	 */
	public void onRelaxEdge(Edge edge) {
		boolean fSide = whichSide(edge.getNodeFrom());
		boolean pSide = whichSide(edge.getNodeTo());

		if ((!doorOpen && fSide != pSide) || ((doorOpen && edge.getNodeFrom().getMPosition().y < startDoorY) || (doorOpen && edge.getNodeFrom().getMPosition().y > startDoorY + doorSize))) {
			return;
		}

		// Calculate force between the node "from" and the node "to"
		Vector2f force = calculateForceAlongAnEdge(edge);

		// transmit force projection to file and person nodes
		force.negate();
		applyForceTo(edge.getNodeFrom(), force); // fNode: attract fNode to pNode
		// which half of the screen are we on?
		applySpeedTo(edge.getNodeFrom()); // fNode: move it.
		constrainNode(edge.getNodeFrom(), whichSide(edge.getNodeFrom())); // Keep it in bounds.
	}

	/**
	 * Method that allows Physics Engine to modify Speed / Position during the update phase.
	 *
	 * @param edge the node to which the force apply
	 *
	 * @Note Standard physics is "Position Variation = Speed x Duration" with a convention of "Duration=1" between to frames
	 */
	public void onUpdateEdge(Edge edge) {
		edge.decay();
	}

	/**
	 * Method that allows Physics Engine to modify Speed / Position during the relax phase.
	 *
	 * @param fNode the node to which the force apply
	 *
	 * @Note Standard physics is "Position Variation = Speed x Duration" with a convention of "Duration=1" between to frames
	 */
	public void onRelaxNode(FileNode fNode ) {
		boolean mySide = whichSide(fNode);

		Vector2f forceBetweenFiles = new Vector2f();
		Vector2f forceSummation    = new Vector2f();

		// Calculation of repulsive force between persons
		for (FileNode n : code_swarm.getLivingNodes()) {
			if (n != fNode && mySide == whichSide(n)) {
				// elemental force calculation, and summation
				forceBetweenFiles = calculateForceBetweenfNodes(fNode, n);
				forceSummation.add(forceBetweenFiles);
			}
		}
		// Apply repulsive force from other files to this Node
		applyForceTo(fNode, forceSummation);
	}

	/**
	 * Method that allows Physics Engine to modify Speed / Position during the update phase.
	 *
	 * @param fNode the node to which the force apply
	 *
	 * @Note Standard physics is "Position Variation = Speed x Duration" with a convention of "Duration=1" between to frames
	 */
	public void onUpdateNode(FileNode fNode) {
		// Apply Speed to Position on nodes
		applySpeedTo(fNode);
		constrainNode(fNode, whichSide(fNode)); // Keep it in bounds.

		// shortening life
		fNode.decay();

		// Apply drag (reduce Speed for next frame calculation)
		fNode.getMSpeed().scale(DRAG);
	}

	/**
	 * Method that allows Physics Engine to modify Speed / Position during the relax phase.
	 *
	 * @param pNode the node to which the force apply
	 *
	 * @Note Standard physics is "Position Variation = Speed x Duration" with a convention of "Duration=1" between to frames
	 */
	public void onRelaxPerson(PersonNode pNode) {
		if (pNode.getMSpeed().length() == 0) {
			// Range (-1,1)
			pNode.getMSpeed().set(pNode.getMass()*((float)Math.random()-pNode.getMass()),pNode.getMass()*((float)Math.random()-pNode.getMass()));
		}

		pNode.getMSpeed().scale(pNode.getMass());
		pNode.getMSpeed().normalize();
		pNode.getMSpeed().scale(5);

		float distance = pNode.getMSpeed().length();
		if (distance > 0) {
			float deltaDistance = (pNode.getMass() - distance) / (distance * 2);
			deltaDistance *= ((float)pNode.getLife() / pNode.getLifeInit());

			pNode.getMSpeed().scale(deltaDistance);
		}
	}

	/**
	 * Method that allows Physics Engine to modify Speed / Position during the update phase.
	 *
	 * @param pNode the node to which the force apply
	 *
	 * @Note Standard physics is "Position Variation = Speed x Duration" with a convention of "Duration=1" between to frames
	 */
	public void onUpdatePerson(PersonNode pNode) {
		boolean rightSide = whichSide(pNode);

		applySpeedTo(pNode);

		// Check for collisions with neighbors.
		for (PersonNode p : code_swarm.getLivingPeople()) {
			if (pNode != p) {
				Vector2f force = calculateForceBetweenpNodes(pNode,p);
				pNode.getMPosition().add(force);
			}
		}

		constrainNode(pNode, rightSide); // Keep it in bounds.

		if (doorOpen) {
			// Check for vertical wall collisions
			// 4 walls to check.
			//  |  |  |
			//  |  |  |
			//  |     |
			//  |  |  |
			//  |  |  |
			if (pNode.getMPosition().y < startDoorY || pNode.getMPosition().y > midWayY) { // Above the door, and below the door.
				if (rightSide) {
					if ((pNode.getMPosition().x < (midWayX + pNode.getMass()) && pNode.getMSpeed().x < 0.0f) || (pNode.getMPosition().x > (code_swarm.getCodeSwarmWidth() - pNode.getMass()) && pNode.getMSpeed().x > 0.0f)) {
						pNode.getMSpeed().x = -pNode.getMSpeed().x;
						int i = 0;
						while (pNode.getMPosition().x < (midWayX + pNode.getMass()) || pNode.getMPosition().x > (code_swarm.getCodeSwarmWidth() - pNode.getMass())) {
							pNode.getMPosition().x += pNode.getMSpeed().x * (i++ % 10);
						}
					}
				} else { // left side
					if ((pNode.getMPosition().x < pNode.getMass() && pNode.getMSpeed().x < 0.0f) || (pNode.getMPosition().x > (midWayX - pNode.getMass()) && pNode.getMSpeed().x > 0.0f)) {
						pNode.getMSpeed().x = -pNode.getMSpeed().x;
						int i = 0;
						while (pNode.getMPosition().x < pNode.getMass() || pNode.getMPosition().x > (midWayX - pNode.getMass())) {
							pNode.getMPosition().x += pNode.getMSpeed().x * (i++ % 10);
						}
					}
				}
			} else { // Same level as the door
				if ((pNode.getMPosition().x < pNode.getMass() && pNode.getMSpeed().x < 0.0f) || (pNode.getMPosition().x > (code_swarm.getCodeSwarmWidth() - pNode.getMass()) && pNode.getMSpeed().x > 0.0f)) {
					pNode.getMSpeed().x = -pNode.getMSpeed().x;
					int i = 0;
					while (pNode.getMPosition().x < pNode.getMass() || pNode.getMPosition().x > (code_swarm.getCodeSwarmWidth() - pNode.getMass())) {
						pNode.getMPosition().x += pNode.getMSpeed().x * (i++ % 10);
					}
				}
			}
		} else { // Door is closed.
			// Check for vertical wall collisions
			// 3 walls to check.
			//  |  |  |
			//  |  |  |
			//  |  |  |
			//  |  |  |
			//  |  |  |

			if (rightSide) {
				if ((pNode.getMPosition().x < (midWayX + pNode.getMass()) && pNode.getMSpeed().x < 0.0f) || (pNode.getMPosition().x > (code_swarm.getCodeSwarmWidth() - pNode.getMass()) && pNode.getMSpeed().x > 0.0f)) {
					pNode.getMSpeed().x = -pNode.getMSpeed().x;
					int i = 0;
					while (pNode.getMPosition().x < (midWayX + pNode.getMass()) || pNode.getMPosition().x > (code_swarm.getCodeSwarmWidth() - pNode.getMass())) {
						pNode.getMPosition().x += pNode.getMSpeed().x * (i++ % 10);
					}
				}
			} else { // left side
				if ((pNode.getMPosition().x < pNode.getMass() && pNode.getMSpeed().x < 0.0f) || (pNode.getMPosition().x > (midWayX - pNode.getMass()) && pNode.getMSpeed().x > 0.0f)) {
					pNode.getMSpeed().x = -pNode.getMSpeed().x;
					int i = 0;
					while (pNode.getMPosition().x < pNode.getMass() || pNode.getMPosition().x > (midWayX - pNode.getMass())) {
						pNode.getMPosition().x += pNode.getMSpeed().x * (i++ % 10);
					}
				}
			}
		}

		// Check for horizontal wall collisions
		// 2 walls to check.
		//  _______
		//
		//
		//
		//  _______

		if ((pNode.getMPosition().y < pNode.getMass() && pNode.getMSpeed().y < 0.0f) || ((pNode.getMPosition().y > (code_swarm.getCodeSwarmHeight() - pNode.getMass()) && pNode.getMSpeed().y > 0.0f))) {
			pNode.getMSpeed().y = -pNode.getMSpeed().y;
			int i = 0;
			while (pNode.getMPosition().y < pNode.getMass() || pNode.getMPosition().y > (code_swarm.getCodeSwarmHeight() - pNode.getMass())) {
				pNode.getMPosition().y += pNode.getMSpeed().y * (i++ % 10);
			}
		}
		// shortening life
		pNode.decay();

		// Apply drag (reduce Speed for next frame calculation)
		pNode.getMSpeed().scale(DRAG);
	}

	/**
	 *
	 * @return Vector2f vector holding the starting location for a Person Node
	 */
	public Vector2f pStartLocation() {
		float x = (float)Math.random() * midWayX + midWayX;
		float y = (float)Math.random() * code_swarm.getCodeSwarmHeight();

		constrain(x, (midWayX + 10), (code_swarm.getCodeSwarmWidth() - 10));
		constrain(y, 10, (code_swarm.getCodeSwarmHeight() - 10));

		Vector2f vec = new Vector2f(x, y);

		return vec;
	}

	/**
	 *
	 * @return Vector2f vector holding the starting location for a File Node
	 */
	public Vector2f fStartLocation() {
		float x = (float)Math.random() * midWayX + midWayX;
		float y = (float)Math.random() * code_swarm.getCodeSwarmHeight();

		constrain(x, (midWayX + 10), (code_swarm.getCodeSwarmWidth() - 10));
		constrain(y, 10, (code_swarm.getCodeSwarmHeight() - 10));

		Vector2f vec = new Vector2f(x, y);

		return vec;
	}

	/**
	 *
	 * @param mass
	 * @return Vector2f vector holding the starting velocity for a Person Node
	 */
	public Vector2f pStartVelocity(float mass) {
		Vector2f vec = new Vector2f(mass*((float)Math.random()*2 - 1), mass*((float)Math.random()*2 -1));
		return vec;
	}

	/**
	 *
	 * @param mass
	 * @return Vector2f vector holding the starting velocity for a File Node
	 */
	public Vector2f fStartVelocity(float mass) {
		Vector2f vec = new Vector2f(mass*((float)Math.random()*2 - 1), mass*((float)Math.random()*2 -1));
		return vec;
	}
}

