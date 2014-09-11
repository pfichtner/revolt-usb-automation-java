revolt-usb-automation-java
==========================

Java example to command wireless 433mhz power outlets

The pearl / revolt-power px-1672 and -1674 is a package with a 433 mhz power outlet and a usb dongle. 
The only software to control it that comes supplied is the gui-only Huading RF.exe (About Dialog: First RF V1.0 Hanson Han). 
The dongle has the usb id ffff:1122 and the program talks to it via usb urb out packets. 
Windows recognizes the dongle as HID device.

All the work was done by https://github.com/kralo and Ralph Babel, http://babel.de, for finding the checksum and the resend-frame behavior. 

So blame me for the bugs in the java implementation and thank those guys above for examing th protocol!
