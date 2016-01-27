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

#include "org_bbi_linuxjoy_NoJoy.h"
#include <jni.h>
#include <stdio.h>
#include <windows.h>
#include <Xinput.h>

#define XBOX_JOYINFO 0x080B01

#define KEY_DEADZONE 0
#define KEY_VIBRATION 1
#define KEY_VERSION 2

/*
We use this particular version of the library so this dll will work in Windows 7
and possibly earlier. If we use Xinput.lib in Visual C++ 2015 this dll will not run
in Windows version earlier than 8 (failed dependency)
*/
#pragma comment(lib, "XInput9_1_0.lib")

static XINPUT_GAMEPAD saved_state[4];
static XINPUT_STATE controller_state[4];

static WORD hardDeadZone = 6000;

static const char version[] = "NoJoy Windows XInput (njnative.dll) v1.00";

// fill our buffer with the latest joystick event
void nj_put(jbyte *buf, jint offset, jint time, jshort val, jbyte type, jbyte num) {
	// This is the Linux joystick API data structure as described in:
	//		Documentation/input/joystick-api.txt 
	// in the kernel source directory. We assume little endian (x86 machine)
	buf[0 + offset] = (time & 0xff);
	buf[1 + offset] = (time >> 8) & 0xff;
	buf[2 + offset] = (time >> 16) & 0xff;
	buf[3 + offset] = (time >> 24) & 0xff;
	buf[4 + offset] = (val & 0xff);
	buf[5 + offset] = (val >> 8) & 0xff;
	buf[6 + offset] = type;
	buf[7 + offset] = num;
}

bool dz_check(jshort value) {
	if (value > hardDeadZone || value < -hardDeadZone) {
		return true;
	}
	return false;
}

