JFLAGS = -g
JC = javac
JVM = java
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
        Splash.java
		
MAIN = Splash

default: classes

classes: $(CLASSES:.java=.class)
		$(JVM) $(MAIN) $(B) $(R) $(S) $(h) $(input) $(dump) $(probe) $(result)

clean:
	$(RM) *.class