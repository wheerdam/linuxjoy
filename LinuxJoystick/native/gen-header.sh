if [ ! -e ../dist/LinuxJoystick.jar ]
then
	echo LinuxJoystick distribution not found
	exit -1
fi

javah -cp ../dist/LinuxJoystick.jar org.bbi.linuxjoy.NoJoy
