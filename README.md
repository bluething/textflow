# Textflow

A Java application for indexing text files with configurable rules and support for multiple file formats.

### Features

* Multi-format Support: Handles TXT, HTML, and JSON files with extensible architecture  
* Concurrent Processing: Efficiently processes multiple files in parallel  
* Large File Support: Optimized for handling files up to hundreds of MB  
* Extensible Rules: Easy to add new indexing rules

### Current Indexing Rules  

* Words starting with uppercase: Counts words that begin with a capital letter  
* Words longer than 5 characters: Lists all unique words with more than 5 characters

### Requirements  

* Java 21 or higher  
* Maven 3.9 or higher

### Building the Application

`mvn clean package`

### Usage

`java -jar --enable-preview textflow.jar <file1> <file2> ... <fileN>`

#### Examples

`java -jar --enable-preview textflow.jar fakefile_1MB.txt fakefile_1MB.html`

#### Sample Output

```text
================================================================================
FILE INDEXING RESULTS
================================================================================
File: fakefile_1MB.txt
Processing Time: 303 ms
File Size: 1.0 MB
Words starting with uppercase: 2
Words longer than 5 characters: [consectetur, adipiscing]

--------------------------------------------------------------------------------
File: fakefile_1MB.html
Processing Time: 332 ms
File Size: 1.0 MB
Words starting with uppercase: 2
Words longer than 5 characters: [consectetur, adipiscing]

--------------------------------------------------------------------------------

```
