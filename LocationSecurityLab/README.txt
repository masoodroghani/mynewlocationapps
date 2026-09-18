LOCATION SECURITY LAB

Contains two independent Android Studio projects:
1. AppA-LocationSimulator - controlled mock/test location provider.
2. AppB-LocationDetector - receives locations and displays mock/anomaly indicators.

SETUP
- Open each folder separately in Android Studio and allow Gradle sync.
- Install both on a test Android device.
- Grant App B precise location permission.
- Enable Developer Options on the device.
- Developer Options > Select mock location app > Location Simulator.
- Open App A, enter coordinates and start static or A-to-B simulation.
- Open App B and press Start Monitoring.

EXPECTED RESULT
App B should receive App A's test coordinates and normally display isMock=true.

IMPORTANT
App A deliberately uses Android's supported mock/test-location mechanism. It does not hide the mock flag, modify Android, bypass integrity checks, or attempt to defeat anti-spoofing protections.

If Android Studio proposes compatible Gradle/SDK dependency upgrades, they can be accepted. compileSdk/targetSdk are currently set to 35.
