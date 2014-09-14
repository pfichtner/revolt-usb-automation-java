#!/bin/sh

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
echo "<head><title>Home automation</title></head>"
echo "<body>"
#echo "<form action=\"http://www.comp.leeds.ac.uk/cgi-bin/Perl/environment-example\" method=\"POST\">"
echo "<form method=\"POST\">"
echo "<table>"
for nr in 1 2 3 4 ALL; do
echo "<tr>"
echo "<td><input name=\"outlet$nr\" value="ON" type=\"submit\"></td>"
echo "<td><center>$nr</center></td>"
echo "<td><input name=\"outlet$nr\" value="OFF" type=\"submit\"></td>"
echo "</tr>"
done
echo "</table>"

if [ -n "$outlet" ]; then
	# place your call to switch the outlet(s), e.g. java -cp myjar.jar my.Main -outlet "$outlet" -state "$state"
fi

echo "</form>"
echo "</body>"
echo "</html>"
