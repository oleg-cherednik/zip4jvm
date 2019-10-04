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

To simplify usage of _zip4jvm_, there're following classes:
* [ZipIt](#zipit) - add files to archive;
* UnzipIt - extract files from archive;
* ZipMisc - other zip file activities. These classes contains most common operations with limited set of settings.
 
### ZipIt

#### Regular files and directories can be represented as `Path` 

##### Create (or open existed) zip archive and add regular file */cars/bentley-continental.jpg*.
  
```
Path zip = Paths.get("filename.zip");
Path file = Path.get("/cars/bentley-continental.jpg")
ZipIt.zip(zip).add(file);
```
>```
> filename.zip
>  |-- bentley-continental.jpg
>```

**Note:** regular file is added to the root of the zip archive.

##### Create (or open existed) zip archive and add directory */cars*.

```
Path zip = Paths.get("filename.zip");
Path dir = Path.get("/catalog/cars")
ZipIt.zip(zip).add(dir);
```
>```
> filename.zip
>  |-- cars
>       |-- bentley-continental.jpg
>       |-- feffari-458-italia.jpg
>       |-- wiesmann-gt-mf5.jpg 
>```

**Note:** directory is added to the root of the zip archive keeping the initial structure.

##### Create (or open existed) zip archive and add some regular files and/or directories.

```
Path zip = Paths.get("filename.zip");
Collection<Path> paths = Arrays.asList(
        Paths.get("/bikes/ducati-panigale-1199.jpg"),
        Paths.get("/bikes/honda-cbr600rr.jpg"),
        Paths.get("/cars"),
        Paths.get("/saint-petersburg.jpg"));
ZipIt.zip(zip).add(paths);
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

**Note:** each regular file from the list is added to the root of the zip archive.

**Note:** each directory from the list is added to the root of the zip archive keeping the initial structure. 

##### Create (or open existed) zip archive and add some regular files and/or directories using stream.
  
```
Path zip = Paths.get("filename.zip");
try (ZipFile.Writer zipFile = ZipIt.zip(zip).stream()) {
    zipFile.add(Paths.get("/bikes/ducati-panigale-1199.jpg"));
    zipFile.add(Paths.get("/bikes/honda-cbr600rr.jpg"));
    zipFile.add(Paths.get("/cars"));
    zipFile.add(Paths.get("/saint-petersburg.jpg");
}
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

**Note:** each regular file from the list is added to the root of the zip archive.

**Note:** each directory from the list is added to the root of the zip archive keeping the initial structure.

#### Regular files and empty directories are available as `InputStream`

##### Create (or open existed) zip archive and add input stream content as regular files.

```
ZipFile.Entry entry = ZipFile.Entry.builder()
                                   .inputStreamSupplier(() -> new FileInputStream("/cars/bentley-continental.jpg"))
                                   .fileName("my_cars/bentley-continental.jpg")
                                   .lastModifiedTime(System.currentTimeMillis()).build();
Path zip = Paths.get("filename.zip");
ZipIt.zip(zip).addEntry(entry);
```
>```
> filename.zip
>  |-- my_cars
>  |    |-- bentley-continental.jpg
>```  

**Note:** any content form input stream is treated as regular file with given full name.

##### Create (or open existed) zip archive and add input streams content as regular files.

```
ZipFile.Entry entryBentley = ZipFile.Entry.builder()
                                          .inputStreamSupplier(() -> new FileInputStream("/cars/bentley-continental.jpg"))
                                          .fileName("my_cars/bentley-continental.jpg")
                                          .lastModifiedTime(System.currentTimeMillis()).build();

ZipFile.Entry entryKawasaki = ZipFile.Entry.builder()
                                           .inputStreamSupplier(() -> new FileInputStream("/bikes/kawasaki-ninja-300.jpg"))
                                           .fileName("my_bikes/kawasaki.jpg")
                                           .lastModifiedTime(System.currentTimeMillis()).build();

List<ZipFile.Entry> entries = Arrays.asList(entryBentley, entryKawasaki);

Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("filename.zip");
ZipIt.zip(zip).addEntry(entries);
```
>```
> filename.zip
>  |-- my_cars
>  |    |-- bentley-continental.jpg
>  |-- my_bikes
>  |    |-- kawasaki.jpg
>```  

**Note:** each entry is treated as separate input stream of the regular file.   

#### Zip file settings: `ZipFileSettings`

All zip operations include `ZipFileSettings`. [Default setings](#zip-file-settings-defaults) is
used when it's not explicitly set. Settings contains zip archive scope properties as well as
provider for entry specific settings. The key for entry settings is **fileName**.

**Note:** user should not worry about directory marker `/`, because `zip4jvm` does not support
duplicated file names and it's impossible to have same file name for file and directory.

 - _splitSize_ - `-1` solid archive or split archive with given size for each part
   - _min size_ - `64 * 1024 = 64Kb` 
 - _comment_ - `null`, i.e. no comment
   - _no comment_ - `null` or `empty string`
   - _max length_ - `65,535` symbols 
 - _zip64_ - use `true` or not `false` zip64 format for global zip structure
   - **Note:** _zip64_ is switched on automatically if needed
   - **Note:** it does not mean that entry structure is in _zip64_ format as well
 - _entrySettingsProvider_ - file name base provider of settings for entry
   - **Note:** each entry could have different settings 

#### Zip file settings defaults

 - _splitSize_ - `-1`, i.e. off or solid archive
 - _comment_ - `null`, i.e. no comment
 - _zip64_ - `false`, i.e. standard format for global zip structure
   - **Note:** _zip64_ is switched on automatically if needed
   - **Note:** it does not mean that entry structure is in _zip64_ format as well
 - _entrySettingsProvider_ - `default`, i.e. all entries has same default entry settings 

#### Zip entry settings: `ZipEntrySettings`

Each entry has it's own settings. These settings could be different for every entry. If this settings
are not explicitly set, then `default` entry settings are used for all added entries.

#### Zip entry settings defaults

 - _compression_ - `deflate`
 - _compressionLevel_ - `normal`
 - _encryption_ - `off`, i.e. no encryption
 - _comment_ - `null`, i.e. no comment
 - _zip64_ - `false`, i.e. standard format for entry structure
 - _utf8_ - `true`, i.e. entry's name and comment are stored using `UTF8` charset   

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