JNIEXPORT jbyteArray JNICALL Java_org_bbi_linuxjoy_NoJoy_nativePoll
  (JNIEnv *env, jobject obj, jint index)
{
	// let's figure out if anything has changed
	DWORD r = XInputGetState(index, &controller_state[index]);
	if (r != ERROR_SUCCESS) {
		return env->NewByteArray(0);
	}
	else {
		// XINPUT_GAMEPAD data structure documentation:
		// https://msdn.microsoft.com/en-us/library/windows/desktop/microsoft.directx_sdk.reference.xinput_gamepad(v=vs.85).aspx

		jint eventCount = 0;
		jbyte buf[8192];

		// use packetnumber as time for debugging
		jint time = controller_state[index].dwPacketNumber;

		// check if we have a change of state for the buttons
		WORD currButtons = controller_state[index].Gamepad.wButtons;
		WORD prevButtons = saved_state[index].wButtons;
		
		if (currButtons != prevButtons) {
			if ((currButtons & XINPUT_GAMEPAD_A) != (prevButtons & XINPUT_GAMEPAD_A)) {
				nj_put(buf, eventCount * 8, time, (currButtons & XINPUT_GAMEPAD_A) >> 12, 1, 0);
				eventCount++;
			}
			if ((currButtons & XINPUT_GAMEPAD_B) != (prevButtons & XINPUT_GAMEPAD_B)) {
				nj_put(buf, eventCount * 8, time, (currButtons & XINPUT_GAMEPAD_B) >> 13, 1, 1);
				eventCount++;
			}
			if ((currButtons & XINPUT_GAMEPAD_X) != (prevButtons & XINPUT_GAMEPAD_X)) {
				nj_put(buf, eventCount * 8, time, (currButtons & XINPUT_GAMEPAD_X) >> 14, 1, 2);
				eventCount++;
			}
			if ((currButtons & XINPUT_GAMEPAD_Y) != (prevButtons & XINPUT_GAMEPAD_Y)) {
				nj_put(buf, eventCount * 8, time, (currButtons & XINPUT_GAMEPAD_Y) >> 15, 1, 3);
				eventCount++;
			}
			if ((currButtons & XINPUT_GAMEPAD_LEFT_SHOULDER) != (prevButtons & XINPUT_GAMEPAD_LEFT_SHOULDER)) {
				nj_put(buf, eventCount * 8, time, (currButtons & XINPUT_GAMEPAD_LEFT_SHOULDER) >> 8, 1, 4);
				eventCount++;
			}
			if ((currButtons & XINPUT_GAMEPAD_RIGHT_SHOULDER) != (prevButtons & XINPUT_GAMEPAD_RIGHT_SHOULDER)) {
				nj_put(buf, eventCount * 8, time, (currButtons & XINPUT_GAMEPAD_RIGHT_SHOULDER) >> 9, 1, 5);
				eventCount++;
			}
			if ((currButtons & XINPUT_GAMEPAD_BACK) != (prevButtons & XINPUT_GAMEPAD_BACK)) {
				nj_put(buf, eventCount * 8, time, (currButtons & XINPUT_GAMEPAD_BACK) >> 5, 1, 6);
				eventCount++;
			}
			if ((currButtons & XINPUT_GAMEPAD_START) != (prevButtons & XINPUT_GAMEPAD_START)) {
				nj_put(buf, eventCount * 8, time, (currButtons & XINPUT_GAMEPAD_START) >> 4, 1, 7);
				eventCount++;
			}
			if ((currButtons & XINPUT_GAMEPAD_LEFT_THUMB) != (prevButtons & XINPUT_GAMEPAD_LEFT_THUMB)) {
				nj_put(buf, eventCount * 8, time, (currButtons & XINPUT_GAMEPAD_LEFT_THUMB) >> 6, 1, 9);
				eventCount++;
			}
			if ((currButtons & XINPUT_GAMEPAD_RIGHT_THUMB) != (prevButtons & XINPUT_GAMEPAD_RIGHT_THUMB)) {
				nj_put(buf, eventCount * 8, time, (currButtons & XINPUT_GAMEPAD_RIGHT_THUMB) >> 7, 1, 10);
				eventCount++;
			}
		}

		// check triggers.
		// Xinput reports the trigger state from 0 to 255 while LinuxJoystick expects -32767 to 32767
		SHORT tempVal = controller_state[index].Gamepad.bLeftTrigger;
		SHORT prevVal = saved_state[index].bLeftTrigger;
		if (tempVal != prevVal) {
			if (tempVal != 0) {
				tempVal = (WORD) ((tempVal + 1) * 256 - 1) - 32768;
			}
			else {
				tempVal = -32767;
			}
			nj_put(buf, eventCount * 8, time, tempVal, 2, 2);
			eventCount++;
		}

		tempVal = controller_state[index].Gamepad.bRightTrigger;
		prevVal = saved_state[index].bRightTrigger;
		if (tempVal != prevVal) {
			if (tempVal != 0) {
				tempVal = (WORD)((tempVal + 1) * 256 - 1) - 32768;
			}
			else {
				tempVal = -32767;
			}
			nj_put(buf, eventCount * 8, time, tempVal, 2, 5);
			eventCount++;
		}
		
		// check sticks
		tempVal = controller_state[index].Gamepad.sThumbLX;
		prevVal = saved_state[index].sThumbLX;
		if (tempVal != prevVal) {
			if (tempVal == -32768) {
				tempVal = -32767;
			}
			if (dz_check(prevVal) && !dz_check(tempVal)) {
				tempVal = 0;
				nj_put(buf, eventCount * 8, time, tempVal, 2, 0);
				eventCount++;
			}
			else if (dz_check(tempVal)) {			
				nj_put(buf, eventCount * 8, time, tempVal, 2, 0);
				eventCount++;
			}
		}

		tempVal = controller_state[index].Gamepad.sThumbLY;
		prevVal = saved_state[index].sThumbLY;
		if (tempVal != prevVal) {
			if (tempVal == -32768) {
				tempVal = -32767;
			}			
			tempVal ^= 0xffff; // flip to match LinuxJoystick API
			if (dz_check(prevVal) && !dz_check(tempVal)) {
				tempVal = 0;
				nj_put(buf, eventCount * 8, time, tempVal, 2, 1);
				eventCount++;
			}
			else if (dz_check(tempVal)) {
				if (tempVal == -32768) {
					tempVal = -32767;
				}
				nj_put(buf, eventCount * 8, time, tempVal, 2, 1);
				eventCount++;
			}
		}

		tempVal = controller_state[index].Gamepad.sThumbRX;
		prevVal = saved_state[index].sThumbRX;
		if (tempVal != prevVal) {
			if (tempVal == -32768) {
				tempVal = -32767;
			}
			if (dz_check(prevVal) && !dz_check(tempVal)) {
				tempVal = 0;
				nj_put(buf, eventCount * 8, time, tempVal, 2, 3);
				eventCount++;
			}
			else if (dz_check(tempVal)) {
				nj_put(buf, eventCount * 8, time, tempVal, 2, 3);
				eventCount++;
			}
		}

		tempVal = controller_state[index].Gamepad.sThumbRY;
		prevVal = saved_state[index].sThumbRY;
		if (tempVal != prevVal) {
			if (tempVal == -32768) {
				tempVal = -32767;
			}
			tempVal ^= 0xffff; // flip to match LinuxJoystick API
			if (dz_check(prevVal) && !dz_check(tempVal)) {
				tempVal = 0;
				nj_put(buf, eventCount * 8, time, tempVal, 2, 4);
				eventCount++;
			}
			else if (dz_check(tempVal)) {
				if (tempVal == -32768) {
					tempVal = -32767;
				}
				nj_put(buf, eventCount * 8, time, tempVal, 2, 4);
				eventCount++;
			}
		}
		
		// save current states for the next poll
		CopyMemory(&saved_state[index], &controller_state[index].Gamepad, sizeof(XINPUT_GAMEPAD));

		// create a byte array to be used back in the JVM
		jbyteArray linuxJoyEvents = env->NewByteArray(eventCount * 8);
		env->SetByteArrayRegion(linuxJoyEvents, 0, eventCount * 8, buf);
		return linuxJoyEvents;
	}
}

