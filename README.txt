THE PROJECT

The purpose for this java application is to monitor a folder and calculate the tdidf value for each file within it considering a initial list of terms.
The output will be traced in the terminal, reporting the N top files with the max tfidf, where N is an input parameter.

The architecture is divided into three main blocks:
- The File Monitor (FileMonitor) is the class in charge of monitoring the folder and retrieve the list of files when required.
- The TFIDF Calculator (TfIdf) is the class that keeps track of the terms and files registered and keeps the tfidf values of each one updated.
- The Application (TfIdfApp) is the main class that processes the input parameters, creates and configures the elements and manages the execution as an infinite loop.

This project includes my personal implementations for the FileMonitor and the TfIdf Calculator, but the TfIdfApp uses interfaces so it should be easy to switch them.


INSTALLATION AND DEPENDENCIES

This is a maven project created with eclipse, and can be imported in any other eclipse environment or built in a machine properly configured with Java and Maven.
Internet access is required to build it (or access to a maven repository containing all the dependencies).
The main dependencies are:
- Java 16
- jUnit 4.11
- Maven 3.8.1
- Apache Commons CLI 1.4
- Apache Log4J 1.2.17

The project is configured to generate two jars:
- One containing just the project code that will require all the dependencies available in the classpath (tfidf-1.0.0.jar)
- One uberJar containint the project and all the dependencies, that can be ported and executed as an standalone executable-jar (tfidf.jar).


USAGE

Considering the paths properly configured and the application built, it can be executed in a terminal as:
    java -jar tfidf.jar -d <FOLDER> -n <RESULTS> -p <PERIOD> -t <KEYWORDS>

where:
	<FOLDER> is the absolute path of the folder to be monitored
	<RESULTS> is the number of results that will be reported
	<PERIOD> is the number of seconds between reports
	<KEYWORDS> is the list of terms to be analyzed

For instance:
    java -jar tfidf.jar -d C:\files -n 3 -p 60 -t "password try again"    

It works as an endless loop, monitoring the folder defined as an input and logging in the terminal the updated results with the defined period.
The amount of results shown are also defined as an input parameter.

An example of output:

java -jar tfidf.jar -d c:\Prueba -n 3 -p 10 -t "password try again"
    2021-08-20 09:46:38,765 [INFO ] reportResults - Reporting 3 filtered results from 4 files:
    2021-08-20 09:46:38,767 [INFO ] reportResults -   c:\Prueba\fichero4.txt 0.1665
    2021-08-20 09:46:38,768 [INFO ] reportResults -   c:\Prueba\fichero3.txt 0.1665
    2021-08-20 09:46:38,769 [INFO ] reportResults -   c:\Prueba\fichero1.txt 0.1249
    2021-08-20 09:46:48,777 [INFO ] reportResults - Reporting 3 filtered results from 4 files:
    2021-08-20 09:46:48,779 [INFO ] reportResults -   c:\Prueba\fichero4.txt 0.1665
    2021-08-20 09:46:48,783 [INFO ] reportResults -   c:\Prueba\fichero3.txt 0.1665
    2021-08-20 09:46:48,784 [INFO ] reportResults -   c:\Prueba\fichero1.txt 0.1249
    2021-08-20 09:46:58,799 [INFO ] reportResults - Reporting 3 filtered results from 4 files:
    2021-08-20 09:46:58,800 [INFO ] reportResults -   c:\Prueba\fichero4.txt 0.1665
    2021-08-20 09:46:58,804 [INFO ] reportResults -   c:\Prueba\fichero3.txt 0.1665
    2021-08-20 09:46:58,805 [INFO ] reportResults -   c:\Prueba\fichero1.txt 0.1249
