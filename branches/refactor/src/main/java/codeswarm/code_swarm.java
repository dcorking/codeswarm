package codeswarm;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import codeswarm.physics.PhysicsEngine;
import codeswarm.processing.ColorAssigner;
import codeswarm.processing.ColorBins;
import codeswarm.processing.ColorTest;
import codeswarm.processing.Drawable;
import codeswarm.processing.Edge;
import codeswarm.processing.FileEvent;
import codeswarm.processing.FileNode;
import codeswarm.processing.Node;
import codeswarm.processing.PersonNode;
import codeswarm.ui.MainView;

public class code_swarm extends PApplet implements TaskListener {

	/** @remark needed for any serializable class */
	private static final long serialVersionUID = 0;

	// User-defined variables
	private int FRAME_RATE = 24;
	private long UPDATE_DELTA = -1;
	private String SCREENSHOT_FILE;
	private int background;

	// Data storage
	private BlockingQueue<FileEvent> eventsQueue;
	private boolean isInputSorted = false;
	private static CopyOnWriteArrayList<FileNode> nodes;
	private static CopyOnWriteArrayList<Edge> edges;
	private static CopyOnWriteArrayList<PersonNode> people;
	private LinkedList<ColorBins> history;

	private boolean finishedLoading = false;

	// Temporary variables
	private FileEvent currentEvent;
	private Date nextDate;
	private Date prevDate;
	//  private FileNode prevNode;

	// Graphics objects
	private static PFont font;
	private static PFont boldFont;
	private PImage sprite;

	// Graphics state variables
	private boolean looping = true;
	private boolean coolDown = false;
	private boolean showHistogram = true;
	private boolean showDate = true;
	private boolean showLegend = false;
	private boolean showPopular = false;
	private boolean showEdges = false;
	private boolean showEngine = false;
	private boolean showHelp = false;
	private boolean takeSnapshots = false;
	private boolean showDebug = false;
	private boolean drawNamesSharp = false;
	private boolean drawNamesHalos = false;
	private boolean drawFilesSharp = false;
	private boolean drawFilesFuzzy = false;
	private boolean drawFilesJelly = false;

	// Color mapper
	private ColorAssigner colorAssigner;
	//  private int currentColor;

	// Physics engine configuration
	private String          physicsEngineConfigDir;
	private String          physicsEngineSelection;
	private java.util.Vector<PhysicsEngine> mPhysicsEngineChoices = new java.util.Vector<PhysicsEngine>();
	private PhysicsEngine  mPhysicsEngine = null;
	private boolean safeToToggle = false;
	private boolean wantToToggle = false;
	private boolean toggleDirection = false;


	// Default Physics Engine (class) name
	private static final String PHYSICS_ENGINE_LEGACY  = "PhysicsEngineLegacy";

	// Formats the date string nicely
	private DateFormat formatter = DateFormat.getDateInstance();

	private static CodeSwarmConfig cfg;
	private long lastDrawDuration = 0;
	private String loadingMessage = "Reading input file";
	private static int width=0;
	private static int height=0;
	private int maxFramesSaved;

	private int maxBackgroundThreads;
	private ExecutorService backgroundExecutor;

