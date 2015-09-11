# Introduction #

This guide explains how to setup your development environment to build the binary (ie. jar) version of code\_swarm from Java sources.

  * Target audience : Explanations require at least an understanding of computing and of your Operating System, even if no Java skills are needed.
  * Target environment : For now on, Linux Ubuntu 8.04 and Windows XP are described, but would not be difficult to adapt for other Linux/Unix systems, like for Mac.

# Setting up the environment #

You will need a working **Java Runtime Environment** the **Ant** software building tool, and the **Java SDK (JDK)** from Sun _(no idea if other SDK could do the job)_.

## Linux ##
_This guide is written with a Debian based Linux, Ubuntu 8.04._ It would requires some minor adaptation to use on other distribution (other packaging systems).

  * install ant with the following command (or with your favorite graphical package manager)
```
sudo apt-get install ant
```
  * install sun-java-jdk 1.5 or 1.6 following your distribution recommendation
```
sudo apt-get install sun-java6-jdk
```
  * configure the Java SDK to specify the new installation path, for instance on Ubuntu :
```
sudo update-java-alternatives -s java-6-sun
```

See http://doc.ubuntu-fr.org/java

## Windows ##
This guide as been tested with Windows XP SP3.

  * download ant for all platforms at http://ant.apache.org/bindownload.cgi
  * unpack it where you want it to be installed, and add the location of its binaries to the the "PATH" environment variable. For instance, add at the end :
```
"C:\apache-ant-1.7.0\bin;" 
```
  * download Sun Java SDK at http://java.sun.com/javase/downloads/index.jsp
  * install it and add the "javac" Java compiler to the PATH :
```
"C:\Program Files\Java\jdk1.6.0_06\bin;"
```
  * then create a new environment variable called JAVA\_HOME and set its paht to
```
"C:\Program Files\Java\jdk1.6.0_06;"
```

# Getting the sources #

code\_swarm sources are under a Google Code Subversion repository (svn). They can simply be browsed by your current web browser, but you would prefer a more dedicated tool to get the full source tree and maintain it up to date.

See [Subversion homepage on tigris](http://subversion.tigris.org/) for all appropriate tools and documents. I would recommend "TortoiseSVN" for Windows users, native "Subversion" package would do for Unix-like users.

See the "Source" tab for further instruction on where to browse and checkout the sources.

# Building the sources #

Quick build :
  * Open a terminal/a command line shell, change directory to the root of the code\_swarm source folder, and to launch the build, type
```
ant
```
  * Then to launch code\_swarm use
```
ant run
```
> _Notice that you can type only the second command to do both in once_

Alternatively, you can just try the "run.sh" or "run.bat" scripts to get it done in once, but need to rebuild manually typing "ant" if you modify sources

Other commands :
  * To generate also the Javadoc HTML sources documentation
```
ant all
```

  * To delete all intermediate and binary files
```
ant clean
```

# Using Eclipse #

**Linux users:  You must use Sun's java vm.  The GNU one doesn't work.**
## If you don't have Eclipse installed ##
  * Download the latest Eclipse IDE currently Ganymede.  http://www.eclipse.org/downloads/
    * The Classic or Java Developer version is fine.
  * Eclipse unzips into it's own directory and can be run from there.  It doesn't have to be placed anywhere special.  This is very useful when testing new releases of Eclipse.

## Ensure you have the SVN interface installed ##
  * Help -> Software Updates
  * Click the Available Software tab.
  * Click Add Site...
  * Enter: http://www.polarion.org/projects/subversive/download/eclipse/2.0/update-site/

  * Expand Ganymede and then Collaboration Tools.  Select SVN Team Provider.
  * Expand the polarion.org one and then Subversive SVN Connectors.  Select SVNKit.

  * Click Install... and Follow prompts from there.

## After getting SVN ##

  * Open a new project.  (Just the Project... one, not one of the others.)
    * Expand SVN and choose Projects from SVN.
    * Ensure that Create a new repository location is selected and click next.
    * Enter the URL provided by Google and select Use a custom label.
    * Enter codeswarm for the label if you are helping with the project codeswarm-read-only otherwise.
    * Under Authentication, enter nothing if using codeswarm-read-only.  If you are helping with the project, enter your google account name and the google code password that was generated for you.
    * Click Yes if it asks you to normalize the URL.
    * Select trunk and click Finish.
    * Leave 'Check out as a project configured using the New Project Wizard' selected.
    * Click Finish.
  * Type Java Project in the filter box and select Java Project, Click Next.
    * For project name, use Code Swarm.
    * Leave the rest alone.  Click Finish.

  * Under Project select Properties.
  * Select Java Build Path.
  * Click Add Folder...
  * Expand trunk and click the box next to src.
  * Click OK.
  * Select Included: and click Edit.
  * Click Add...
  * Enter `**/*.java` and click OK.
  * Click Finish.
  * Select the Libraries Tab.
  * Click Add Jars...
  * Select all in the lib directory and click Ok.
  * Select Run/Debug Settings and click New...
  * Select Java Application and click OK.
  * For the Name, enter Code Swarm.
  * Click Search next to Main class.
  * Select code\_swarm and click OK.
  * Select the Arguments tab.
  * For the Program arguments, enter data/sample.config
  * For the VM arguments, put -Xmx1000m  (this is for 1Gb of memory, use less if you need to, 512 works well for most.)
  * Click OK. Until Properties is closed.

The green play button should build and launch the app.  Enjoy.


# See also #
  * [FAQ](FAQ.md)
  * GeneralPlan