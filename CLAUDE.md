# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

zip4jvm is a Java library (Java 8+) for working with ZIP files. It supports reading/writing ZIP archives with multiple compression algorithms (STORE, DEFLATE, BZIP2, LZMA, ZSTD), encryption (PKWare, AES), ZIP64, split archives (PKWare and 7-Zip formats), streaming, and multi-threaded extraction.

## Build and Test Commands

```bash
# Build
./gradlew build

# Run all tests
./gradlew test

# Run a specific test class
./gradlew test --tests "ru.olegcherednik.zip4jvm.ZipFileTest"

# Run a specific test method
./gradlew test --tests "ru.olegcherednik.zip4jvm.ZipFileTest.methodName"

# Run checks (tests + checkstyle + PMD + license report + jacoco)
./gradlew check

# Generate javadoc
./gradlew javadoc
```

Tests use TestNG (not JUnit). The test suite is configured in `src/test/resources/testng.xml` and runs with `parallel="classes" thread-count="4"`.

## Architecture

### Public API (entry points in `src/main/java/ru/olegcherednik/zip4jvm/`)

- **`ZipIt`** – add files/directories/streams to zip archives
- **`UnzipIt`** – extract entries from zip archives (to files or as `InputStream`)
- **`ZipMisc`** – misc operations: get/set comment, list entries, remove entries, merge split archive
- **`ZipInfo`** – diagnostic: print zip structure details, decompose archive into raw data files
- **`ZipFile`** – inner class used by `ZipIt.execute(ZipFileConsumer)` for transactional adding

All public API classes follow a fluent builder pattern: `ZipIt.zip(path).settings(...).add(files)`.

### Settings model (`model/settings/`)

- **`ZipSettings`** – archive-scope settings: split size, comment, zip64 flag, entry settings provider
- **`ZipEntrySettings`** – per-entry settings: compression, level, encryption, password, zip64, UTF-8
- **`ZipEntrySettingsProvider`** – maps file names to `ZipEntrySettings`
- **`UnzipSettings`** – unzip settings: password provider (can be per-file function)

### Internal model (`model/`)

- **`ZipModel`** – central in-memory representation of a zip file (entries, split info, comment)
- **`CentralDirectory`** – central directory with `FileHeader` records
- **`LocalFileHeader`** – local file header (per entry, before data)
- **`ZipEntry`** (`model/entry/`) – internal entry model used during write operations
- **`SrcZip`** (`model/src/`) – abstraction over zip source: `SolidSrcZip`, `PkwareSplitSrcZip`, `SevenZipSplitSrcZip`
- **`SplitTrigger`** (`model/split/`) – controls when to split to next volume
- Extra field records in `model/extrafield/records/` (NTFS timestamps, AES, Unicode path/comment, etc.)

### I/O layer (`io/`)

- **`io/in/`** – `DataInput` abstraction with `BaseDataInput`, `MarkerDataInput`; sub-packages for compressed, encrypted, and file-backed streams
- **`io/out/`** – `DataOutput` abstraction; `OffsOutputStream` tracks current write position; sub-packages for compressed, encrypted, and file-backed outputs
- **`io/readers/`** – readers for all zip structures: `ZipModelReader`, `CentralDirectoryReader`, `LocalFileHeaderReader`, `FileHeaderReader`, etc.
- **`io/writers/`** – writers for all zip structures: `ZipModelWriter`, `CentralDirectoryWriter`, `LocalFileHeaderWriter`, etc.

### Engine layer (`engine/`)

- **`engine/zip/ZipEngine`** – orchestrates adding entries to a zip
- **`engine/zip/ZipSymlinkEngine`** – handles symlink entries
- **`engine/zip/RecursiveEngine`** – recursive zip extraction
- **`engine/np/`** – `NamedPath` abstraction: `Directory`, `RegularFile`, `Symlink` wrappers used during zip creation
- **`engine/unzip/UnzipEngine`** – reads zip model and dispatches extraction
- **`engine/unzip/UnzipExtractEngine`** – single-threaded extraction
- **`engine/unzip/UnzipExtractAsyncEngine`** – multi-threaded extraction
- **`engine/info/InfoEngine`** – base for diagnostic engines; `ViewInfoEngine` renders text, `DecomposeInfoEngine` writes binary/text files

### Crypto (`crypto/`)

- `Encoder`/`Decoder` interfaces for encryption/decryption
- `NullEncoder`/`NullDecoder` for unencrypted entries
- `aes/` – AES encryption (WinZip AES)
- `pkware/` – traditional PKWare (ZIP) encryption
- `strong/` – strong encryption support

### View (`view/`) and Decompose (`decompose/`)

- `view/` – text-based rendering of zip structures (used by `ZipInfo.printShortInfo()`)
- `decompose/` – writes raw binary and text representations of each zip structure to files (used by `ZipInfo.decompose()`)

## Code Conventions

- **Lombok** is used extensively: `@Getter`, `@Setter`, `@RequiredArgsConstructor`, `@Builder`, `@Slf4j`. Experimental Lombok features are forbidden (`lombok.experimental.flagUsage=ERROR`).
- **Apache Commons Collections4** and **Commons Lang3** are used for utilities.
- **Checkstyle** enforces Google Java Style (configured in `misc/checkstyle/checkstyle.xml`).
- **PMD** rules are in `misc/pmd/`.
- All Java source files must have the Apache 2.0 license header (enforced by the `license` Gradle task).
- Field names in tests: `dirSrc`, `dirRoot`, `password`, etc. are constants in `TestData.java` and `Zip4jvmSuite`.
- Test helper `Zip4jvmSuite` sets up shared test data in `@BeforeSuite`. Tests should use `subDirNameAsMethodName()` utilities to isolate output directories.

## Key Dependencies

- `org.apache.commons:commons-compress` – underlying compression codec support
- `com.github.luben:zstd-jni` – Zstandard compression
- `org.tukaani:xz` – LZMA compression
- `org.testng:testng` – test framework
- `org.assertj:assertj-core` – fluent assertions
- `org.mockito:mockito-core` – mocking
- Test-only: `net.sf.sevenzipjbinding`, `net.lingala.zip4j`, `de.idyl:winzipaes` – cross-compatibility tests
