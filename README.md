[![Maven Central](https://maven-badges.herokuapp.com/maven-central/ru.oleg-cherednik.zip4jvm/zip4jvm/badge.svg)](https://maven-badges.herokuapp.com/maven-central/ru.oleg-cherednik.zip4jvm/zip4jvm)
[![javadoc](https://javadoc.io/badge2/ru.oleg-cherednik.zip4jvm/zip4jvm/javadoc.svg)](https://javadoc.io/doc/ru.oleg-cherednik.zip4jvm/zip4jvm)
[![java1.8](https://badgen.net/badge/java/1.8/blue)](https://badgen.net/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)

[![github-ci](https://github.com/oleg-cherednik/zip4jvm/actions/workflows/github-ci.yml/badge.svg?branch=master&event=push)](https://github.com/oleg-cherednik/zip4jvm/actions)
[![license-scan](https://app.fossa.com/api/projects/git%2Bgithub.com%2Foleg-cherednik%2Fjson-api.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2Foleg-cherednik%2Fjson-api?ref=badge_shield)
[![quality](https://app.codacy.com/project/badge/Grade/7b6b963fef254ff4b00b8be0304e829b?branch=master)](https://app.codacy.com/gh/oleg-cherednik/zip4jvm/dashboard?branch=master)
[![coverage](https://app.codacy.com/project/badge/Coverage/7b6b963fef254ff4b00b8be0304e829b?branch=master)](https://app.codacy.com/gh/oleg-cherednik/zip4jvm/coverage/dashboard?branch=master)

<details><summary>develop</summary>
<p>

[![github-ci](https://github.com/oleg-cherednik/zip4jvm/actions/workflows/github-ci.yml/badge.svg?branch=develop&event=push)](https://github.com/oleg-cherednik/zip4jvm/actions)
[![quality](https://app.codacy.com/project/badge/Grade/7b6b963fef254ff4b00b8be0304e829b?branch=develop)](https://app.codacy.com/gh/oleg-cherednik/zip4jvm/dashboard?branch=develop)
[![coverage](https://app.codacy.com/project/badge/Coverage/7b6b963fef254ff4b00b8be0304e829b?branch=develop)](https://app.codacy.com/gh/oleg-cherednik/zip4jvm/coverage/dashboard?branch=develop)

</p>
</details>

<p align="center">
    <a href="https://github.com/oleg-cherednik/zip4jvm/blob/master/img/zip4jvm_qr.png?raw=true">
        <img alt="QR-code" src="img/zip4jvm_qr_small.png" />
    </a>
</p>

# zip4jvm - a java library for working with zip files

## Features

* Add regular files or directories to new or existed zip archive;

* Extract regular files or directories from zip archive;

* Encryption algorithms support:
  * [PKWare](https://en.wikipedia.org/wiki/PKWare)
  * [AES](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard)

* Compression support:
  * STORE
  * [DEFLATE (default)](https://en.wikipedia.org/wiki/DEFLATE)
  * [ENHANCED DEFLATE](http://deflate64.com) (read-only)
  * [BZIP2](https://en.wikipedia.org/wiki/Bzip2)
  * [LZMA](https://en.wikipedia.org/wiki/Lempel%E2%80%93Ziv%E2%80%93Markov_chain_algorithm)
  * [ZSTD](https://en.wikipedia.org/wiki/Zstandard)

* Individual settings for each zip entry (i.e. some of the files can be
  encrypted, and some - not);

* Streaming support for adding and extracting;

* Read/Write password-protected Zip files and streams;

* [ZIP64](https://en.wikipedia.org/wiki/Zip_(file_format)#ZIP64) format support;

* Multi-volume zip archive support:
  * [PKWare](https://pkware.cachefly.net/webdocs/casestudies/APPNOTE.TXT), i.e. `filename.zip`, `filename.z01`, `filename.z02`
  * [7-Zip](https://en.wikipedia.org/wiki/7-Zip#Features), i.e. `filename.zip.001`, `filename.zip.002`, `filename.zip.003` (read-only)

* Unicode for comments and file names.
