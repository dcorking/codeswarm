**_The Processing prototype will not be developed or supported by this project. It is kept in tags for historical reference. Use at your own risk._**

**Go to http://processing.org/learning for help with the Processing language.**

## Source Files ##
  * code\_swarm.pde - Main
  * Node.pde - abstract entities which move around
  * PersonNode.pde - represents a person
  * FileNode.pde - represents a file
  * FileEvent.pde - a file committed at a certain time by a certain person
  * Edge.pde - connects Nodes and pulls them together
  * ColorAssigner.pde - maps files to colors using regexes
  * ColorBins.pde - stores histogram bar colors

So far only code\_swarm.pde has been cleaned (mildly) and commented.

## Data ##

The files I use as input, which contain the commit events, are in event format.
  * Event-formatted files are flat and sorted by time. `loadRepEvents()`
  * Use `/code_swarm/data/sample-repevents.xml` to see what it looks like.

There are other formats supported in the prototype, but these are deprecated.

## Configuring code\_swarm.pde ##

You will need to make a few changes inside code\_swarm.pde in order to suit your program :

  * **int WIDTH = 640;** defines the width of the generated screenshots. This is the recommended value (below that, graphics start to get over one another).
  * **int HEIGHT = 480;** defines the height of the generated screenshots. This is the recommended value.
  * **int FRAME\_RATE = 24;** defines the frame rate (how many images per second in the resulting video to get a smooth animation. 24 is a good value.
  * **String INPUT\_FILE = "log.xml";** should be changed to whatever your log file's name is (in the example above, it was **activity.xml**)
  * **long dateSkipper** defines the frequency to which a new image is shown. By default, there are 4 images per day (6\*60\*60\*1000, or one every 6 hours). You could change that to every 2 hours if you have a very active project using: 2\*60\*60\*1000.
  * **boolean takeSnapshots** has to be true if you want to generate screenshots, which is necessary at this point
  * **boolean showLegend** (around line 60) could be set to true if you want a color legend to be shown in the top-left of your screenshots. So far, the legend is a big ugly as it doesn't translate the regular expressions in a human way, but still it helps understand what is what.

## Executing code\_swarm ##
Once you are ready, just click the "play/run" button and wait. This operation could take up to several hours for large activity logs. To give a rough idea, it takes 2 hours using one processor of a dual-core 1.3GHz for an activity log of about 15000 commits (spread over 4 years).

## Executing for PHP project ##
For PHP projects (or any language that is not currently supported) you will need to:

  1. add a **phpColors();** call to the **initColors()** function (around line 110) and comment out the current **eclipseColors();** call
  1. create a phpColors() function that will look like this:
```
  void phpColors() {
    //code (red)
    colorAssigner.addRule( ".*\\.php", color(0,255,255), color(15,255,255) );

    //documentation (blue)
    colorAssigner.addRule( ".*/documentation/.*|.*/lang/.*|.*\\.html|.*\\.htm", color(150,255,255), color(170,255,255) );

    //media (turquoise)
    colorAssigner.addRule( ".*\\.gif|.*\\.jpg|.*\\.jpeg|.*\\.png|.*\\.css|.*\\.swf", color(120,255,255), color(135,255,255) );

    //alternative code (orange)
    colorAssigner.addRule( ".*\\.js|.*\\.jar|.*\\.war|.*\\.java|.*\\.class|.*\\.lzx", color(25,255,255), color(40,255,255) );

    //anything else (purple)
    colorAssigner.addRule( ".*", color(200,255,255), color(215,255,255) );
  }
```

Be careful that the color code is very tricky. We might need more information here, but the basic idea is that it's not a normal RGB code and it's not either a RGB color code mask. I added comments before every line so you can get an idea of what to use for at least five colors.

## Assigning Colors ##
The `ColorAssigner` object is created at construction time and is a flexible way to color your file nodes. It's `addRule()` method takes one regular expression and two colors, essentially defining a map of file path to color range. During the event loop, when files are introduced to the system, the `ColorAssigner.getColor()` method looks at the files's path and tries to match it with a regular expression in its list.  The `getColor()` method then returns a randomly chosen color from a linear interpolation of the range (in RGB space).
Caveats:
  * If the file does not match any regular expression in the list, a default color is returned.
  * If a file path matches multiple regular expressions in the list, the one added first is chosen.
  * Colors may be easier to _assign_ in HSB space. They are always interpolated in RGB space.

## Running ##
Download and run the Processing IDE from http://processing.org. Open code\_swarm.pde.