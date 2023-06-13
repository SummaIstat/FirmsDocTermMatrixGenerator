********************************************************
*****     FirmsDocTermMatrixGenerator readme       *****
********************************************************

======================================================================
What is FirmsDocTermMatrixGenerator and what is it intended to be used for
======================================================================

FirmsDocTermMatrixGenerator is a Java application that reads all the documents (related to scraped enterprises' websites) 
contained in a specified Solr collection, extracts all the word from them and generates a matrix having:
one word for each column
one enterprise for each row
the number of occurrencies of each word in each firm set of scraped webpages in the cells

The words are obtained in this way:
All the words present in Solr are retrieved
All the words having less than 3 or more than 25 characters are discarded
All the words not recognized as "first language" words or "second language" words are discarded  
The "first language" words are lemmatized with TreeTagger and stemmed with SnowballStemmer
The "second language" words are lemmatized with TreeTagger and stemmed with SnowballStemmer
The words contained in a "go word list" are added to the word list
The words contained in a "stop word list" are removed from the word list

I developed and used this program in order to produce the Term-Document matrix to be used by learners in the analysis phase.

======================================================================
How is the project folder made
======================================================================

The SolrTSVImporter folder is the Eclipse project ready to be run or modified in the IDE (you just have to import
the project "as an existing project" and optionally change some configuration parameters in the code).

Ignoring the hidden directories and the hidden files, under the main directory you can find 5 subdirectories :
1) src => contains the source code of the program
2) bin => contains the compiled version of the source files
3) lib => contains the jar files (libraries) that the program needs
4) sandbox => contains the executable jar file that you have to use in order to launch the program and some test input files that you can modify on the basis of your needs

As you probably already know it is definitely not a good practice to put all this stuff into a downloadable project folder, but i decided to break the rules in order to facilitate your job. Having all the stuff that will be necessary in just one location and by following the instructions you should be able to test the whole environment in 5-10 minutes.

======================================================================
How to execute the program on your PC by using the executable jar file
======================================================================

***** VERY IMPORTANT NOTE *****
*
*   I am assuming that :
*
*   ==> the Solr index you are going to use was created by using the specific schema.xml 
*   configuration file that I provided in the SolrTSVImporter project 
*
*   ==> the Solr version that you are using is the one that I suggested and provided in the 
*   SolrTSVImporter project (or a compatible one)
*
*   If it is not the case you could experience technical problems !
* 
******************************* 

If you have Java already installed on your PC you just have to apply the following instruction points:

1) create a folder on your filesystem (let's say "myDir")

2) copy the file FirmsDocTermMatrixGenerator.jar from sandbox directory to "myDir"

3) copy the file fdtmgConf.properties from sandbox directory to "myDir"

4) copy the file goWordsList.txt from sandbox directory to "myDir"

5) copy the file stopWordsList.txt from sandbox directory to "myDir"

6) install and properly configure TreeTagger (please refer to the TreeTagger website http://www.cis.uni-muenchen.de/~schmid/tools/TreeTagger/)

7) customize the parameters inside the fdtmgConf.properties file :
    	
    	Change the value of the parameters under the "paths section" according with the position of the files and folders on your filesystem.

    	Change the value of the parameters under the "technical parameters" according with your needs

8) open a terminal, go into the myDir directory, type and execute the following command:
        java -jar FirmsDocTermMatrixGenerator.jar fdtmgConf.properties

9) at the end of the program execution you should find inside the directory myDir the output matrix file and a log file


======================================================================
LINUX			
======================================================================

If you are using a Linux based operating system open a terminal and type on a single line :

java -jar 
-Xmx_AmountOfRamMemoryInMB_m
/path_of_the_directory_containing_the_jar/FirmsDocTermMatrixGenerator.jar 
/path_of_the_directory_containing_the_conf_file/fdtmgConf.properties


eg:

java -jar -Xmx2048m FirmsDocTermMatrixGenerator.jar fdtmgConf.properties

java -jar -Xmx1024m /home/summa/workspace/FirmsDocTermMatrixGenerator/sandbox/FirmsDocTermMatrixGenerator.jar /home/summa/workspace/FirmsDocTermMatrixGenerator/sandbox/fdtmgConf.properties


======================================================================
WINDOWS			
======================================================================

If you are using a Windows based operating system you just have to do exactly the same, the only difference is that you have to substitute the slashes "/" with the backslashes "\" in the filepaths.

eg:

java -jar -Xmx1536m C:\workspace\FirmsDocTermMatrixGenerator\sandbox\FirmsDocTermMatrixGenerator.jar C:\workspace\FirmsDocTermMatrixGenerator\sandbox\fdtmgConf.properties

======================================================================
LICENSING
======================================================================

This software is released under the European Union Public License v. 1.2
A copy of the license is included in the project folder.

======================================================================
Considerations
======================================================================

This program is still a work in progress so be patient if it is not completely fault tolerant; in any case feel free to contact me (donato.summa@istat.it) if you have any questions or comments.
