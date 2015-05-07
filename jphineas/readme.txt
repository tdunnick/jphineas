This is an example of one possible layout for configurations and data.  

Within web.xml, you should set the Configuration param to reflect the location of
this folder.  The default (below) has it located at the system root:

  <!-- set this param-value to the master configuration file -->
  <context-param>
    <param-name>Configuration</param-name>
    <param-value>/jphineas/config/jPhineas.xml</param-value>
  </context-param>

All of these folders and files are (indirectly) referenced via jPhineas.xml.
Each of the configurations assumes a default directory for relative references
that is two folders above it's own (e.g. the parent of config/).  You can either 
override this location in the configuration, or provide full paths for individual 
references.