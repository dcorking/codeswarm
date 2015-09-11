# Introduction #

You can now run code\_swarm with opengl support.

# Details #

In quick, the steps are:
  * locate your config file, a sample is /data/sample.config
  * set UseOpenGL to true in the config file
  * add the 'lib' dir to your library path:
    * macosx: export DYLD\_LIBRARY\_PATH=$DYLD\_LIBRARY\_PATH:<path to code\_swarm/lib>
    * linux: export LD\_LIBRARY\_PATH=$LD\_LIBRARY\_PATH:<path to code\_swarm/lib>
  * 'ant run'