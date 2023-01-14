javac TP1.java
sort $1 -u -k 1n -k 2n
awk '/^[^#]/ {print $0"\n" $2 " " $1 }' $1 | sort -u -k 1n -k 2n > out.txt
/usr/bin/time -f"%e\n%M" java TP1 out.txt $2