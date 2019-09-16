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

Create new zip archive with given *settings* and add existed file or directory.  
```
Path 
ZipIt.add(zip, path, settings);
```
 

#### Create zip archive with given files
#### Add given files to the existed zip archive 

```
compile 'ru.oleg-cherednik.zip4jvm:zip4jvm:1.0'
```
 

### Advanced Mode 
