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
    <version>1.0</version>
</dependency>
~~~~

### Gradle

~~~~
compile 'ru.oleg-cherednik.zip4jvm:zip4jvm:1.0'
~~~~

## Usage

### ZipIt (Standard Mode)

##### Create (or add to existed) zip archive and add file */cars/bentley-continental.jpg*.  
```
Path zip = Paths.get("filename.zip");
Path file = Path.get("/cars/bentley-continental.jpg")
ZipIt.add(zip, file);
```
>```
> filename.zip
>  |-- bentley-continental.jpg
>```

##### Create (or add to existed) zip archive and add directory */cars*.
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

##### Create (or add to existed) zip archive and add some files and/or directories.
```
Path zip = Paths.get("filename.zip");
Collection<Path> paths = Arrays.asList(
        Paths.get("/cars"),
        Paths.get("/Star Wars/one.jpg"),
        Paths.get("/Star Wars/two.jpg"),
        Paths.get("/saint-petersburg.jpg"));
ZipIt.add(zip, paths);
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

> **ZipFileSettings** could be additionally set for all methods. See default settings.

_**Note:** see [ZipIt (Advanced Mode)](#zipit-advanced-mode) for using extended zip it operations_              

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

### ZipIt (Advanced Mode)

### UnzipIt (Advanced Mode)

### CreatePassword function
