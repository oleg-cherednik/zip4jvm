[![Maven Central](https://maven-badges.herokuapp.com/maven-central/ru.oleg-cherednik.zip4jvm/zip4jvm/badge.svg)](https://maven-badges.herokuapp.com/maven-central/ru.oleg-cherednik.zip4jvm/zip4jvm)
[![Build Status](https://travis-ci.org/oleg-cherednik/zip4jvm.svg?branch=master)](https://travis-ci.org/oleg-cherednik/zip4jvm)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![codecov](https://codecov.io/gh/oleg-cherednik/zip4jvm/branch/dev/graph/badge.svg)](https://codecov.io/gh/oleg-cherednik/zip4jvm)
[![Known Vulnerabilities](https://snyk.io//test/github/oleg-cherednik/zip4jvm/badge.svg?targetFile=build.gradle)](https://snyk.io//test/github/oleg-cherednik/zip4jvm?targetFile=build.gradle)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/7b6b963fef254ff4b00b8be0304e829b)](https://www.codacy.com/app/oleg-cherednik/zip4jvm?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=oleg-cherednik/zip4jvm&amp;utm_campaign=Badge_Grade)
[![coverity](https://scan.coverity.com/projects/4735/badge.svg)](https://scan.coverity.com/projects/oleg-cherednik-zip4jvm)

zip4jvm - a java library for working with zip files
=====================

## Features

* Add regular files or directories to new or existed zip archive;
* Extract regular files or directories from zip archive;
* [PKWare](https://en.wikipedia.org/wiki/PKWare) and [AES](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard) encryption algorithms support;
* Store and [Deflate](https://en.wikipedia.org/wiki/DEFLATE) compression support;  
* Individual settings for each zip entry (i.e. some of files can be encrypted, and some - not);
* Streaming support for adding and extracting; 
* Read/Write password protected Zip files and streams;
* [ZIP64](https://en.wikipedia.org/wiki/Zip_(file_format)#ZIP64) format support;
* Multi-volume zip archive support (i.e. `filename.zip`, `filename.z01`, `filename.z02`);
* Unicode for comments and file names.

## Gradle

~~~~
compile 'ru.oleg-cherednik.zip4jvm:zip4jvm:0.7'
~~~~

## Maven

~~~~
<dependency>
    <groupId>ru.oleg-cherednik.zip4jvm</groupId>
    <artifactId>zip4jvm</artifactId>
    <version>0.7</version>
</dependency>
~~~~

## Usage

### Standard Mode

To simplify usage of _zip4jvm_, there're utility classes:
* [ZipIt](#zipit) - add files to archive;
* UnzipIt - extract files from archive;
* ZipMisc - other zip file activities. These classes contains most common operations with limited set of settings.
 
#### ZipIt
##### Create (or open existed) zip archive and add file */cars/bentley-continental.jpg*.
  
```
Path zip = Paths.get("filename.zip");
Path file = Path.get("/cars/bentley-continental.jpg")
ZipIt.add(zip, file);
```
>```
> filename.zip
>  |-- bentley-continental.jpg
>```

##### Create (or open existed) zip archive and add directory */cars*.

```
Path zip = Paths.get("filename.zip");
Path dir = Path.get("/cars")
ZipIt.add(zip, dir);
```
>```
> filename.zip
>  |-- cars
>       |-- bentley-continental.jpg
>       |-- feffari-458-italia.jpg
>       |-- wiesmann-gt-mf5.jpg 
>```

##### Create (or open existed) zip archive and add some files and/or directories.

```
Path zip = Paths.get("filename.zip");
Collection<Path> paths = Arrays.asList(
        Paths.get("/bikes/ducati-panigale-1199.jpg"),
        Paths.get("/bikes/honda-cbr600rr.jpg"),
        Paths.get("/cars"),
        Paths.get("/saint-petersburg.jpg"));
ZipIt.add(zip, paths);
```
>```
> filename.zip
>  |-- cars
>  |    |-- bentley-continental.jpg
>  |    |-- feffari-458-italia.jpg
>  |    |-- wiesmann-gt-mf5.jpg
>  |-- ducati-panigale-1199.jpg
>  |-- honda-cbr600rr.jpg
>  |-- saint-petersburg.jpg 
>```

**Note:** added directories will hold the initial structure.

**Note:** added files will be added to the root of the zip archive.

##### Create (or open existed) zip archive and add file */cars/bentley-continental.jpg* using stream.
  
```
Path zip = Paths.get("filename.zip");
Path file = Path.get("/cars/bentley-continental.jpg")
ZipIt.add(zip, file);
```
>```
> filename.zip
>  |-- bentley-continental.jpg
>```

> **ZipFileSettings** could be additionally set for all methods. See default settings.

**Note:** _see [ZipIt (Advanced Mode)](#zipit-advanced-mode) for using extended zip it operations
._              

### UnzipIt (Standard Mode)

##### Extract all entries into given directory.
```
Path zip = Paths.get("filename.zip");
Path destDir = Paths.get("/filename_content");
UnzipIt.extract(zip, destDir);
```
>```
> filename.zip
>  |-- cars
>  |    |-- bentley-continental.jpg
>  |    |-- feffari-458-italia.jpg
>  |    |-- wiesmann-gt-mf5.jpg
>  |-- one.jpg
>  |-- two.jpg
>  |-- saint-petersburg.jpg 
>```
>```
> /filename_content
>   |-- cars
>   |    |-- bentley-continental.jpg
>   |    |-- feffari-458-italia.jpg
>   |    |-- wiesmann-gt-mf5.jpg
>   |-- one.jpg
>   |-- two.jpg
>   |-- saint-petersburg.jpg 
>```                                                    
 
##### Extract a file into given directory.
```
Path zip = Paths.get("filename.zip");
Path destDir = Paths.get("/filename_content");
UnzipIt.extract(zip, destDir, "cars/bentley-continental.jpg");
```
>```
> filename.zip
>  |-- cars
>  |    |-- bentley-continental.jpg
>  |    |-- feffari-458-italia.jpg
>  |    |-- wiesmann-gt-mf5.jpg
>  |-- one.jpg
>  |-- two.jpg
>  |-- saint-petersburg.jpg 
>```
>```
> /filename_content
>   |-- bentley-continental.jpg
>```

##### Extract one directory into given directory.
```
Path zip = Paths.get("filename.zip");
Path destDir = Paths.get("/filename_content");
UnzipIt.extract(zip, destDir, "cars");
```
>```
> filename.zip
>  |-- cars
>  |    |-- bentley-continental.jpg
>  |    |-- feffari-458-italia.jpg
>  |    |-- wiesmann-gt-mf5.jpg
>  |-- one.jpg
>  |-- two.jpg
>  |-- saint-petersburg.jpg 
>```
>```
> /filename_content
>   |-- cars
>   |    |-- bentley-continental.jpg
>   |    |-- feffari-458-italia.jpg
>   |    |-- wiesmann-gt-mf5.jpg
>```

##### Extract some files and/or directories into given directory.
```
Path zip = Paths.get("filename.zip");
Path destDir = Paths.get("/filename_content");
Collection<Path> fileNames = Arrays.asList(
        "cars",
        "Star Wars/one.jpg",
        "saint-petersburg.jpg");
UnzipIt.extract(zip, destDir, fileName);
```
>```
> filename.zip
>  |-- cars
>  |    |-- bentley-continental.jpg
>  |    |-- feffari-458-italia.jpg
>  |    |-- wiesmann-gt-mf5.jpg
>  |-- Star Wars
>  |    |-- one.jpg
>  |    |-- two.jpg
>  |-- saint-petersburg.jpg 
>```
>```
> /filename_content
>   |-- cars
>   |    |-- bentley-continental.jpg
>   |    |-- feffari-458-italia.jpg
>   |    |-- wiesmann-gt-mf5.jpg
>   |-- one.jpg
>   |-- two.jpg            
>   |-- saint-petersburg.jpg
>```                     
_**Note:** `CreatePassword` function could be optionally added to all methods. See details in [CreatePassword function](#createpassword-function)._
_**Note:** see [UnzipIt (Advanced Mode)](#unzipit-advanced-mode) for using extended unzip it operations._

### ZipIt (Advanced Mode)

### UnzipIt (Advanced Mode)

### CreatePassword function

## Model
### ZipFile.Entry

This is a user friendly definition of the zip entry. It could be a regular file or a directory (i
.e. _empty directory_, because no need to add additional entry for each directory with content).

* **inputStreamSup** - input stream supplier; it should retrieve `null` or `InputStream`;
* **fileName** - full file name of the entry relative to the root of the zip archive (i.e.
 `cars/bentley-continental.jpg`);
* **lastModifiedTime** - last modification time _(by default it's `System.currentTimeMillis()`)_;
* **regularFile** - `true` if entry is a regular file; internally zip adds special marker `/` to
 the _**fileName**_ for directory and _**inputStreamSup**_ result is ignored (i.e. directory
  cannot have `InputStream`). _Note:_ no need to add marker `/` to the _**fileName**_ manually -
  this is internal representation; _zip4jvm_ retrieves this instance without this marker.  

##### Links
* Home page: https://github.com/oleg-cherednik/zip4jvm
* Maven:
  * **central:** https://mvnrepository.com/artifact/ru.oleg-cherednik.zip4jvm/zip4jvm
  * **download:** http://repo1.maven.org/maven2/ru/oleg-cherednik/zip4jvm/zip4jvm/
