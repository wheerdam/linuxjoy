#!/bin/bash

if [ ! $# -eq 2 ]
then
	echo "Usage: ./gen-cpp-template.sh [input.h] [output.cpp]"
	exit -1
fi

if [ ! -e $1 ]
then
	echo "$1 not found"
	exit -1
fi

if [ -e $2 ]
then
	echo "$2 already exists"
	exit -1
fi

echo "#include \"$1\"" > $2
echo "#include <jni.h>" >> $2
echo "#include <stdio.h>" >> $2
echo "" >> $2

while IFS='' read -r line || [[ -n "$line" ]]; do
	echo "$line" | sed -n '/JNIEXPORT/p' >> $2
	echo "$line" | sed -n 's/JNIEnv \*/JNIEnv \*env/p' \
			     | sed 's/jobject/jobject obj/' \
			     | sed 's/jclass/jclass cls/' \
			     | sed 's/jint/jint index/g' \
			     | sed 's/;/\n\{\n\n\}\n/' >> $2
done < "$1"
