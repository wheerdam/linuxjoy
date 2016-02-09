/*
	Copyright 2016 Wira Mulia

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

#include "../org_bbi_linuxjoy_NoJoy.h"
#include <jni.h>
#include <stdio.h>
#include <fcntl.h>
#include <string.h>
#include <linux/joystick.h>

#define MAX_DEVICES 64

static int fd[MAX_DEVICES];
static int index_map[MAX_DEVICES];
static int device_count = 0;

/*
 * The following is an array of identifier string that we recognize and will
 * be mapped with the ID field of the joyinfo array that the enumerate 
 * function will return. The identifier string of the device is acquired by 
 * using the JSIOCGNAME ioctl on the joystick file handle. The enumerate 
 * function will iterate through this array and assign the index if it finds
 * a match.
 *
 * An identifier entry can not be longer than 255 characters. Make sure that
 * this list corresponds to the list defined in linuxjoy-api document.
 */

#define KNOWN_DEVICES 2

static char *identifiers[] = {
	"Unknown",
	"Microsoft X-Box 360 pad"
};

JNIEXPORT jbyteArray JNICALL Java_org_bbi_linuxjoy_NoJoy_nativePoll
  (JNIEnv *env, jobject obj, jint index)
{
	int nr = 0;
	jbyte buf[8192];

	if(index >= 0 && index < device_count) {
		nr = read(fd[index], buf, 8192);
		if(nr == -1) {
			nr = 0; // return nothing
		}
	}
	jbyteArray r = (*env)->NewByteArray(env, nr);
	(*env)->SetByteArrayRegion(env, r, 0, nr, buf);
	return r;
}

JNIEXPORT jboolean JNICALL Java_org_bbi_linuxjoy_NoJoy_openNativeDevice
  (JNIEnv *env, jobject obj, jint index)
{
	int i;
	char path[256];

	if(index < 0 || index >= device_count){
		printf("njnative[%d]: invalid index\n", index);
		return JNI_FALSE;
	} else {
		i = index_map[index];  // get our file #
		snprintf(path, 256, "/dev/input/js%d", i);
		fd[index] = open(path, O_RDONLY | O_NONBLOCK);
		if(fd[index] == -1) {
			printf("njnative[%d]: failed to open %s\n", index, path);
			return JNI_FALSE;
		} else {
			printf("njnative[%d]: %s opened\n", index, path);
			return JNI_TRUE;
		}
	}
}

JNIEXPORT jboolean JNICALL Java_org_bbi_linuxjoy_NoJoy_isNativeDeviceOpen
  (JNIEnv *env, jobject obj, jint index)
{
	if(index < 0 || index >= device_count) {
		return JNI_FALSE;
	} else if(fd[index] != -1) {
		return JNI_TRUE;
	} else {
		return JNI_FALSE;
	}
}

JNIEXPORT jintArray JNICALL Java_org_bbi_linuxjoy_NoJoy_enumerate
  (JNIEnv *env, jclass cls)
{
	int i, j, fd;
	char buttons, axes;
	char path[256];
	char name[256];
	int joyinfo[MAX_DEVICES];

	// reset device count
	device_count = 0;

	// iterate through /dev/input/jsXX
	for(i = 0; i < MAX_DEVICES; i++) {
		snprintf(path, 256, "/dev/input/js%d", i);
		fd = open (path, O_RDONLY | O_NONBLOCK);
		
		if(fd != -1) {			
			// we have a device file we can open
			index_map[device_count] = i;  // map our count with file #
			ioctl(fd, JSIOCGBUTTONS, &buttons);
			ioctl(fd, JSIOCGAXES, &axes);

			if (ioctl(fd, JSIOCGNAME(sizeof(name)), name) < 0)
				strncpy(name, "Unknown", sizeof(name));	

			joyinfo[device_count] = 0; // default generic

			// find if we have a matching identifier string
			for(j = 0; j < KNOWN_DEVICES; j++) {
				if (strcmp(name, identifiers[j]) == 0) {
					joyinfo[device_count] = j;
				}
			}	

			joyinfo[device_count] |= (buttons << 8);			
			joyinfo[device_count] |= (axes << 16);

			printf("njnative[%d]: %s - \"%s\" (%d buttons, %d axes)\n",
				 device_count, path, name, buttons, axes);
			device_count++;
			close(fd);
		}
	}
	printf("njnative: %d devices found\n", device_count);
	jintArray r = (*env)->NewIntArray(env, device_count);
	(*env)->SetIntArrayRegion(env, r, 0, device_count, joyinfo);
	return r;	
}

JNIEXPORT jboolean JNICALL Java_org_bbi_linuxjoy_NoJoy_closeNativeDevice
  (JNIEnv *env, jobject obj, jint index)
{
	if(index < 0 || index >= device_count) {
		return JNI_FALSE;
	}

	if(close(fd[index]) == -1) {
		printf("njnative[%d]: close returned an error\n", index);
		return JNI_FALSE;
	} else {
		printf("njnative[%d]: closed\n", index);
		return JNI_TRUE;
	}
}

JNIEXPORT jbyteArray JNICALL Java_org_bbi_linuxjoy_NoJoy_setNativeProperty
  (JNIEnv *env, jclass cls, jint index, jint key, jint value)
{
	return (*env)->NewByteArray(env, 0);
}

JNIEXPORT jstring JNICALL Java_org_bbi_linuxjoy_NoJoy_getVersionString
  (JNIEnv *env, jclass cls)
{
	char str[] = "NoJoy Linux Non-blocking (libnjnative.so) v1.00";
	jstring r = (*env)->NewStringUTF(env, str);
	return r;
}

