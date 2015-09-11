# Introduction #

There are several ways to generate a video:

  * if you want to access a subversion repository, simply run **`runrepositoryfetch.bat`** on windows systems or **`runrepository.sh`** on linux (linux support for this way has not been tested yet).
A dialog will show up, asking you for the repository url, your username and password.
The repository gets fetched, converted and codeswarm will be started. This will create the frames of the video for you. If you want to create a real video, see "**Creating the video**" (below).
  * if you want to access a non-svn repository, read on:

This guide explains how to generate a video under a Linux install (it is based on Linux tools) but might be applied to a Windows or OSX workstation as well (some other tools might need to be used)

The prerequisite, of course, is to download code\_swarm from SVN (check out the [Source Checkout](http://code.google.com/p/codeswarm/source/checkout) page for that). You will also need `java` and `ant` to build it.

Generating your video is done in 4 steps :

  1. get the activity log out of your versioning system
  1. convert the activity log to something code\_swarm can use
  1. executing code\_swarm
  1. making a video out of the exported images


<table border='1'>
<tr>
<td>
<h1>Step 1: Getting the activity log</h1>
</td>
</tr>

<tr>
<td>
<h2>SVN</h2>
</td>
<td>
<h2>CVS</h2>
</td>
<td>
<h2>darcs</h2>
</td>
<td>
<h2>MediaWiki</h2>
</td>
<td>
<h2>Mercurial</h2>
<img src='http://www.selenic.com/hg-logo/logo-droplets-25.png' />
</td>


</tr>

<tr>
<td valign='top'>
To get the activity log from your SVN repository, just get a local copy of your repository, enter the main directory and execute<br>
<pre><code>  svn log -v &gt; activity.log<br>
</code></pre>

This will create a file called <b>activity.log</b>.<br>
<br>
</td>

<td valign='top'>
To get the activity log from your CVS repository, just get a local copy of your repository, enter the main directory and execute<br>
<pre><code>  cvs log &gt; activity.log<br>
</code></pre>

This will create a file called <b>activity.log</b>.<br>
<br>
</td>

<td valign='top'>
<pre><code>  darcs changes --summary --reverse &gt; activity.log<br>
</code></pre>

</td>

<td valign='top'>
To get a wikiswarm report from the wiki use the maintenance script inside of the <a href='https://gerrit.wikimedia.org/r/gitweb?p=mediawiki/extensions/SwarmExport.git;a=blob;f=swarmExport.php;hb=HEAD'>SwarmExport extension</a>
</td>
<td valign='top'>

Nothing to do here, read next step.<br>
<br>
</td>


</tr>

<tr>
<td>
<h1>Step 2: Convert the log</h1>
code_swarm has a python converter in its <code>convert_logs</code> directory. For a list of all the  supported systems, use: <code>python convert_logs.py --help</code> .<br>
<br>
<b>Note</b>: Before you start using it, you might want to read <a href='http://code.google.com/p/codeswarm/issues/detail?id=6'>this issue report</a>.<br>
<br>
</td>
</tr>

<tr>
<td>
<ol><li>Move to the convert_logs directory<br>
</li><li>Copy the activity.log file generated above into the current directory<br>
</li><li>Launch the convert_logs.py script:<br>
</td>
<td valign='top'>
<h2>Mercurial</h2>
<pre><code>$ cd $CODESWARM_HOME/convert_logs/<br>
$ python hg_log.py /Path/To/Your/Mercurial/Repository<br>
</code></pre></li></ol>

this produces the file <code>$CODESWARM_HOME/data/hglog.xml</code> for your given repository.<br>
</td>

</tr>
<tr>

<td>
<h2>SVN</h2>
<pre><code>python convert_logs.py -s activity.log -o activity.xml<br>
</code></pre>
</td>
<td>
<h2>CVS</h2>
<pre><code>python convert_logs.py -c activity.log -o activity.xml<br>
</code></pre>
</td>
<td>
<h2>darcs</h2>
To document.<br>
</td>
<td valign='top'>
<h2>MediaWiki</h2>
<pre><code>python convert_logs.py --wikiswarm-log=activity.log -o activity.xml<br>
</code></pre>
</td>
</tr>

<tr>
<td>

This will generate a file called activity.xml.<br>
<br>
At this point, you will not need activity.log anymore, so you can delete it.<br>
<br>
</td>
</tr>

<tr>
<td>
<h1>Step 3: Executing code_swarm</h1>
</td>
</tr>

<tr>
<td>

Before running, you will need to configure code_swarm for your project. In the <code>data</code> folder you'll find an example of a configuration file (<code>sample.config</code>).<br>
<br>
Minimally, just edit your configuration file to reflect the correct path to <code>InputFile</code>, which is the previously generated <code>xml</code> (<code>activity.xml</code> or <code>hglog.xml</code>).<br>
<br>
To execute code_swarm, you need just need <b>ant</b> and <b>javac</b>. Execute <code>run.sh</code> script in the code_swarm main folder. For more details refer to the [<a href='HowtoBuild.md'>HowtoBuild</a>] Wiki page.<br>
<br>
</td>
</tr>


<tr>
<td>
<h1>Step 4: Creating the video</h1>
</td>
</tr>
<tr>
<td>
The previous step created a series of screenshots in the PNG format under the <b>code_swarm/frames/</b> directory. Now you want to use them to build a video. MEncoder is one tool to do that. Just get into that frames/ directory and type:<br>
<br>
<pre><code>  mencoder mf://*.png -mf fps=24:type=png -ovc lavc -oac copy -o movie.avi<br>
</code></pre>

For a 15000 commits video, this might take about 40MB, so you want to reduce the size a little bit. You can do that by converting to DivX (MPG4) and reducing the frame rate:<br>
<br>
<pre><code>  mencoder movie.avi  -ovc xvid -oac mp3lame -xvidencopts bitrate=200 -o project-activity.avi<br>
</code></pre>

For a 40MB video, this reduces the size to 10MB. You can also try using the h264 format if your MEncoder has the right libraries to do so:<br>
<br>
<pre><code>  mencoder movie.avi -ovc x264 -oac mp3lame -xvidencopts bitrate=200 -o project-activity2.avi<br>
</code></pre>

To make a high quality .mov (no loss from the frames) suitable for iMovie or Final Cut:<br>
<br>
<pre><code>  ffmpeg -f image2 -r 24 -i ./frames/code_swarm-%05d.png -sameq ./out.mov -pass 2<br>
</code></pre>

(Or you can use Quick Time Player to open a image sequence and then save a resulting high quality .mov file.)<br>
<br>
That's it, you should have a beautiful video of your project's activity by now!<br>
</td>
</tr>
</table>