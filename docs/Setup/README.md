# Setup 
## Without Docker
Since the stable version of Soot does not support Java 9+ (with modules) yet, make sure that the version of your Java is not higher than 8. You can use [jEnv](https://www.jenv.be) to manage different versions of Java in your machine. If you do not have Java 8, install openjdk8 first (for [Linux](https://openjdk.java.net/install/), and for [Mac](https://apple.stackexchange.com/a/334385)).

* `git clone git@github.com:noidsirius/SootTutorial.git`
* `cd SootTutorial`
* (Optional) if you want to use `jEnv` to manage Java versions (e.g. the installed Java version is `1.8.0_202`)
  * `jenv local 1.8.0_202`
* (Optional) to verify everything is set up correctly
  * `./gradlew check`

### Android
For codes that analyze Android apps, you need to install [Android SDK](https://developer.android.com/studio), [build-tools 29.0.3](https://developer.android.com/studio/releases/build-tools), and [adb](https://developer.android.com/studio/command-line/adb). Set the environmental variable `ANDROID_HOME` to the path to Android SDK. For example in MacOSX you can set it by `export ANDROID_HOME=~/Library/Android/sdk/`.

## With Docker

- Use the Docker image from docker hub (`docker run -it noidsirius/soot_tutorial:latest`) or
- Build the image locally (`docker build . -t soot_tutorial`) and run it (`docker run -it soot_tutorial`)
