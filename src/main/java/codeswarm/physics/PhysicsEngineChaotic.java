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
public class PhysicsEngineChaotic extends PhysicsEngine
{

	private Properties cfg;
	private float DRAG;

	public PhysicsEngineChaotic(code_swarm drawable) {
		super(drawable);
	}

	/**
	 * Method for initializing parameters.
	 * @param p Properties from the config file.
	 */
	public void setup (Properties p)
	{
		cfg = p;
		DRAG = Float.parseFloat(cfg.getProperty("drag","0.00001"));
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
	 * Legacy method that calculate the attractive/repulsive force between a person and one of its file along their link (the edge).
	 *
	 * @param edge the link between a person and one of its file
	 * @return force force calculated between those two nodes
	 */
	private Vector2f calculateForceAlongAnEdge(Edge edge )
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

	private void checkCollisionNew(Node nodeA, Node nodeB/*, float maxD*/) //removed maxD argument since it is not used
	{
		Vector2f tmp = new Vector2f();
		tmp.sub(nodeA.getMPosition(), nodeB.getMPosition());
		double distance = tmp.length();
		if (distance <= (nodeA.getMass() + nodeB.getMass())) {
			float dx = nodeA.getMPosition().x - nodeB.getMPosition().x;
			float dy = nodeA.getMPosition().y - nodeB.getMPosition().y;
			float collision_angle = (float)Math.atan2(dx,dy);
			float magnitude1 = nodeA.getMSpeed().length();
			float magnitude2 = nodeB.getMSpeed().length();
			float direction1 = (float)Math.atan2(nodeA.getMSpeed().y, nodeA.getMSpeed().x);
			float direction2 = (float)Math.atan2(nodeB.getMSpeed().y, nodeB.getMSpeed().x);
			float new_xspeed1 = magnitude1 * (float)Math.cos(direction1 - collision_angle);
			float final_yspeed1 = magnitude1 * (float)Math.sin(direction1 - collision_angle);
			float new_xspeed2 = magnitude2 * (float)Math.cos(direction2 - collision_angle);
			float final_yspeed2 = magnitude2 * (float)Math.sin(direction2 - collision_angle);
			float final_xspeed1 = ((nodeA.getMass()-nodeB.getMass())*new_xspeed1+(nodeB.getMass()+nodeB.getMass())*new_xspeed2)/(nodeA.getMass()+nodeB.getMass());
			float final_xspeed2 = ((nodeA.getMass()+nodeA.getMass())*new_xspeed1+(nodeB.getMass()-nodeA.getMass())*new_xspeed2)/(nodeA.getMass()+nodeB.getMass());

			float nodeA_xspeed = (float)(Math.cos(collision_angle)*final_xspeed1+Math.cos(collision_angle+Math.PI/2)*final_yspeed1);
			float nodeA_yspeed = (float)(Math.sin(collision_angle)*final_xspeed1+Math.sin(collision_angle+Math.PI/2)*final_yspeed1);
			float nodeB_xspeed = (float)(Math.cos(collision_angle)*final_xspeed2+Math.cos(collision_angle+Math.PI/2)*final_yspeed2);
			float nodeB_yspeed = (float)(Math.sin(collision_angle)*final_xspeed2+Math.sin(collision_angle+Math.PI/2)*final_yspeed2);

			nodeA.getMSpeed().set(nodeA_xspeed,nodeA_yspeed);
			nodeB.getMSpeed().set(nodeB_xspeed,nodeB_yspeed);
		}
	}

	// unused method but keeping are for legacy sake
	/* 
	private void checkCollision(Node nodeA, Node nodeB, float maxD)
	{
		Vector2f dVec = new Vector2f();

		dVec.sub(nodeB.getMPosition(), nodeA.getMPosition());
		double d = dVec.length();
		if (d <= (maxD)) { // Yep, a collision
			dVec.normalize();
			float Vp1 = nodeA.getMSpeed().dot(dVec);
			float Vp2 = nodeB.getMSpeed().dot(dVec);
			float dt = (float) ((nodeA.getMass() + nodeB.getMass() - d)/(Vp1 + Vp2));
			nodeA.getMPosition().set(nodeA.getMPosition().x - nodeA.getMSpeed().x * dt, nodeA.getMPosition().y - nodeA.getMSpeed().y * dt);
			nodeB.getMPosition().set(nodeB.getMPosition().x - nodeB.getMSpeed().x * dt, nodeB.getMPosition().y - nodeB.getMSpeed().y * dt);
			dVec.sub(nodeB.getMPosition(), nodeA.getMPosition());
			d = dVec.length();
			dVec.normalize();
			float Va1 = nodeA.getMSpeed().dot(dVec);
			float Va2 = nodeB.getMSpeed().dot(dVec);
			float Vb1 = (-nodeA.getMSpeed().x * dVec.y + nodeA.getMSpeed().y * dVec.x);
			float Vb2 = (-nodeB.getMSpeed().x * dVec.y + nodeB.getMSpeed().y * dVec.x);

			float ed = 1; // ed <= 1, for elastic collision ed = 1
			float vap1 = Va1 + (1 + ed) * (Va2 - Va1) / (1 + nodeA.getMass() / nodeB.getMass());
			float vap2 = Va2 + (1 + ed) * (Va1 - Va2) / (1 + nodeB.getMass() / nodeA.getMass());

			nodeA.getMSpeed().x = vap1*dVec.x - Vb1*dVec.y;
			nodeA.getMSpeed().y = vap1*dVec.y + Vb1*dVec.x;
			nodeB.getMSpeed().x = vap2*dVec.x - Vb2*dVec.y;
			nodeB.getMSpeed().y = vap2*dVec.y + Vb2*dVec.x;

			nodeA.getMPosition().x += nodeA.getMSpeed().x * dt;
			nodeA.getMPosition().y += nodeA.getMSpeed().y * dt;
			nodeB.getMPosition().x += nodeB.getMSpeed().x * dt;
			nodeB.getMPosition().y += nodeB.getMSpeed().y * dt;
		}
	}
	*/


	/**
	 * Legacy method that calculate the repulsive force between two similar nodes (either files or persons).
	 *
	 * @param nodeA [in]
	 * @param nodeB [in]
	 */
	private void calculateForceBetweenfNodes(FileNode nodeA, FileNode nodeB )
	{
		checkCollisionNew(nodeA, nodeB/*, 5*/); //removed maxD argument since it is not used in checkCollisionNew
	}

	/**
	 * Legacy method that calculate the repulsive force between two similar nodes (either files or persons).
	 *
	 * @param nodeA [in]
	 * @param nodeB [in]
	 */
	private void calculateForceBetweenpNodes(PersonNode nodeA, PersonNode nodeB )
	{
		checkCollisionNew(nodeA, nodeB/*, 50*/); //removed maxD argument since it is not used in checkCollisionNew
	}


	/**
	 * Legacy method that apply a force to a node, converting acceleration to speed.
	 *
	 * @param node [in] Node the node to which the force apply
	 * @param force [in] force a force Vector representing the force on a node
	 *
	 * TODO: does force should be a property of the node (or not?)
	 */
	private void applyForceTo(Node node, Vector2f force )
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
	 * Legacy method that apply a force to a node, converting acceleration to speed.
	 *
	 * @param node the node to which the force apply
	 */
	private void applySpeedTo(Node node )
	{
		// This block enforces a maximum absolute velocity.
		if (node.getMSpeed().length() > node.getMaxSpeed()) {
			Vector2f mag = new Vector2f(node.getMSpeed().x / node.getMaxSpeed(), node.getMSpeed().y / node.getMaxSpeed());
			node.getMSpeed().scale(1/mag.lengthSquared());
		}

		// This block convert Speed to Position
		node.getMPosition().add(node.getMSpeed());
	}

	/**
	 *  Do nothing.
	 */
	public void initializeFrame() {
	}

	/**
	 *  Do nothing.
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
		// Calculate force between the node "from" and the node "to"
		Vector2f force = calculateForceAlongAnEdge(edge);

		// transmit force projection to file and person nodes
		force.negate();
		applyForceTo(edge.getNodeFrom(), force); // fNode: attract fNode to pNode
		applySpeedTo(edge.getNodeFrom()); // fNode: move it.
	}

	/**
	 * Method that allows Physics Engine to modify Speed / Position during the update phase.
	 *
	 * @param edge the node to which the force apply
	 *
	 * @Note Standard physics is "Position Variation = Speed x Duration" with a convention of "Duration=1" between to frames
	 */
	public void onUpdateEdge(Edge edge) {
		// shortening life
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
		// Calculation of repulsive force between files
		for (FileNode n : code_swarm.getLivingNodes()) {
			if (n != fNode) {
				// elemental force calculation, and summation
				calculateForceBetweenfNodes(fNode, n);
			}
		}
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

		// ensure coherent resulting position
		fNode.getMPosition().set(constrain(fNode.getMPosition().x, 0.0f, (float)code_swarm.getCodeSwarmWidth()),constrain(fNode.getMPosition().y, 0.0f, (float)code_swarm.getCodeSwarmHeight()));

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
		pNode.getMSpeed().scale(4);

		float distance = pNode.getMSpeed().length();
		if (distance > 0) {
			float deltaDistance = (pNode.getMass() - distance) / (distance * 2);
			deltaDistance *= ((float)pNode.getLife() / pNode.getLifeInit());

			pNode.getMSpeed().scale(deltaDistance);
		}

		applySpeedTo(pNode);
	}

	/**
	 * Method that allows Physics Engine to modify Speed / Position during the update phase.
	 *
	 * @param pNode the node to which the force apply
	 *
	 * @Note Standard physics is "Position Variation = Speed x Duration" with a convention of "Duration=1" between to frames
	 */
	public void onUpdatePerson(PersonNode pNode) {
		// Check for collisions with neighbors.
		for (PersonNode p : code_swarm.getLivingPeople()) {
			if (pNode != p) {
				calculateForceBetweenpNodes(pNode,p);
			}
		}

		// ensure coherent resulting position
		pNode.getMPosition().set(constrain(pNode.getMPosition().x, 0.0f, (float)code_swarm.getCodeSwarmWidth()),constrain(pNode.getMPosition().y, 0.0f, (float)code_swarm.getCodeSwarmHeight()));

		if ((pNode.getMPosition().x < pNode.getMass() && pNode.getMSpeed().x < 0.0f) || (pNode.getMPosition().x > (code_swarm.getCodeSwarmWidth() - pNode.getMass()) && pNode.getMSpeed().x > 0.0f)) {
			// we hit a vertical wall
			pNode.getMSpeed().x = -pNode.getMSpeed().x;
			while (pNode.getMPosition().x < pNode.getMass() || pNode.getMPosition().x > (code_swarm.getCodeSwarmWidth() - pNode.getMass())) {
				pNode.getMPosition().x += pNode.getMSpeed().x;
			}
		}
		if ((pNode.getMPosition().y < pNode.getMass() && pNode.getMSpeed().y < 0.0f) || (pNode.getMPosition().y > (code_swarm.getCodeSwarmHeight() - pNode.getMass()) && pNode.getMSpeed().y > 0.0f)) {
			// we hit a horizontal wall
			pNode.getMSpeed().y = -pNode.getMSpeed().y;
			while (pNode.getMPosition().y < pNode.getMass() || pNode.getMPosition().y > (code_swarm.getCodeSwarmHeight() - pNode.getMass())) {
				pNode.getMPosition().y += pNode.getMSpeed().y;
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
		Vector2f vec = new Vector2f(code_swarm.getCodeSwarmWidth()*(float)Math.random(), code_swarm.getCodeSwarmHeight()*(float)Math.random());
		return vec;
	}

	/**
	 *
	 * @return Vector2f vector holding the starting location for a File Node
	 */
	public Vector2f fStartLocation() {
		Vector2f vec = new Vector2f(code_swarm.getCodeSwarmWidth()*(float)Math.random(), code_swarm.getCodeSwarmHeight()*(float)Math.random());
		return vec;
	}

	/**
	 *
	 * @return Vector2f vector holding the starting velocity for a Person Node
	 */
	public Vector2f pStartVelocity(float mass) {
		Vector2f vec = new Vector2f(mass*((float)Math.random()*2 - 1), mass*((float)Math.random()*2 -1));
		return vec;
	}

	/**
	 *
	 * @return Vector2f vector holding the starting velocity for a File Node
	 */
	public Vector2f fStartVelocity(float mass) {
		Vector2f vec = new Vector2f(mass*((float)Math.random()*2 - 1), mass*((float)Math.random()*2 -1));
		return vec;
	}
}
