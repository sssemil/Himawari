# Himawari
Himawari-8 images downloader, and as desktop background setter.

Building
--------

To build jar run:

    ./gradlew jar
    
Usage
-----

 -d,--delay <arg>             delay in ms between each check for a new
                              image. Default is 1 second.
 
 -db,--set-desktop            downloads only on a not cellular network.
                              For now only "O2 Deutschland" is supported.
 
-f,--image-file-name <arg>   file name for downloaded image. Default from
                              the server by default
 
-h,--help                    show help.
 
-l,--level <arg>             increases the quality (and the size) of each
                              tile. Possible values are 4, 8, 16, 20

-m,--check-mobile            downloads only on a not cellular network.
                              For now only "O2 Deutschland" is supported.

-o,--out-dir <arg>           out directory for downloaded images and
                              logs. "output/" is default.

-s,--single-loop             image will be downloaded only once.

-sb,--set-lock-screen        downloads only on a not cellular network.

-w,--save-logs               save logs to file.

Running
-------
Here's an example to save logs, check for moble network, use level 8 quality,
run it once, set as desktop and lockscreen background, and save to ~/Pictures/.

    java -jar ./Himawari-1.0.jar -w -m -l 8 -s -db -sb -o ~/Pictures/
    
To run it every 10 minutes you can use crontab:

    crontab -e

This will open crontab editing. Then add this line:

    */10 * * * * java -jar ./Himawari-1.0.jar -w -m -l 8 -s -db -sb -o ~/Pictures/

Acknowledgments
---------------
Based on @boramalper's work - https://github.com/boramalper/himawaripy