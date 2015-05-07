jPhineas - a PHIN-MS compatible web application

jPhineas is a Java J2EE adaptation of my Phineas messaging application.  The
original is written in 'C' and considerably smaller (at least 10x smaller!),
but of course not portable and currently only targeted for MS OS (a *NIX version
is in the works).  So if you don't want to put up an additional server to support
PHIN-MS messaging, you might want to check Phineas out first.  For folks looking
for Enterprise OS independence this is the right place.

JPHINEAS.ZIP should contain:

readme.txt - this file
jphineas.war - a web archive with hopefully working sender
jphineas - a folder with basic configurations, etc to get started

After unpacking the archive, copy the WAR file to your web container 
(Tomcat, Jetty, or whatever will provide JSP support). Locate the 
jphineas folder somewhere that will be writable by your web server. 
Then modify web.xml for a full path to jphineas/config/jPhineas.xml, 
make any additional changes you like, and restart the application 
context (or server).

If successful, you should see the "help"/"index" page at /jphineas/console,
and supporting smoke screens (:-) at /jphineas/sender and /jphineas/receiver.
The "help" page includes most of what you'll need to get started assuming
you are familiar with PHIN-MS and web applications in general.

Note that jphineas/security holds the default PHINMS server certificate in
various forms including a CA.  Feel free to substitute your own (making 
appropriate changes in the configuration).

To build modify build.xml properties found near the top for paths to your 
environment's jars, JDK, etc.  Note the Unit Tests are written in Groovy and
need a groovy compiler/run time.  Feel free to re-code these in Java, but keep
in mind Groovy reflection allows one to easily test (private) methods not 
normally visible to pure Jave, and that's why I use it.