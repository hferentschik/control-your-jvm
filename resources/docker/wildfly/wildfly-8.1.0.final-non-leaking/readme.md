# Wildfly 8.1.0 Docker image

This directory contains the `Dockerfile` and resources to build a Wildfly 8.1.0 image running on
[Ubuntu Kylin 14.04](http://www.ubuntu.com/) within a Oracle JVM 7.

# Build the image

    > docker build -t control_your_jvm/wildfly-8.1.0.final:non-leaking .

The image patches Wildfly 8.1.0 by replacing the `javax.el` version. This is not to fix a bug, but
rather just makes it easier to demonstrate the detection of an actual class loader leak. The patch
disables caching within _BeanELResolver_ via a _SoftReference_ cache. Don't use this patch in
production!

# Start Wildfly

    > docker run -it --volumes-from my-data -p 1099:1099 -p 7000:7000 -p 7091:7091 -p 8180:8180 -p 10090:10090 -p 10099:10099 control_your_jvm/wildfly-8.1.0.final:non-leaking

## Explanation of used Java flags

    # standard wildfly server options
    -server
    -Xms64m -Xmx512m -XX:MaxPermSize=256m
    -Djava.net.preferIPv4Stack=true
    -Djava.awt.headless=true

    # allow for taking flight recordings (rule number one of flight recording, always start with these JVM options)
    -XX:+UnlockCommercialFeatures XX:+FlightRecorder

    # http://stackoverflow.com/questions/3334911/what-does-jvm-flag-cmsclassunloadingenabled-actually-do
    -XX:+CMSClassUnloadingEnabled -XX:+CMSPermGenSweepingEnabled \

    # allow for remote JMX access (unsecured!)
    # needed to remotely attach via Java Mission Control
    -Dcom.sun.management.jmxremote=true
    -Dcom.sun.management.jmxremote.port=7091
    -Dcom.sun.management.jmxremote.authenticate=false
    -Dcom.sun.management.jmxremote.ssl=false
    -Dcom.sun.management.jmxremote.rmi.port=7091

    # Required when using proxying for RMI
    -Djava.rmi.server.hostname=127.0.0.1

    # Make sure that log mamanger classes are also available for java agent
    # see (todo wildfly issue key)
    -Djboss.modules.system.pkgs=org.jboss.byteman,org.jboss.logmanager
    -Djava.util.logging.manager=org.jboss.logmanager.LogManager
    -Xbootclasspath/p:$JBOSS_HOME/modules/system/layers/base/org/jboss/logmanager/main/jboss-logmanager-1.5.2.Final.jar

    # Run all relevant Wildfly ports with an offset. Useful when running multiple instances
    -Djboss.socket.binding.port-offset=100



