JAVA=/usr/lib/jvm/java-7-openjdk-amd64

all: libnjnative.so

libnjnative.so:
	gcc -I $(JAVA)/include -fPIC -o $@ -shared linuxjoy.c

clean:
	rm *.so
