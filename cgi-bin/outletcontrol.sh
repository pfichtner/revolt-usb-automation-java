#!/bin/sh

#################################################################################################
# Shell script to control the outlets via cmdline tool                                          #
# Command line tools can be found e.g. on https://github.com/kralo/revolt-usb-automation-python # 
# or https://github.com/pfichtner/revolt-usb-automation-java/                                   #
#                                                                                               #
# If you want to name your outlets put a file name outlets.txt into the same directory          #
#################################################################################################

# Convert POST to GET
if [ "$REQUEST_METHOD" = POST ]
then
  query_string=`dd bs=1 count="$CONTENT_LENGTH" 2>/dev/null`
fi
query_string=`echo "$query_string" | awk 'BEGIN {RS="&"; FS="="} {print $0}'`

outlet_line=`echo "$query_string" | grep -e "outlet.*$"`
outlet=`echo "$outlet_line" | cut -d'=' -f1 | cut -c7-`
state=`echo "$outlet_line" | cut -d'=' -f2`


echo "Content-type: text/html"
echo
echo "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">"
echo "<html>"
echo "<head>"
echo "<title>revolt home automation</title>"

echo "<style type=\"text/css\">"
echo "#submit {"
echo "    background-color: #ccc;"
echo "    border-radius:6px;"
echo "    margin-left: 12px;"
echo "    margin-right: 12px;"
echo "    color: #fff;"
echo "    font-size: 64px;"
echo "    text-decoration: none;"
echo "    cursor: pointer;"
echo "    border:none;"
echo "}"
echo "#submit:hover {"
echo "    border: none;"
echo "    background:red;"
echo "    box-shadow: 0px 0px 1px #777;"
echo "}"
echo "</style>"

echo "</head>"
echo "<body>"
#echo "<form action=\"http://www.comp.leeds.ac.uk/cgi-bin/Perl/environment-example\" method=\"POST\">"
echo "<form method=\"POST\">"
echo "<table>"


configfile=`dirname $0`/outlets.txt
for nr in 1 2 3 4 ALL; do
	name=$nr
	[ -r $configfile ] && name=`grep -i "outlet.$nr" $configfile  | cut -f2 -d'='`
	[ -z "$name" ] && name=$nr
	echo "<tr>"
	echo "<td><input id=\"submit\" name=\"outlet$nr\" value="ON" type=\"submit\"></td>"
	echo "<td><center><b>$name</b></center></td>"
	echo "<td><input id=\"submit\" name=\"outlet$nr\" value="OFF" type=\"submit\"></td>"
	echo "</tr>"
done
echo "</table>"

if [ -n "$outlet" ]; then
	base="/path/where/you/placed/the/jar"
        /usr/bin/java -cp $base/cmdline-0.0.1-shaded.jar com.github.pfichtner.revoltusbautomationjava.cmdline.Main -outlet "$outlet" -state "$state"

fi

echo "</form>"
echo "</body>"
echo "</html>"
