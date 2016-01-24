for %%f in (*.java) do (
	echo Compiling %%f
	javac -cp ..\..\dev-distrib\LinuxJoystick.jar %%f
)