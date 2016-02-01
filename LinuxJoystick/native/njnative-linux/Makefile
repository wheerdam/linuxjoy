all: libnjnative.so

libnjnative.so:
	gcc -I /usr/lib/jvm/java-7-openjdk-amd64/include -fPIC -o $@ -shared linuxjoy.c

clean:
	rm *.so
