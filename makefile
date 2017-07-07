jar: clean
	javac -sourcepath src/ -d bin/ src/config/Configuration.java
	cd bin ; jar cf ../Configuration.jar config/*.class

clean:
	if [ -d "bin/" ] ; then { [ "$(ls -A bin/)" ] && rm -r bin/* || true ; } ; else mkdir bin ; fi ;
	if [ -f Configuration.jar ] ; then rm Configuration.jar ; fi
