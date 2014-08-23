#! /bin/bash
# build-demo-container.sh

# Used to build Docker container which run the different demo programs directly via entry points.
# Also builds a container with exlpicit GC monitoring falgs enabled

# Usage './build-demo-container.sh'

# build container for the 4 Demo programs
for i in 1 2 3 4
do
   sed -i.orig "s@command=java.*@command=java -cp /demo Demo $i@g" etc/supervisor/supervisor.conf
   docker build -t control_your_jvm/demo:$i .
   mv etc/supervisor/supervisor.conf.orig etc/supervisor/supervisor.conf
done

# build a container w/ gc logging options enabled
sed -i.orig "s@command=java.*@command=java -verbose:gc -XX:+PrintGCTimeStamps -XX:+PrintGCDetails -cp /demo Demo 4@" etc/supervisor/supervisor.conf
docker build -t control_your_jvm/demo:gc-logging .
mv etc/supervisor/supervisor.conf.orig etc/supervisor/supervisor.conf