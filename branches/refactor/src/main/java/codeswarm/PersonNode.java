package codeswarm;

import javax.vecmath.Vector2f;

/**
 * A node describing a person, which is repulsed by other persons.
 */
public class PersonNode extends Node {
  private int flavor;
  private int colorCount = 1;
  private int minBold;
  protected int touches;
  private static int PERSON_LIFE_INIT = 255;
  private static int PERSON_LIFE_DECREMENT = -1;
  private static float DEFAULT_PERSON_SPEED = 2.0f;

  static{
	  CodeSwarmConfig cfg = code_swarm.getConfig();
	  
	  PERSON_LIFE_INIT = cfg.getIntProperty(CodeSwarmConfig.PERSON_LIFE_KEY,255);
	  if (PERSON_LIFE_INIT <= 0) {
		  PERSON_LIFE_INIT = 255;
	  }

	  PERSON_LIFE_DECREMENT = cfg.getIntProperty(CodeSwarmConfig.PERSON_DECREMENT_KEY,-1);
	  if (PERSON_LIFE_DECREMENT >= 0) {
		  PERSON_LIFE_DECREMENT = -1;
	  }
	  
	  DEFAULT_PERSON_SPEED = cfg.getFloatProperty(CodeSwarmConfig.PERSON_SPEED_KEY, DEFAULT_NODE_SPEED);
  }
  
  /**
   * 1) constructor.
   */
  public PersonNode(String n, int processingAppletColor, float personMass, Vector2f startLocation, Vector2f startVelocity, code_swarm drawableArea) {
    super(PERSON_LIFE_INIT, PERSON_LIFE_DECREMENT, drawableArea); // -1
    flavor = processingAppletColor;
    maxSpeed = DEFAULT_PERSON_SPEED;
    name = n;
    minBold = (int)(PERSON_LIFE_INIT * (1 - ((float) HIGHLIGHT_PCT)/100));
    touches = 1;
    mass = personMass;
    mPosition.set(startLocation);
    mSpeed.set(startVelocity);
  }

  /**
   * 5) drawing the new state.
   */
  public void draw() {
    if (isAlive()) {
    	drawableArea.textAlign(processing.core.PConstants.CENTER, processing.core.PConstants.CENTER);

      /** TODO: proportional font size, or light intensity,
                or some sort of thing to disable the flashing */
      if (life >= minBold)
    	  drawableArea.textFont(code_swarm.getBoldPFont());
      else
    	  drawableArea.textFont(code_swarm.getPFont());

      drawableArea.text(name, mPosition.x, mPosition.y);
    }
  }

  public void freshen () {
    life = PERSON_LIFE_INIT;
    touches++;
  }

  public void addColor(int c) {
	  drawableArea.colorMode(processing.core.PConstants.RGB);
    flavor = drawableArea.lerpColor(flavor, c, 1.0f / colorCount);
    colorCount++;
  }
  
  public int getFlavor(){
	  return flavor;
  }
}