JNIEXPORT jboolean JNICALL Java_org_bbi_linuxjoy_NoJoy_openNativeDevice
  (JNIEnv *env, jobject obj, jint index)
{
	ZeroMemory(&controller_state[index], sizeof(XINPUT_STATE));

	DWORD r = XInputGetState(index, &controller_state[index]);
	if (r == ERROR_SUCCESS) {
		CopyMemory(&saved_state[index], &controller_state[index].Gamepad, sizeof(XINPUT_GAMEPAD));
		printf("njnative: device %d ready\n", index);
		fflush(stdout);
		return JNI_TRUE;
	} else {
		return JNI_FALSE;
	}
}

JNIEXPORT jboolean JNICALL Java_org_bbi_linuxjoy_NoJoy_isNativeDeviceOpen
  (JNIEnv *env, jobject obj, jint index)
{
	DWORD r = XInputGetState(index, &controller_state[index]);
	if (r == ERROR_SUCCESS) {
		return JNI_TRUE;
	}
	else {
		return JNI_FALSE;
	}
}

JNIEXPORT jintArray JNICALL Java_org_bbi_linuxjoy_NoJoy_enumerate
  (JNIEnv *env, jclass cls)
{
	int i;
	jintArray joyInfo;
	joyInfo = env->NewIntArray(4);
	jint val[4];

	// check if any of the four controllers are ready to be used
	for (i = 0; i < 4; i++) {
		DWORD r = XInputGetState(i, &controller_state[i]);
		if (r == ERROR_SUCCESS) {
			val[i] = XBOX_JOYINFO;
		}
		else {
			val[i] = -1;
		}
	}
	env->SetIntArrayRegion(joyInfo, 0, 4, val);
	return joyInfo;
}

JNIEXPORT jboolean JNICALL Java_org_bbi_linuxjoy_NoJoy_closeNativeDevice
  (JNIEnv *env, jobject obj, jint index)
{
	// MAKE HIM STAY, MURPH!
	// we don't need to de-acquire / etc. with XInput
	return JNI_TRUE;
}

JNIEXPORT jbyteArray JNICALL Java_org_bbi_linuxjoy_NoJoy_setNativeProperty
(JNIEnv *env, jclass cls, jint index, jint key, jint val)
{
	jbyte r = -1;

	if (key == KEY_DEADZONE) {
		hardDeadZone = (WORD)val;
		r = 0;
	}
	else if (key == KEY_VIBRATION && index >= 0 && index < 4) {
		XINPUT_VIBRATION vibration;
		ZeroMemory(&vibration, sizeof(XINPUT_VIBRATION));
		vibration.wRightMotorSpeed = (WORD) (val & 0xFFFF);
		vibration.wLeftMotorSpeed = (WORD)((val>>16) & 0xFFFF);
		XInputSetState(index, &vibration);
		r = 0;
	}
	else if (key == KEY_VERSION) {
		jbyte rv[] = { 0x01, 0x00, 0x00, 0x00 };
		jbyteArray versionReturn = env->NewByteArray(4);
		env->SetByteArrayRegion(versionReturn, 0, 4, rv);
		return versionReturn;

	}

	jbyteArray errorReturn = env->NewByteArray(1);
	env->SetByteArrayRegion(errorReturn, 0, 1, &r);
	return errorReturn;
}

JNIEXPORT jstring JNICALL Java_org_bbi_linuxjoy_NoJoy_getVersionString
(JNIEnv *env, jclass cls)
{
	return env->NewStringUTF(version);
}
