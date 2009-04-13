package codeswarm;

/**
 * An Edge link two nodes together : a File to a Person.
 */
public class Edge extends Drawable {
  protected FileNode nodeFrom;
  protected PersonNode nodeTo;
  protected float len;
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
  Edge(FileNode from, PersonNode to, code_swarm drawableArea) {
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