# File Tree
File Tree is a multiplatform Kotlin based software for files and directories viewing.  
![](gifs/huge_files_and_directories.gif)
Main features:
- FTP servers browsing support (File -> Open FTP address)
  ![](gifs/ftp_server_browsing.gif)
- Text files, images and ZIP/JAR archives content preview
  ![](gifs/text_zip_jpeg_files_preview.gif)
- File extension filtering  
  ![](gifs/show_hidden_and_extension_filters.gif)
- Show hidden files
- Open files
  ![](gifs/open_directories_and_files.gif)

## Building File Tree
Building File Tree requires JDK and Kotlin.  
To build executable jar file navigate to the project root directory and run command:  

```gradle clean build```

## Running File Tree
To run File Tree build it from source code and run following command in the terminal:  

```java -jar build/libs/FileTree-1.0.jar```

To run tests:  
```gradle test```