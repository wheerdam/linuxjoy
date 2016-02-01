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
	char file_index[8];
	char path[256] = "/dev/input/js";

	if(index < 0 || index >= device_count){
		printf("njnative[%d]: invalid index\n", index);
		return JNI_FALSE;
	} else {
		i = index_map[index];  // get our file #
		snprintf(file_index, 8, "%d", i);
		strncat(path, file_index, 256);
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
	int i, fd;
	char buttons, axes;
	char file_index[8];
	char prefix[] = "/dev/input/js";
	char path[256];
	int joyinfo[MAX_DEVICES];

	// reset device count
	device_count = 0;

	// iterate through /dev/input/jsXX
	for(i = 0; i < MAX_DEVICES; i++) {
		path[0] = 0;
		strncat(path, prefix, 256);
		snprintf(file_index, 16, "%d", i);
		strncat(path, file_index, 256);
		fd = open (path, O_RDONLY | O_NONBLOCK);
		
		// we have a device file we can open
		if(fd != -1) {			
			index_map[device_count] = i;  // map our count with file #
			ioctl(fd, JSIOCGBUTTONS, &buttons);
			ioctl(fd, JSIOCGAXES, &axes);
			joyinfo[device_count] = 0x00; // report generic
			joyinfo[device_count] |= (buttons << 8);			
			joyinfo[device_count] |= (axes << 16);
			printf("njnative[%d]: %s detected (%d buttons, %d axes)\n",
				 device_count, path, buttons, axes);
			device_count++;
			close(fd);
			fd = -1;
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

