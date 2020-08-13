cwd="$PWD"
cd ../../android-native-protocol
bash build_android_libraries.sh
cd "$cwd"
cp ../../android-native-protocol/target/aarch64-linux-android/release/libandroid_native_protocol.so libs/arm64-v8a
cp ../../android-native-protocol/target/armv7-linux-androideabi/release/libandroid_native_protocol.so libs/armeabl-v7a
cp ../../android-native-protocol/target/i686-linux-android/release/libandroid_native_protocol.so libs/x86
