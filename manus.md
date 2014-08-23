# Demo manuscript

* [jps, jinfo, jstat](#jps--jinfo--jstat)
* [jstack, jcmd](#jstack--jcmd)
* [jmap, jhat](#jmap--jhat)
* [Mission Control](#mission-control)

---

_The examples assume that your docker host is reachable under the IP 192.168.59.103. This needs to
be changed in case your Docker host as a different IP!_

Commands are default Java command line tool commands or docker commands, except `docker-enter`.
The latter is a shortcut for the following
(see also [docker-enter](https://github.com/jpetazzo/nsenter/blob/master/docker-enter)):

```
function docker-enter() {
  boot2docker ssh '[ -f /var/lib/boot2docker/nsenter ] || (docker run --rm -v /var/lib/boot2docker/:/target jpetazzo/nsenter ; sudo curl -Lo /var/lib/boot2docker/docker-enter https://raw.githubusercontent.com/jpetazzo/nsenter/master/docker-enter )'
  boot2docker ssh -t sudo /var/lib/boot2docker/docker-enter "$@"
}
```

---

## jps, jinfo, jstat

Using Java 8

1. Start demo program 4

   ```
   > docker run -p 1099:1099 -p 80:80 -t -i control_your_jvm/demo:4
   ```

1. Demo jps

   ```
   > docker ps
   > docker-enter <docker-id>
   # jps
   # jps -lvm

   ; alterntively jps remote
   > jps -J-Dhttp.proxyHost=192.168.59.103 -J-Djava.rmi.server.disableHttp=false 192.168.59.103
   ```

1. Demo jinfo

   ```
   # jinfo <vmid>
   # jinfo -flag UnlockCommercialFeatures <vmid>
   # jinfo -flag +UnlockCommercialFeatures <vmid>
   # jinfo -flag UnlockCommercialFeatures <vmid>
   ```

1. (optional) Demo the setting of manageable flags. First we check whether commercial features flag is set,
   then we enable commercial features. Only then FlightRecorder even becomes available. Then we
   enable the FlightRecorder flag and check that it is set.

   ```
   # jinfo -flag UnlockCommercialFeatures <vmid>
   # jinfo -flag +UnlockCommercialFeatures <vmid>
   # jinfo -flag +FlightRecorder <vmid>
   # jinfo -flag FlightRecorder <vmid>
   ```

1. Demo jstat

   ```
   # jstat -options
   # jstat -gc <vmid> 5s 10

   ; optional remote jstat
   > jstat -J-Dhttp.proxyHost=192.168.59.103 -J-Djava.rmi.server.disableHttp=false -gc <vmid>@192.168.59.103 5s 10
   ```

   Discuss output. Talk about occuring garbage collections. Demo 4 shows constant garbage collections.

1. Start demo program 1 via Docker and run jstat

   ```
   > docker run -p 1099:1099 -p 80:80 -t -i control_your_jvm/demo:1
   > docker ps
   > docker-enter <docker-id>
   # jps
   # jstat -gc <vmid> 5s 10
   ```

   Compare result with previous demo. No garbage collections, but app is still busy as `top` shows.

### Garbage-collected heap statistics.

| Code  | Description                                           |
| ------| ------------------------------------------------------|
| S0C   | Current survivor space 0 capacity (KB).               |
| S1C   | Current survivor space 1 capacity (KB).               |
| S0U   | Survivor space 0 utilization (KB).                    |
| S1U   | Survivor space 1 utilization (KB).                    |
| EC    | Current eden space capacity (KB).                     |
| EU    | Eden space utilization (KB).                          |
| OC    | Current old space capacity (KB).                      |
| OU    | Old space utilization (KB).                           |
| MC    | Metaspace capacity (KB).                              |
| MU    | Metacspace utilization (KB).                          |
| YGC   | Number of young generation garbage collection events. |
| YGCT  | Young generation garbage collection time.             |
| FGC   | Number of full GC events.                             |
| FGCT  | Full garbage collection time.                         |
| GCT   | Total garbage collection time.                        |


## jstack, jcmd

1. Demo creating thread dump using jstack

   ```
   > docker run -t -i control_your_jvm/demo:3
   > docker ps
   > docker-enter <container-id>
   ; use jcmd without options as alternative to jps
   # jcmd
   # jcmd <vmid> Thread.print
   # jstack <vmid>

   ; optional - demo how jcmd can be used with a main class argument targeting mutliple VMs
   ; start a second Demo program
   # cd /demo
   # java -cp . Demo 3 &
   ; Run jcmd using the main class name
   # jcmd Demo VM.uptime
   ```

1. Other ways to create stack trace

  ```
  > ctrl-\
  > kill - QUIT <pid>
  ```

## jmap / jhat

Demonstrate the detection of a memory leak within Wildfly due to [HV-838](https://hibernate.atlassian.net/browse/HV-838). First we start a Widlfly server containing a Validator version which still causes the leak.
The leak gets triggered by deploying the kitchensink demo app and registering some members. This will
cause classmate to cache some of the deployment classes which then will not be able to be garbage
collected anymore. After registering some users we take a heap dump and localize the leak using jhat.

Then we start Wildfly with the latest Validator version and deploy the kitchensink app again, registering
some users. After taking a new heap dump we show that the leak is gone.

 1. Start wildfly docker image

    ```
    > docker run -it -p 1099:1099 -p 7000:7000 -p 7091:7091 -p 8180:8180 -p 10090:10090 -p 10099:10099 --volumes-from my-data control_your_jvm/wildfly-8.1.0.final:leaking
    ```

 1. Show the running Wildfly server

    * [Welcome page](http://192.168.59.103:8180)
    * [Admin Console](http://192.168.59.103:10090) _(admin/admin)_

 1. Redeploy and create members via script

    ```
    > ./kitchensink-control.sh redeploy 2
    ```

 1. Get a shell onto Docker container using [nsenter](https://github.com/jpetazzo/nsenter)

    ```
    > docker ps
    > docker-enter <id>
    # su wildfly
    ```

 1. Determine Wildfly id via `jps`

 1. Trigger GC

    ```
    # jstat -gc <vmid>
    # jcmd <vmid> GC.run
    # jstat -gc <vmid>
    ```

 1. Get a heap dump

    ```
    ; -F needed in case JVM does not respond
    # time jmap -F -dump:format=b,file=heap.bin <vmid>
    ```

 1. Start jhat to browse the dump

    ```
    # jhat heap.bin
    ```

    http://192.168.59.103:7000

 1. Show that kitchen-sink classes are still not garbage collected

    Look for 'org.jboss.as.quickstarts.kitchensink.model.IMember'

 1. Repeat from step one with control_your_jvm/wildfly-8.1.0.final:non-leaking

 1. Run some OQL queries http://192.168.59.103:7000/oql

    ```
    > select file.path.value.toString() from java.io.File file
    > select heap.objects("org.hibernate.validator.HibernateValidator")
    > select referrers(v) from org.hibernate.validator.HibernateValidator v
    > select root(v) from org.hibernate.validator.HibernateValidator v
    ```

## Mission Control

### Basic UI

 1. Start Wildfly docker image

    ```
    docker run -it -p 1099:1099 -p 7091:7091 -p 8180:8180 -p 10090:10090 -p 10099:10099 control_your_jvm/wildfly-8.1.0.final:non-leaking
    ```

 1. Connect remotely

    ```
    > jmc
    ```

 1. Choose "File -> Connect" -> "Create New Connection" -> 192.168.59.103:7091

 1. Show the different tabs

 1. Show JMBean browser (_com.sun.management:type=HotSpotDiagnostic_)

### Flight Recorder recordings

* Via the JMC GUI
 * Show profile editor
* jcmd

  ```
  > jcmd <pid> JFR.start name=WildflyJFRRecording settings=default
  > jcmd <pid> JFR.check
  > jcmd <pid> JFR.dump name=WildflyJFRRecording filename=/tmp/Wildfly.jfr
  > jcmd <pid> JFR.stop
  > jcmd <pid> JFR.stop name=WildflyJFRRecording
  ```

* Command line option

  ```
  -XX:StartFlightRecording=delay=20s,duration=60s,name=WildflyJFRRecording,filename=/tmp/Wildfly.jfr,settings=profile
  ```





