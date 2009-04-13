package codeswarm;

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
	Drawable(int lifeInit, int lifeDecrement, code_swarm drawableArea) {
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