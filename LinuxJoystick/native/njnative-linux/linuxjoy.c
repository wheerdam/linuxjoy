#include "../org_bbi_linuxjoy_NoJoy.h"
#include <jni.h>
#include <dirent.h>
#include <stdio.h>
#include <fcntl.h>
#include <string.h>
#include <linux/joystick.h>

#define MAX_DEVICES 64 // less than 100

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
	char file_index[3];
	char path[256] = "/dev/input/js";

	if(index < 0 || index >= device_count) {
		printf("njnative[%d]: invalid index\n", index);
		return JNI_FALSE;
	} else {
		i = index_map[index];
		if(i < 10) {
			file_index[0] = i + '0';
			file_index[1] = 0;
		} else {
			file_index[0] = (i / 10) + '0';
			file_index[1] = (i % 10) + '0';
			file_index[2] = 0;
		}
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
	char index[3];
	char path[256] = "/dev/input/js";
	int joyinfo[MAX_DEVICES];

	// reset device count
	device_count = 0;

	// map our indexing with jsXX files
	for(i = 0; i < MAX_DEVICES; i++) {
		if(i < 10) {
			index[0] = i + '0';
			index[1] = 0;
		} else {
			index[0] = (i / 10) + '0';
			index[1] = (i % 10) + '0';
			index[2] = 0;
		}
		strncat(path, index, 256);
		fd = open (path, O_RDONLY | O_NONBLOCK);
		if(fd != -1) {
			joyinfo[device_count] = 0x00; // report generic
			index_map[device_count] = i;
			ioctl(fd, JSIOCGBUTTONS, &buttons);
			joyinfo[device_count] |= (buttons << 8);
			ioctl(fd, JSIOCGAXES, &axes);
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
	char str[256] = "NoJoy Linux Non-blocking (libnjnative.so) v1.00";
	jstring r = (*env)->NewStringUTF(env, str);
	return r;
}

