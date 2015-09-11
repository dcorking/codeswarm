# Audience #
In order of priority:
  1. Desktop systems using the command line. Simple and modular.
  1. Inexperienced users who need a GUI. Easy to use.
  1. Large projects (like Linux and Wikipedia) needing lots of compute power. Fast and scalable.

# Use Cases #

I see two cases where people will use code\_swarm.  One is a casual, exploratory look at how a project is structured and who is working on what.

  1. Exploration: Viewing the history of a project.
    * Real-time rending is critical.
    * The less setup and configuration necessary the better. Sensible defaults
  1. Rendering: Creating a video of a project's history for sharing with others.
    * Expose all of the options, so that a user can customize the visualization to their liking
    * Speed is nice, but not paramount

# Data Input #
  * A standard XML schema for this project.
    * I suggest we base it around the loadRepEvents() format. - michael.ogawa
      * I'd suggest to create a data-access-object for the loadRepEvents-format. That way, the format can be altered (enhanced) in a simple way. Furthermore a well-defined data-access-interface could enable different input-formats by using different daos. - kraeusen
  * A collection of simple, stand-alone tools (in Python?) to convert originating data to the standard format.
    * Python would work great for this purpose, I can put together a small tool and you can be the judge if it is heading in the right direction. - cgalvan
    * I think it's not a good idea to require the user to have several environments installed to run codeswarm. IMHO it would be great if the whole application would simply depend on a jre. (Combined with a gui and binary releases, codeswarm would be an easy to install and easy to use application). - kraeusen
  * Do NOT support multiple input formats to the visualizer. Only allow input of the standard format.

## Data Wishlist ##
  * ~~svn~~
  * ~~cvs~~
  * ~~git~~
  * ~~MediaWiki~~
  * bzr

# Visualizer #
  * Use the Processing library for now as an import, but make application independent of the P IDE gradually.
    * This implies using Java.
  * The ONLY program input will be a config file. It will contain:
    * Path to RepEvents file
    * Color assignment rules
    * Whether and where to save frames
  * The config file can be created with a text editor or a GUI front end.
  * Why only a text file as input? This ensures that the color rules will be saved, as well as decouple the GUI from the visualization.
    * ~~I'd suggest to switch the file-format to standard java properties file. That way, it would be easy to load and store the config as well as to edit it in a text-editor (with the limitation that some characters would have to be escaped). - kraeusen~~ Done.
  * ~~It would be great if the visualization would not simply stop when the last repository check-in is logged, but fade out (to the future) until no person or file is "alive" anymore. - kraeusen~~ Done.

# Application code #
  * code\_swarm.java needs to be split into several java-files and some cleanup is required. I believe that more developers would contribute if the code would be easier to understand. -kraeusen
  * I would love to create a nice looking easy to use Swing-GUI for codeswarm, but that requires some code-cleanup in the first place. I'd prefer using Netbeans instead of Eclipse for that. - kraeusen