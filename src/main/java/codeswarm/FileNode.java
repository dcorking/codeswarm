package codeswarm;

import javax.vecmath.Vector2f;

import processing.core.PApplet;

/**
 * A node describing a file, which is repulsed by other files.
 */
public class FileNode extends Node implements Comparable<FileNode> {
  private int nodeHue;
  private int minBold;
  protected int touches;
  private static float DEFAULT_FILE_SPEED = 7.0f;
  private static int FILE_LIFE_INIT = 255;
  private static int FILE_LIFE_DECREMENT = -1;
  private int maxTouches;

  static{
	  CodeSwarmConfig cfg = code_swarm.getConfig();
	  
	  DEFAULT_FILE_SPEED = cfg.getFloatProperty(CodeSwarmConfig.FILE_SPEED_KEY, DEFAULT_NODE_SPEED);

	  FILE_LIFE_INIT = cfg.getIntProperty(CodeSwarmConfig.FILE_LIFE_KEY,255);
	  if (FILE_LIFE_INIT <= 0) {
		  FILE_LIFE_INIT = 255;
	  }

	  FILE_LIFE_DECREMENT = cfg.getIntProperty(CodeSwarmConfig.FILE_DECREMENT_KEY,-2);
	  if (FILE_LIFE_DECREMENT >= 0) {
		  FILE_LIFE_DECREMENT = -2;
	  }
  }
  
  /**
   * 1) constructor.
   */
  public FileNode(FileEvent fe, float fileMass, Vector2f startLocation, Vector2f startVelocity, code_swarm drawableArea) {
    super(FILE_LIFE_INIT, FILE_LIFE_DECREMENT, drawableArea); // 255, -2
    name = fe.getPath() + fe.getFilename();
    touches = fe.getWeight();
    life = FILE_LIFE_INIT;
    drawableArea.colorMode(processing.core.PConstants.RGB);
    minBold = (int)(FILE_LIFE_INIT * ((100.0f - HIGHLIGHT_PCT)/100));
    nodeHue = drawableArea.getColorAssigner().getColor(name);
    mass = fileMass;
    maxSpeed = DEFAULT_FILE_SPEED;
    mPosition.set(startLocation);
    mSpeed.set(startVelocity);
  }

  /**
   * 5) drawing the new state.
   */
  public void draw() {
    if (life > 0) {
      if (drawableArea.isDrawFilesSharp()) {
        drawSharp();
      }
      if (drawableArea.isDrawFilesFuzzy()) {
        drawFuzzy();
      }
      if (drawableArea.isDrawFilesJelly()) {
        drawJelly();
      }

      /** TODO : this would become interesting on some special event, or for special materials
       * colorMode( RGB ); fill( 0, life ); textAlign( CENTER, CENTER ); text( name, x, y );
       * Example below:
       */
      if (drawableArea.isShowPopular()) {
    	  drawableArea.textAlign( processing.core.PConstants.CENTER, processing.core.PConstants.CENTER );
        if (this.qualifies()) {
        	drawableArea.text(touches, mPosition.x, mPosition.y - (8 + (int)Math.sqrt(touches)));
        }
      }
    }
  }

  /**
   * 6) reseting life as if new.
   */
  public void freshen() {
    life = FILE_LIFE_INIT;
    if (++touches > maxTouches) {
      maxTouches = touches;
    }
  }

  /**
   * reset life and add event weight to touches
   */
  public void freshen( FileEvent fe ) {
    life = FILE_LIFE_INIT;
    touches += fe.getWeight();

    // do not allow negative touches
    if ( touches < 0 )
      touches = 0;
    if ( touches > maxTouches )
      maxTouches = touches;
  }

  public boolean qualifies() {
    if (this.touches >= (maxTouches * 0.5f)) {
      return true;
    }
    return false;
  }

  public int compareTo(FileNode fn) {
    int retval = 0;
    if (this.touches < fn.touches) {
      retval = -1;
    } else if (this.touches > fn.touches) {
      retval = 1;
    }
    return retval;
  }

  public void drawSharp() {
	  drawableArea.colorMode(processing.core.PConstants.RGB);
	  drawableArea.fill(nodeHue, life);
    float w = 3;

    if (life >= minBold) {
    	drawableArea.stroke(255, 128);
      w *= 2;
    } else {
    	drawableArea.noStroke();
    }

    drawableArea.ellipseMode(processing.core.PConstants.CENTER);
    drawableArea.ellipse(mPosition.x, mPosition.y, w, w);
  }

  public void drawFuzzy() {
	  drawableArea.tint(nodeHue, life);

    float w = 8 + (PApplet.sqrt(touches) * 4);
    // not used float dubw = w * 2;
    float halfw = w / 2;
    if (life >= minBold) {
    	drawableArea.colorMode(processing.core.PConstants.HSB);
    	drawableArea.tint(drawableArea.hue(nodeHue), drawableArea.saturation(nodeHue) - 192, 255, life);
      // image( sprite, x - w, y - w, dubw, dubw );
    }
    // else
    drawableArea.image(drawableArea.getSprite(), mPosition.x - halfw, mPosition.y - halfw, w, w);
  }

  public void drawJelly() {
	  drawableArea.noFill();
    if (life >= minBold)
    	drawableArea.stroke(255);
    else
    	drawableArea.stroke(nodeHue, life);
    float w = PApplet.sqrt(touches);
    drawableArea.ellipseMode(processing.core.PConstants.CENTER);
    drawableArea.ellipse(mPosition.x, mPosition.y, w, w);
  }
  
  public int getNodeHue(){
	  return nodeHue;
  }
  
  public int getTouches(){
	  return touches;
  }
  
  /**
   * @return file node as a string
   */
  public String toString() {
    return "FileNode{" + "name='" + name + '\'' + ", nodeHue=" + nodeHue + ", touches=" + touches + '}';
  }
}