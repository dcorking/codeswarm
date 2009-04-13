package codeswarm;

import javax.vecmath.Vector2f;

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

}