	/**
	 * Initialisation
	 */
	public void setup() {
		width=cfg.getIntProperty(CodeSwarmConfig.WIDTH_KEY,640);
		if (width <= 0) {
			width = 640;
		}

		height=cfg.getIntProperty(CodeSwarmConfig.HEIGHT_KEY,480);
		if (height <= 0) {
			height = 480;
		}

		maxBackgroundThreads=cfg.getIntProperty(CodeSwarmConfig.MAX_THREADS_KEY,4);
		if (maxBackgroundThreads <= 0) {
			maxBackgroundThreads = 4;
		}
		backgroundExecutor = new ThreadPoolExecutor(1, maxBackgroundThreads, Long.MAX_VALUE, TimeUnit.NANOSECONDS, new ArrayBlockingQueue<Runnable>(4 * maxBackgroundThreads), new ThreadPoolExecutor.CallerRunsPolicy());

		if (cfg.getBooleanProperty(CodeSwarmConfig.USE_OPEN_GL, false)) {
			size(width, height, OPENGL);
		} else {
			size(width, height);
		}

		showLegend = cfg.getBooleanProperty(CodeSwarmConfig.SHOW_LEGEND, false);
		showHistogram = cfg.getBooleanProperty(CodeSwarmConfig.SHOW_HISTORY, false);
		showDate = cfg.getBooleanProperty(CodeSwarmConfig.SHOW_DATE, false);
		showEdges = cfg.getBooleanProperty(CodeSwarmConfig.SHOW_EDGES, false);
		showDebug = cfg.getBooleanProperty(CodeSwarmConfig.SHOW_DEBUG, false);
		takeSnapshots = cfg.getBooleanProperty(CodeSwarmConfig.TAKE_SNAPSHOTS_KEY, false);
		drawNamesSharp = cfg.getBooleanProperty(CodeSwarmConfig.DRAW_NAMES_SHARP, true);
		drawNamesHalos = cfg.getBooleanProperty(CodeSwarmConfig.DRAW_NAMES_HALOS, false);
		drawFilesSharp = cfg.getBooleanProperty(CodeSwarmConfig.DRAW_FILES_SHARP, false);
		drawFilesFuzzy = cfg.getBooleanProperty(CodeSwarmConfig.DRAW_FILES_FUZZY, true);
		drawFilesJelly = cfg.getBooleanProperty(CodeSwarmConfig.DRAW_FILES_JELLY, false);
		background = cfg.getBackground().getRGB();

		UPDATE_DELTA = cfg.getIntProperty(CodeSwarmConfig.MSEC_PER_FRAME_KEY, -1);
		if (UPDATE_DELTA == -1) {
			int framesperday = cfg.getIntProperty(CodeSwarmConfig.FRAMES_PER_DAY_KEY, 4);
			if (framesperday > 0) {
				UPDATE_DELTA = (86400000 / framesperday);
			}
		}
		if (UPDATE_DELTA <= 0) {
			// Default to 4 frames per day.
			UPDATE_DELTA = 21600000;
		}

		isInputSorted = cfg.getBooleanProperty(CodeSwarmConfig.IS_INPUT_SORTED_KEY, false);

		/**
		 * This section loads config files and calls the setup method for all physics engines.
		 */

		physicsEngineConfigDir = cfg.getStringProperty( CodeSwarmConfig.PHYSICS_ENGINE_CONF_DIR, "physics_engine");
		File f = new File(physicsEngineConfigDir);
		String[] configFiles = null;
		if ( f.exists()  &&  f.isDirectory() ) {
			configFiles = f.list();
		}
		for (int i=0; configFiles != null  &&  i<configFiles.length; i++) {
			if (configFiles[i].endsWith(".config")) {
				Properties p = new Properties();
				String ConfigPath = physicsEngineConfigDir + System.getProperty("file.separator") + configFiles[i];
				try {
					p.load(new FileInputStream(ConfigPath));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					System.exit(1);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
				String ClassName = p.getProperty("name", "__DEFAULT__");
				if ( ! ClassName.equals("__DEFAULT__")) {
					PhysicsEngine pe = getPhysicsEngine(ClassName);
					pe.setup(p);
					pe.setName(ClassName);
					mPhysicsEngineChoices.add(pe);
				} else {
					System.out.println("Skipping config file '" + ConfigPath + "'.  Must specify class name via the 'name' parameter.");
					System.exit(1);
				}
			}
		}

		if (mPhysicsEngineChoices.size() == 0) {
			System.out.println("No physics engine config files found in '" + physicsEngineConfigDir + "'.");
			System.exit(1);
		}

		// Physics engine configuration and instantiation
		physicsEngineSelection = cfg.getStringProperty( CodeSwarmConfig.PHYSICS_ENGINE_SELECTION, PHYSICS_ENGINE_LEGACY );

		for (PhysicsEngine p : mPhysicsEngineChoices) {
			if (physicsEngineSelection.equals(p.getName())) {
				mPhysicsEngine = p;
			}
		}

		if (mPhysicsEngine == null) {
			System.out.println("No physics engine matches your choice of '" + physicsEngineSelection + "'. Check '" + physicsEngineConfigDir + "' for options.");
			System.exit(1);
		}

		smooth();
		frameRate(FRAME_RATE);

		// init data structures
		nodes       = new CopyOnWriteArrayList<FileNode>();
		edges       = new CopyOnWriteArrayList<Edge>();
		people      = new CopyOnWriteArrayList<PersonNode>();
		history     = new LinkedList<ColorBins>();

		if (isInputSorted) {
			//If the input is sorted, we only need to store the next few events
			eventsQueue = new ArrayBlockingQueue<FileEvent>(5000);
		} else {
			//Otherwise we need to store them all at once in a data structure that will sort them
			eventsQueue = new PriorityBlockingQueue<FileEvent>();
		}

		// Init color map
		initColors();

		loadRepEvents(cfg.getStringProperty(CodeSwarmConfig.INPUT_FILE_KEY)); // event formatted (this is the standard)
		while (!finishedLoading && eventsQueue.isEmpty());
		prevDate = eventsQueue.peek().getDate();

		SCREENSHOT_FILE = cfg.getStringProperty(CodeSwarmConfig.SNAPSHOT_LOCATION_KEY);

		maxFramesSaved = (int) Math.pow(10, SCREENSHOT_FILE.replaceAll("[^#]","").length());

		// Create fonts
		String fontName = cfg.getStringProperty(CodeSwarmConfig.FONT_KEY,"SansSerif");
		String fontNameBold = cfg.getStringProperty(CodeSwarmConfig.FONT_KEY_BOLD,"SansSerif");
		Integer fontSize = cfg.getIntProperty(CodeSwarmConfig.FONT_SIZE, 10);
		Integer fontSizeBold = cfg.getIntProperty(CodeSwarmConfig.FONT_SIZE_BOLD, 14);
		font = createFont(fontName, fontSize);
		boldFont = createFont(fontNameBold, fontSizeBold);

		textFont(font);

		// Create the file particle image
		sprite = loadImage(cfg.getStringProperty(CodeSwarmConfig.SPRITE_FILE_KEY,"particle.png"));
		// Add translucency (using itself in this case)
		sprite.mask(sprite);
	}

	/**
	 * Load a colormap
	 */
	public void initColors() {
		colorAssigner = new ColorAssigner();
		int i = 1;
		String property;
		while ((property = cfg.getColorAssignProperty(i)) != null) {
			ColorTest ct = new ColorTest();
			ct.loadProperty(property);
			colorAssigner.addRule(ct);
			i++;
		}
		// Load the default.
		ColorTest ct = new ColorTest();
		ct.loadProperty(CodeSwarmConfig.DEFAULT_COLOR_ASSIGN);
		colorAssigner.addRule(ct);
	}

	/**
	 * Main loop
	 */
	public void draw() {
		long start = System.currentTimeMillis();
		background(background); // clear screen with background color

		this.update(); // update state to next frame

		// Draw edges (for debugging only)
		if (showEdges) {
			for (Edge edge : edges) {
				edge.draw();
			}
		}

		// Surround names with aura
		// Then blur it
		if (drawNamesHalos) {
			drawPeopleNodesBlur();
		}

		// Then draw names again, but sharp
		if (drawNamesSharp) {
			drawPeopleNodesSharp();
		}

		// Draw file particles
		for (FileNode node : nodes) {
			node.draw();
		}

		textFont(font);

		// Show the physics engine name
		if (showEngine) {
			drawEngine();
		}

		// help, legend and debug information are exclusive
		if (showHelp) {
			// help override legend and debug information
			drawHelp();
		}
		else if (showDebug) {
			// debug override legend information
			drawDebugData();
		}
		else if (showLegend) {
			// legend only if nothing "more important"
			drawLegend();
		}

		if (showPopular) {
			drawPopular();
		}

		if (showHistogram) {
			drawHistory();
		}

		if (showDate) {
			drawDate();
		}

		if (takeSnapshots) {
			dumpFrame();
		}

		// Stop animation when we run out of data AND all nodes are dead
		if (eventsQueue.isEmpty()) {
			coolDown = true;
			if ( !isThereLife() ) {
				// noLoop();
				backgroundExecutor.shutdown();
				try {
					backgroundExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
				} catch (InterruptedException e) { /* Do nothing, just exit */}
				exit();
			}
		}

		long end = System.currentTimeMillis();
		lastDrawDuration = end - start;
	}


	/**
	 * Surround names with aura
	 */
	public void drawPeopleNodesBlur() {
		colorMode(HSB);
		// First draw the name
		for (PersonNode p : people) {
			fill(hue(p.getFlavor()), 64, 255, p.getLife());
			p.draw();
		}

		// Then blur it
		filter(BLUR, 3);
	}

	/**
	 * Draw person's name
	 */
	public void drawPeopleNodesSharp() {
		colorMode(RGB);
		for (int i = 0; i < people.size(); i++) {
			PersonNode p = people.get(i);
			fill(lerpColor(p.getFlavor(), color(255), 0.5f), max(p.getLife() - 50, 0));
			p.draw();
		}
	}

	/**
	 * Draw date in lower-right corner
	 */
	public void drawDate() {
		fill(255);
		String dateText = formatter.format(prevDate);
		textAlign(RIGHT, BASELINE);
		textSize(font.size);
		if (coolDown)
			dateText = "End of history: " + dateText;
		text(dateText, width - 1, height - textDescent());
	}

	/**
	 *  Draw histogram in lower-left
	 */
	public void drawHistory() {
		int counter = 0;
		strokeWeight(1);

		for (ColorBins cb : history) {
			for (int i = 0; i < cb.getNum(); i++) {
				int c = cb.getColorList()[i];
				stroke(c, 200);
				point(counter, height - i - 3);
			}
			counter++;
		}
	}

	/**
	 * Show the Loading screen.
	 */

	public void drawLoading() {
		noStroke();
		textFont(font, 20);
		textAlign(LEFT, TOP);
		fill(255, 200);
		text(loadingMessage, 0, 0);
	}

	/**
	 *  Show color codings
	 */
	public void drawLegend() {
		noStroke();
		textFont(font);
		textAlign(LEFT, TOP);
		fill(255, 200);
		text("Legend:", 0, 0);
		for (int i = 0; i < colorAssigner.getTests().size(); i++) {
			ColorTest t = colorAssigner.getTests().get(i);
			fill(t.getC1(), 200);
			text(t.getLabel(), font.size, (i + 1) * font.size);
		}
	}

	/**
	 *  Show physics engine name
	 */
	public void drawEngine() {
		fill(255);
		textAlign(RIGHT, BASELINE);
		textSize(10);
		text(physicsEngineSelection, width-1, height - (textDescent() * 5));
	}

	/**
	 *  Show short help on available commands
	 */
	public void drawHelp() {
		int line = 0;
		noStroke();
		textFont(font);
		textAlign(LEFT, TOP);
		fill(255, 200);
		text("Help on keyboard commands:", 0, 10*line++);
		text("space bar : pause", 0, 10*line++);
		text("           a : show name halos", 0, 10*line++);
		text("           b : show debug", 0, 10*line++);
		text("           d : show date", 0, 10*line++);
		text("           e : show edges", 0, 10*line++);
		text("           E : show physics engine name", 0, 10*line++);
		text("            f : draw files fuzzy", 0, 10*line++);
		text("           h : show histogram", 0, 10*line++);
		text("            j : draw files jelly", 0, 10*line++);
		text("            l : show legend", 0, 10*line++);
		text("           p : show popular", 0, 10*line++);
		text("           q : quit code_swarm", 0, 10*line++);
		text("           s : draw names sharp", 0, 10*line++);
		text("           S : draw files sharp", 0, 10*line++);
		text("   minus : previous physics engine", 0, 10*line++);
		text("      plus : next physics engine", 0, 10*line++);
		text("           ? : show help", 0, 10*line++);
	}
	/**
	 *  Show debug information about all drawable objects
	 */
	public void drawDebugData() {
		noStroke();
		textFont(font);
		textAlign(LEFT, TOP);
		fill(255, 200);
		text("Nodes: " + nodes.size(), 0, 0);
		text("People: " + people.size(), 0, 10);
		text("Queue: " + eventsQueue.size(), 0, 20);
		text("Last render time: " + lastDrawDuration, 0, 30);
	}

	/**
	 * TODO This could be made to look a lot better.
	 */
	public void drawPopular() {
		CopyOnWriteArrayList <FileNode> al=new CopyOnWriteArrayList<FileNode>();
		noStroke();
		textFont(font);
		textAlign(RIGHT, TOP);
		fill(255, 200);
		text("Popular Nodes (touches):", width-120, 0);
		for (int i = 0; i < nodes.size(); i++) {
			FileNode fn = nodes.get(i);
			if (fn.qualifies()) {
				// Insertion Sort
				if (al.size() > 0) {
					int j = 0;
					for (; j < al.size(); j++) {
						if (fn.compareTo(al.get(j)) <= 0) {
							continue;
						} else {
							break;
						}
					}
					al.add(j,fn);
				} else {
					al.add(fn);
				}
			}
		}

		int i = 1;
		ListIterator<FileNode> it = al.listIterator();
		while (it.hasNext()) {
			FileNode n = it.next();
			// Limit to the top 10.
			if (i <= 10) {
				text(n.getName() + "  (" + n.getTouches() + ")", width-100, 10 * i++);
			} else if (i > 10) {
				break;
			}
		}
	}

	/**
	 * @param name
	 * @return physics engine instance
	 */
	@SuppressWarnings("unchecked")
	public PhysicsEngine getPhysicsEngine(String name) {
		PhysicsEngine pe = null;
		try {
			Class<PhysicsEngine> c = (Class<PhysicsEngine>)Class.forName(name);
			Class partypes[] = {code_swarm.class};
			Constructor<PhysicsEngine> peConstructor = c.getConstructor(partypes);
			Object arglist[] = {this};
			pe = peConstructor.newInstance(arglist);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		return pe;
	}

	/**
	 * @return list of people whose life is > 0
	 */
	public static Iterable<PersonNode> getLivingPeople() {
		return filterLiving(people);
	}

	/**
	 * @return list of edges whose life is > 0
	 */
	public static Iterable<Edge> getLivingEdges() {
		return filterLiving(edges);
	}

	/**
	 * @return list of file nodes whose life is > 0
	 */
	public static Iterable<FileNode> getLivingNodes() {
		return filterLiving(nodes);
	}

	private static <T extends Drawable> Iterable<T> filterLiving(Iterable<T> iter) {
		ArrayList<T> livingThings = new ArrayList<T>();
		for (T thing : iter)
			if (thing.isAlive())
				livingThings.add(thing);
		return livingThings;
	}

	/**
	 *  Take screenshot
	 */
	public void dumpFrame() {
		if (frameCount < maxFramesSaved) {
			final String outputFileName = insertFrame(SCREENSHOT_FILE);
			final PImage image = get();

			backgroundExecutor.execute(new Runnable() {
				public void run() {
					image.save(new File(outputFileName).getAbsolutePath());
				}
			});
			//  saveFrame(SCREENSHOT_FILE);
		}
	}

	/**
	 *  Update the particle positions
	 */
	public void update() {
		// Create a new histogram line
		ColorBins cb = new ColorBins();
		history.add(cb);

		nextDate = new Date(prevDate.getTime() + UPDATE_DELTA);
		currentEvent = eventsQueue.peek();

		while (currentEvent != null && currentEvent.getDate().before(nextDate)) {
			if (finishedLoading) {
				currentEvent = eventsQueue.poll();
				if (currentEvent == null)
					return;
			}
			else {
				try {
					currentEvent = eventsQueue.take();
				} catch (InterruptedException e) {
					System.out.println("Interrupted while fetching current event from eventsQueue");
					e.printStackTrace();
					continue;
				}
			}

			FileNode n = findNode(currentEvent.getPath() + currentEvent.getFilename());
			if (n == null) {
				float FILE_MASS = cfg.getFloatProperty(CodeSwarmConfig.FILE_MASS_KEY,1.0f);
				n = new FileNode(currentEvent, FILE_MASS, mPhysicsEngine.pStartLocation(), mPhysicsEngine.pStartVelocity(FILE_MASS), this);
				nodes.add(n);
			} else {
				n.freshen(currentEvent);
			}

			// add to color bin
			cb.add(n.getNodeHue());

			PersonNode p = findPerson(currentEvent.getAuthor());
			if (p == null) {
				float PERSON_MASS = cfg.getFloatProperty(CodeSwarmConfig.PERSON_MASS_KEY,1.0f);
				p = new PersonNode(currentEvent.getAuthor(), color(0), PERSON_MASS, mPhysicsEngine.pStartLocation(), mPhysicsEngine.pStartVelocity(PERSON_MASS), this);
				people.add(p);
			} else {
				p.freshen();
			}
			p.addColor(n.getNodeHue());

			Edge ped = findEdge(n, p);
			if (ped == null) {
				ped = new Edge(n, p, this);
				edges.add(ped);
			} else
				ped.freshen();

			/*
			 * if ( currentEvent.date.equals( prevDate ) ) { 
			 * 	Edge e = findEdge( n, prevNode); 
			 * 	if ( e == null ) { 
			 * 		e = new Edge( n, prevNode ); 
			 * 		edges.add( e ); 
			 * 	} else {
			 * 		e.freshen(); 
			 * 	} 
			 * }
			 */

			//      prevDate = currentEvent.date;
			//      prevNode = n;
			if (finishedLoading)
				currentEvent = eventsQueue.peek();
			else {
				while (eventsQueue.isEmpty());
				currentEvent = eventsQueue.peek();
			}
		}

		if ( !coolDown )
			prevDate = nextDate;

		// sort colorbins
		cb.sort();

		// restrict history to drawable area
		while (history.size() > 320)
			history.remove();

		// Do not allow toggle Physics Engine yet.
		safeToToggle = false;

		// Init frame:
		mPhysicsEngine.initializeFrame();

		Iterable<Edge> livingEdges = getLivingEdges();
		Iterable<FileNode> livingNodes = getLivingNodes();
		Iterable<PersonNode> livingPeople = getLivingPeople();

		// update velocity
		for (Edge edge : livingEdges) {
			mPhysicsEngine.onRelaxEdge(edge);
		}

		// update velocity
		for (FileNode node : livingNodes) {
			mPhysicsEngine.onRelaxNode(node);
		}

		// update velocity
		for (PersonNode person : livingPeople) {
			mPhysicsEngine.onRelaxPerson(person);
		}

		// update position
		for (Edge edge : livingEdges) {
			mPhysicsEngine.onUpdateEdge(edge);
		}

		// update position
		for (FileNode node : livingNodes) {
			mPhysicsEngine.onUpdateNode(node);
		}

		// update position
		for (PersonNode person : livingPeople) {
			mPhysicsEngine.onUpdatePerson(person);
		}

		// Finalize frame:
		mPhysicsEngine.finalizeFrame();

		safeToToggle = true;
		if (wantToToggle == true) {
			switchPhysicsEngine(toggleDirection);
		}
	}

	/**
	 * Checks the node list for signs of life.
	 * @return Does life exist?
	 */
	public boolean isThereLife() {
		for (FileNode node : nodes) {
			if (node.getLife() > 0)
				return true;
		}
		return false;
	}

	/**
	 * Searches the nodes array for a given name
	 * @param name
	 * @return FileNode with matching name or null if not found.
	 */
	public FileNode findNode(String name) {
		for (FileNode node : nodes) {
			if (node.getName().equals(name))
				return node;
		}
		return null;
	}

	/**
	 * Searches the nodes array for a given name
	 * @param n1 From
	 * @param n2 To
	 * @return Edge connecting n1 to n2 or null if not found
	 */
	public Edge findEdge(Node n1, Node n2) {
		for (Edge edge : edges) {
			if (edge.getNodeFrom() == n1 && edge.getNodeTo() == n2)
				return edge;
		}
		return null;
	}

	/**
	 * Searches the people array for a given name.
	 * @param name
	 * @return PersonNode for given name or null if not found.
	 */
	public PersonNode findPerson(String name) {
		for (PersonNode p : people) {
			if (p.getName().equals(name))
				return p;
		}
		return null;
	}

	/**
	 *  Load the standard event-formatted file.
	 *  @param filename
	 */
	public void loadRepEvents(String filename) {
		final String fullFilename = filename;

		XMLQueueLoader eventLoader = new XMLQueueLoader(fullFilename, eventsQueue, isInputSorted);
		eventLoader.addTaskListener(this);

		if (isInputSorted)
			backgroundExecutor.execute(eventLoader);
		else
			//we have to load all of the data before we can continue if it isn't sorted
			eventLoader.run();
	}

	/*
	 * Output file events for debugging void printQueue() { while(
	 * eventsQueue.size() > 0 ) { FileEvent fe = (FileEvent)eventsQueue.poll();
	 * println( fe.date ); } }
	 */

	/**
	 * @note Keystroke callback function
	 */
	public void keyPressed() {
		switch (key) {
		case ' ': {
			pauseButton();
			break;
		}
		case 'a': {
			drawNamesHalos = !drawNamesHalos;
			break;
		}
		case 'b': {
			showDebug = !showDebug;
			break;
		}
		case 'd': {
			showDate = !showDate;
			break;
		}
		case 'e' : {
			showEdges = !showEdges;
			break;
		}
		case 'E' : {
			showEngine = !showEngine;
			break;
		}
		case 'f' : {
			drawFilesFuzzy = !drawFilesFuzzy;
			break;
		}
		case 'h': {
			showHistogram = !showHistogram;
			break;
		}
		case 'j' : {
			drawFilesJelly = !drawFilesJelly;
			break;
		}
		case 'l': {
			showLegend = !showLegend;
			break;
		}
		case 'p': {
			showPopular = !showPopular;
			break;
		}
		case 'q': {
			exit();
			break;
		}
		case 's': {
			drawNamesSharp = !drawNamesSharp;
			break;
		}
		case 'S': {
			drawFilesSharp = !drawFilesSharp;
			break;
		}
		case '-': {
			wantToToggle = true;
			toggleDirection = false;
			break;
		}
		case '+': {
			wantToToggle = true;
			toggleDirection = true;
			break;
		}
		case '?': {
			showHelp = !showHelp;
			break;
		}
		}
	}

	/**
	 * Method to switch between Physics Engines
	 * @param direction Indicates whether or not to go left or right on the list
	 */
	public void switchPhysicsEngine(boolean direction) {
		if (mPhysicsEngineChoices.size() > 1 && safeToToggle) {
			boolean found = false;
			for (int i = 0; i < mPhysicsEngineChoices.size() && !found; i++) {
				if (mPhysicsEngineChoices.get(i) == mPhysicsEngine) {
					found = true;
					wantToToggle = false;
					if (direction == true) {
						if ((i+1) < mPhysicsEngineChoices.size()) {
							mPhysicsEngine=mPhysicsEngineChoices.get(i+1);
							physicsEngineSelection=mPhysicsEngineChoices.get(i+1).getName();
						} else {
							mPhysicsEngine=mPhysicsEngineChoices.get(0);
							physicsEngineSelection=mPhysicsEngineChoices.get(0).getName();
						}
					} else {
						if ((i-1) >= 0) {
							mPhysicsEngine=mPhysicsEngineChoices.get(i-1);
							physicsEngineSelection=mPhysicsEngineChoices.get(i-1).getName();
						} else {
							mPhysicsEngine=mPhysicsEngineChoices.get(mPhysicsEngineChoices.size()-1);
							physicsEngineSelection=mPhysicsEngineChoices.get(mPhysicsEngineChoices.size()-1).getName();
						}
					}
				}
			}
		}
	}

	/**
	 *  Toggle pause
	 */
	public void pauseButton() {
		if (looping)
			noLoop();
		else
			loop();
		looping = !looping;
	}

	/**
	 * Draws a point.
	 * @param x
	 * @param y
	 * @param red
	 * @param green
	 * @param blue
	 */
	public void drawPoint (int x, int y, int red, int green, int blue) {
		noStroke();
		colorMode(RGB);
		stroke(red, green, blue);
		point(x, y);
	}

	/**
	 * Draws a line.
	 * @param fromX
	 * @param fromY
	 * @param toX
	 * @param toY
	 * @param red
	 * @param green
	 * @param blue
	 */
	public void drawLine (int fromX, int fromY, int toX, int toY, int red, int green, int blue) {
		noStroke();
		colorMode(RGB);
		stroke(red, green, blue);
		strokeWeight(1.5f);
		line(fromX, fromY, toX, toY);
	}

	/**
	 * Returns the height of the code swarm component.
	 * 
	 * @return int height of the code swarm component
	 */
	public static int getCodeSwarmHeight(){
		return height;
	}

	/**
	 * Returns the width of the code swarm component.
	 * 
	 * @return int width of the code swarm component
	 */
	public static int getCodeSwarmWidth(){
		return width;
	}

	public static CodeSwarmConfig getConfig(){
		return cfg;
	}

	/**
	 * code_swarm Entry point.
	 * @param args : should be the path to the config file
	 */
	public static void main(String args[]) {
		try {
			if (args.length > 0) {
				System.out.println("code_swarm is free software: you can redistribute it and/or modify");
				System.out.println("it under the terms of the GNU General Public License as published by");
				System.out.println("the Free Software Foundation, either version 3 of the License, or");
				System.out.println("(at your option) any later version.");
				System.out.flush();
				cfg = new CodeSwarmConfig(args[0]);
				PApplet.main(new String[] { "code_swarm" });
			} else {
				System.err.println("Specify a config file.");
			}
		} catch (IOException e) {
			System.err.println("Failed due to exception: " + e.getMessage());
		}
	}
	/**
	 * the alternative entry-point for code_swarm. It gets called from
	 * {@link MainView} after fetching the repository log.
	 * @param config the modified config
	 *        (it's InputFile-property has been changed to reflect the
	 *        fetched repository-log)
	 */
	public static void start(CodeSwarmConfig config){
		cfg = config;
		PApplet.main(new String[]{"codeswarm.code_swarm"});
	}

	@Override
	public void fireTaskDoneEvent() {
		finishedLoading = true;
	}

	public static PFont getBoldPFont(){
		return boldFont;
	}

	public static PFont getPFont(){
		return font;
	}

	public PImage getSprite(){
		return sprite;
	}

	public boolean isDrawFilesSharp(){
		return drawFilesSharp;
	}

	public boolean isDrawFilesFuzzy(){
		return drawFilesFuzzy;
	}

	public boolean isDrawFilesJelly(){
		return drawFilesJelly;
	}

	public boolean isShowPopular(){
		return showPopular;
	}

	public ColorAssigner getColorAssigner(){
		return colorAssigner;
	}
}
