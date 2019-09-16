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

### Settings

#### Zip archive

`ZipFileSettings` represents entire zip archive settings:
  - **splitSize** - 
  - **comment**
  - defEntrySettings

#### Create zip archive with given file/directory

##### Create (or add to existed) zip archive and add file */cars/bentley-continental.jpg*.  
```
Path zip = Paths.get("filename.zip");
Path file = Path.get("/cars/bentley-continental.jpg")
ZipIt.add(zip, file, ZipFileSettings.builder().build());
```
> ```
> /--
>  |-- bentley-continental.jpg
>```

##### Create (or add to existed) zip archive and add directory */cars*.
```
Path zip = Paths.get("filename.zip");
Path dir = Path.get("/cars")
ZipIt.add(zip, dir, ZipFileSettings.builder().build());
```
> ```
> /--
>  |-- cars
>       |-- bentley-continental.jpg
>       |-- feffari-458-italia.jpg
>       |-- wiesmann-gt-mf5.jpg 
>```

##### Create (or add to existed) zip archive and add directory files and directories.
```
Path zip = Paths.get("filename.zip");
Collection<Path> paths = Arrays.asList("/cars", "/Star Wars/one.jpg", "/Star Wars/two.jpg", "saint-petersburg.jpg");
ZipIt.add(zip, paths, ZipFileSettings.builder().build());
```
> ```
> /--
>  |-- cars
>  |    |-- bentley-continental.jpg
>  |    |-- feffari-458-italia.jpg
>  |    |-- wiesmann-gt-mf5.jpg
>  |-- one.jpg
>  |-- two.jpg
>  |-- saint-petersburg.jpg 
>```

 

#### Create zip archive with given files
#### Add given files to the existed zip archive 

```
compile 'ru.oleg-cherednik.zip4jvm:zip4jvm:1.0'
```
 

### Advanced Mode 
