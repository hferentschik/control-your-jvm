# System tools for checking system activity

## Linux

* [Command line tools to  monitor Linux performance](http://www.tecmint.com/command-line-tools-to-monitor-linux-performance)

## Mac

* top

 * [How to use top](http://www.tech-faq.com/how-to-use-the-unix-top-command.html)
 * [How to reduce the CPU usage of top itself](http://hints.macworld.com/article.php?story=20040213045335693)

    ```
    # Using the options below will reduce CPU cycles of top itself from 10% to 1%
    # -R - do not traverse memory object map
    # -F - do not calculate stats for shared libraries
    # -s - set update interval
    # -n - limit to 30 processes
    > top -ocpu -R -F -s 2 -n30
    ```

* iostat

  ```
  > iostat  -K -w 5
  ```

 * http://betabug.ch/blogs/ch-athens/227


* vm_stat

  ```
  vm_stat 5
  ```

* Dtrace

 * [Dtrace+Java Tutorial](http://dtracehol.com)
 * [DTrace Probes in HotSpot VM](http://docs.oracle.com/javase/6/docs/technotes/guides/vm/dtrace.html)
 * [OpenJDK Dtrace hotsport provider source](http://hg.openjdk.java.net/jdk6/jdk6/hotspot/file/436b4a3231bf/src/os/bsd/dtrace/hotspot.d)
 * [Mac OS X Port Using HotSpot DTrace Probes](https://wiki.openjdk.java.net/display/MacOSXPort/Mac+OS+X+Port+Using+HotSpot+DTrace+Probes)


