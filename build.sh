#!/bin/bash
# Compila Reloj Gordo sin Gradle ni Android Studio. Requiere JDK 17+ y ANDROID_HOME con
# platforms;android-35 y build-tools;35.0.0
set -euo pipefail
cd "$(dirname "$0")"
SDK=${ANDROID_HOME:-$HOME/android-sdk}
BT=$SDK/build-tools/35.0.0
JAR=$SDK/platforms/android-35/android.jar

rm -rf build && mkdir -p build/{res,gen,classes,dex}

"$BT/aapt2" compile --dir res -o build/res/res.zip
"$BT/aapt2" link -I "$JAR" --manifest AndroidManifest.xml build/res/res.zip \
  --java build/gen -o build/unsigned.apk --min-sdk-version 29 --target-sdk-version 35

javac -nowarn -Xlint:-options -source 8 -target 8 -bootclasspath "$JAR" -classpath "$JAR:$BT/core-lambda-stubs.jar" \
  -d build/classes $(find src build/gen -name '*.java')

"$BT/d8" --release --min-api 29 --lib "$JAR" --output build/dex $(find build/classes -name '*.class')
(cd build/dex && zip -q ../unsigned.apk classes.dex)

"$BT/zipalign" -f 4 build/unsigned.apk build/aligned.apk

# Clave de firma: NUNCA va al repo. En CI llega desde los secrets de GitHub.
KS=${KS_FILE:-relojgordo.keystore}
KS_PASS=${KS_PASS:?Define KS_PASS con la contraseña de la clave de firma}
if [ ! -f "$KS" ]; then
  [ -n "${CI:-}" ] && { echo "Falta la clave de firma (secret KEYSTORE_B64)"; exit 1; }
  keytool -genkeypair -keystore "$KS" -alias reloj -keyalg RSA -keysize 4096 -validity 10000 \
    -storepass "$KS_PASS" -keypass "$KS_PASS" -dname "CN=Reloj Gordo" >/dev/null 2>&1
fi

"$BT/apksigner" sign --ks "$KS" --ks-pass "pass:$KS_PASS" \
  --out RelojGordo.apk build/aligned.apk
"$BT/apksigner" verify RelojGordo.apk && echo "OK -> RelojGordo.apk"
