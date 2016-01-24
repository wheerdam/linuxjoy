for %%f in (*.java) do (
	echo Compiling %%f
	javac -cp ..\..\dist\LinuxJoystick.jar %%f
)