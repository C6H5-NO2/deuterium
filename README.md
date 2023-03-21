<div align="center">
  <img src="./src/main/resources/ic_launcher.png"/>
  <p>Two parts, One element</p>
</div>

# Deuterium
Deuterium is an editor for Kotlin script. Users can write a script, execute it, and view its output.



# Implemented Functionality
- [x] Have an editor panel and an output panel.
- [x] Allow users to write script to file and run it using [kotlinc](https://kotlinlang.org/docs/command-line.html#run-scripts).
- [x] Show live output as the script executes.
- [x] Show error messages.
- [x] Show an indication of whether the script is running.
- [x] Show an indication of whether the exit code is non-zero.

　

- [x] Highlight [hard keywords](https://kotlinlang.org/docs/keyword-reference.html#hard-keywords), numbers, and line comments.
- [x] Link location descriptions to cursor positions in code.
- [x] Allow users to run script multiple times. Show an estimated remaining time.



# Build and Run
Require JDK 11, Kotlin 1.8.0, Compose Multiplatform 1.3.0. Gradle version \> 7.5 should work.

```bash
cd /path/to/deuterium
gradle wrapper
gradlew run
# or
gradlew packageReleaseUberJarForCurrentOS
java -jar ./build/compose/jars/[name].jar
```



# Clarification
This project starts with [Code Viewer](https://github.com/JetBrains/compose-multiplatform/tree/master/examples/codeviewer) as base-code. Code Viewer is an example application provided by the Compose Multiplatform framework. It comes with the code for displaying a file in read-only mode and offers an user interface for file tree and text view. Functionalities other than those are newly implemented. The contributions can be viewed in detail through the following comparisons [1](https://github.com/C6H5-NO2/deuterium/compare/375a3df94f06faf9b0a2db66d936f7af0f505f00..92e4ee989bb10c91a24c61fd68375931d9d19c18), [2](https://github.com/C6H5-NO2/deuterium/compare/f5b20988705eea9ca97ee938cee522e23d32c0c3..main).
