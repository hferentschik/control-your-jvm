# Wildfly 8.1.0 Docker image w/ leaking Hibernate Validator version

This directory contain the `Dockerfile` and resources to build a Wildfly 8.1.0 container running on
[Ubuntu xyz](http://www.ubuntu.com/) within a Oracle JVM 7. It also reverts the default Hibernate
Validator version of Wildfly to a version containing a memory leak.

# Build the image

    > docker build -t control_your_jvm/wildfly-8.1.0.final:leaking .

# Start Wildfly

    > docker run -it --volumes-from my-data -p 1099:1099 -p 7000:7000 -p 7091:7091 -p 8180:8180 -p 10090:10090 -p 10099:10099 control_your_jvm/wildfly-8.1.0.final:non-leaking