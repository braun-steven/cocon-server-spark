## Cocon Spark Server
This repository contains the maven project of the spark java REST server for the [Cocon](https://gitlab.com/Tak3r07/cocon-android) android application.

## Some setup details
To build the project run
```bash
$ mvn package
```

You may have to install the corresponding cocon-base jar file to your local .m2 repository first
```bash
$ mvn install:install-file -Dfile=<path-to-jar> -DgroupId=com.tak3r07 
-DartifactId=cocon-base -Dversion=1.0-SNAPSHOT -Dpackaging=jar
```
cocon-base can now be included with
```xml
<dependency>
    <groupId>com.tak3r07</groupId>
    <artifactId>cocon-base</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

After the cocon-server jar has been built successfully run the following to 
start the server
```bash
java -jar <path-to-jar> <PORT>
```
