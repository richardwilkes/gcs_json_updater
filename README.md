## GCSJsonUpdater

Updates data files created by GCS prior to version 4.20 to the 4.20 format.

### Building from the command line

1. Make sure you have JDK 15 installed and set to be used as your default Java compiler. You can
   download it for your platform here: http://jdk.java.net/15/

2. If you are building on Windows, you'll need to install the WiX Toolset from here:
   https://github.com/wixtoolset/wix3/releases/tag/wix3112rtm

3. Clone the source repositories:
   ```
   % git clone https://github.com/richardwilkes/gcs_json_updater
   ```

4. Build and bundle the code for your platform:

   macOS and Linux:
   ```
   % cd gcs
   % ./bundle.sh
   ```
   Windows:
   ```
   > cd gcs
   > .\bundle.bat
   ```
