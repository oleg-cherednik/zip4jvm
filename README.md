[![Maven Central](https://maven-badges.herokuapp.com/maven-central/ru.oleg-cherednik.zip4jvm/zip4jvm/badge.svg)](https://maven-badges.herokuapp.com/maven-central/ru.oleg-cherednik.zip4jvm/zip4jvm)
[![Build Status](https://travis-ci.org/oleg-cherednik/zip4jvm.svg?branch=master)](https://travis-ci.org/oleg-cherednik/zip4jvm)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![codecov](https://codecov.io/gh/oleg-cherednik/zip4jvm/branch/master/graph/badge.svg)](https://codecov.io/gh/oleg-cherednik/zip4jvm)
[![Known Vulnerabilities](https://snyk.io//test/github/oleg-cherednik/zip4jvm/badge.svg?targetFile=build.gradle)](https://snyk.io//test/github/oleg-cherednik/zip4jvm?targetFile=build.gradle)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/7b6b963fef254ff4b00b8be0304e829b)](https://www.codacy.com/app/oleg-cherednik/zip4jvm?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=oleg-cherednik/zip4jvm&amp;utm_campaign=Badge_Grade)

# zip4jvm

Zip files support for JVM application

### Maven

~~~~
<dependency>
    <groupId>ru.oleg-cherednik.zip4jvm</groupId>
    <artifactId>zip4jvm</artifactId>
    <version>0.7</version>
</dependency>
~~~~

### Gradle

~~~~
compile 'ru.oleg-cherednik.zip4jvm:zip4jvm:0.7'
~~~~

## Usage
### ZipIt (Standard Mode)
#### Create (or open existed) zip archive and add file */cars/bentley-continental.jpg*.
  
```
Path zip = Paths.get("filename.zip");
Path file = Path.get("/cars/bentley-continental.jpg")
ZipIt.add(zip, file);
```
>```
> filename.zip
>  |-- bentley-continental.jpg
>```

#### Create (or open existed) zip archive and add directory */cars*.

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

#### Create (or open existed) zip archive and add some files and/or directories.

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
.e. _empty directory_, because no need to add additional entity for each directory with content).

Entry contains following data (all fields are mandatory):

* **inputStreamSup** - input stream supplier; it should retrieve `null` or `InputStream`;
* **fileName** - full file name of the entry relative to the root of the zip archive (i.e.
 `cars/bentley-continental.jpg`);
* **lastModifiedTime** - last modification time (by default it's `System.currentTimeMillis()`);
* **regularFile** - `true` if entry is a regular file; internally zip adds special marker `/` to
 the **fileName** for directory and **inputStreamSup** result will be ignored (i.e. directory
  cannot have `InputStream`). **Note**, the no need to add marker `/` to the **fileName
  ** manually - this is internal representation; _zip4jvm_ retrieves this instance without this
   marker.  

##### Links
* Home page: https://github.com/oleg-cherednik/zip4jvm
* Maven:
  * **central:** https://mvnrepository.com/artifact/ru.oleg-cherednik.zip4jvm/zip4jvm
  * **download:** http://repo1.maven.org/maven2/ru/oleg-cherednik/zip4jvm/zip4jvm/
