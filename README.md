# revolt-usb-automation-java

Java example to command wireless 433mhz power outlets

The pearl / revolt-power px-1672 and px-1674 is a package with a 433 mhz power outlet and a usb dongle. 
The only software to control it that comes supplied is the gui-only Huading RF.exe (About Dialog: First RF V1.0 Hanson Han). 
The dongle has the usb id ffff:1122 and the program talks to it via usb urb out packets. 
Windows recognizes the dongle as HID device.

All the research was done by https://github.com/kralo and Ralph Babel, http://babel.de, for finding the checksum and the resend-frame behavior. 

So blame me for the bugs in the java implementation and thank those guys above for examing the protocol!

This repository hosts four separate items: 
* A command line tool to switch the power outlets
* A [MQTT](https://en.wikipedia.org/wiki/MQTT "MQTT") client, e.g. for integrating into [openHAB](http://www.openhab.org/ "openHAB")
Example entry into items: ```Switch MyItem "MyItem" (GF_Living, Lights) {mqtt=">[brokerNameDefinedInOpenhabCfg:home/devices/px1675/outlet1/value/set:command:ON:true], >[brokerNameDefinedInOpenhabCfg:home/devices/px1675/outlet1/value/set:command:OFF:false]"}```
* A Java/SwingUI application that runs all plattforms http://usb4java.org/ runs (currently Linux x86 32/64 bit/ARM 32 bit; OS X x86 32/64 bit;Windows x86 32/64 bit) very similar to the distributed proprietary Windows EXE
![SwingUI Screenshot](https://pfichtner.github.io/revolt-usb-automation-java/screenshots/swingui.png)
* A sh based cgi-bin that shows on/off buttons where you can refer your favourite script/binary to switch outlets

To communicate with the usb devie [usb4java](http://usb4java.org/) ist used. So please take a look at  http://usb4java.org/faq.html for device permissions. E.g. on linux you have to create a file named ```/etc/udev/rules.d/99-userusbdevices.rules``` containing a line
> SUBSYSTEM=="usb",ATTR{idVendor}=="ffff",ATTR{idProduct}=="1122",MODE="0660",GROUP="gpio"

Please replace gpio by a group your user belongs to. 

