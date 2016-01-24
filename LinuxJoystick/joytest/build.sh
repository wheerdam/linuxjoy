if [ ! -e ../dist/LinuxJoystick.jar ]
then
	echo linuxjoy distribution not found
	exit -1
fi

for i in $( ls *.java ); do
	javac -cp ../dist/LinuxJoystick.jar:. $i
done
