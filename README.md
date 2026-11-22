# ELECTRA AI — S25 Edge Edition

Native Android project for the Samsung Galaxy S25 Edge.

## Current milestone: Core 0.1
- Native Android application shell with a dark cyber/neon-green interface.
- Reads Android-provided device identity, OS version, battery level/status/temperature/voltage, RAM availability, internal storage, active network type, available sensor names, and Android thermal status.
- Manual refresh; read-only diagnostics. No account, cloud upload, root, ADB, or special device permission is required for these checks.

## Important limits
This first milestone is a real native Android project, not yet a finished APK. The current diagnostics read system-reported information; they do not prove battery health or detect physical faults in the display, cameras, speakers, USB connector, or motherboard. Sensor names are listed, but sensor self-tests are not implemented yet. AI server analysis, repair actions, USB accessory diagnostics, and Samsung-specific service integration are not implemented.

ELECTRA will never bypass FRP, screen locks, Activation Lock, or anti-theft controls, and will not change IMEI or flash firmware automatically.

## Build
Open this folder in Android Studio or another Android Gradle environment with JDK 17 and Android SDK 35. Gradle 8.9 and Android Gradle Plugin 8.7.3 are configured. The Gradle wrapper properties are included; wrapper launcher scripts/JAR are not included in this source milestone.
