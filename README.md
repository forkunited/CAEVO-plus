# CAEVO-plus

CAEVO (Cascading EVent Ordering System) is TempEval-style system for extracting 
temporal entities (events and time expressions), and labeling the temporal relations 
between the temporal entities.  This repository contains the code for a reproduction 
and improvement of the original CAEVO system [here](https://github.com/nchambers/caevo).  
The details of the improvements are described in the paper:

*Event Ordering with a Generalized Model for Sieve Prediction Ranking.* 
Bill McDowell, Nathanael Chambers, Alexander G. Ororbia II, and David Reitter
IJCNLP-2017, Taipei, Taiwan. 2017.

License and Dependencies
------------------------
This software is released under the Apache License, Version 2.0. See LICENSE in the 
project root directory for all details. The software was developed in the IST department
of Penn State University funded under NSF grant SES-1528409.

We make note that this software uses Stanford's CoreNLP library which is under a GPL v2.0 license. 
If you use the system as-is, the software is governed instead by the GPL v2. If you wish to use 
the system under the MIT License, you need to replace the CoreNLP dependency with a 
non-GPL library.

The system depends heavily on generic machine learning and NLP utilities from the 
CMU [RTW](http://rtw.ml.cmu.edu/rtw/) group's micro-reading library 
[micro-util](https://github.com/forkunited/micro-util/tree/standalone-caevo/).

Setup
-----

Below are the steps to setup the system.

1. Make sure sure Java 8 and Maven are installed on your system

2. Clone the repository 
```
    git clone https://github.com/forkunited/CAEVO-plus.git
```
3. Download the stand-alone micro-util jar from 
[here](https://drive.google.com/file/d/0B6nD4za_hvG1UXltNWJVSS1aRjQ/view?usp=sharing) 
and place it in the root directory of the CAEVO-plus repository that 
you've cloned. 

4. From the root directory of the CAEVO-plus repository run the setup script:
```
    ./setup.sh
```
5. Download the 300-dimensional pretrained Google News 
[word embeddings](https://code.google.com/archive/p/word2vec/) to an appropriate 
directory.

6. Download [WordNet](http://wordnetcode.princeton.edu/wn3.1.dict.tar.gz) to
an appropriate directory.  Untar the WordNet *dict* directory with:
```
    tar xvf wn3.1.dict.tar.gz
```
7. Copy the *src/main/resources/wordnet/jwnl_properties.xml* WordNet Java configuration file
template to somewhere appropriate (e.g. in the directory where you placed WordNet), and 
edit it to fill in the *dictionary_path* parameter value with the location of the WordNet
*dict* directory that you untared. 

8. After you run *setup.sh*, there should be an *event.properties* file in the root directory
of the CAEVO-plus repository.  Edit this file to fill in the locations of the downloaded
Word2Vec vectors file and the *jwnl_properties.xml* file that you've edited from steps 5-7.

9. Build the project by compiling from the root directory of the CAEVO-plus repository:
```
    mvn clean compile
```
Experiments
-----------
You can run all the experiments from the paper using the *run.sh* script.  Look at the contents
of the script for more documentation on the specific commands and scripts that are run.  The results
will be output to *src/main/resources/output/event_string/ExperimentEvaluationOutput*.

Code structure
--------------
Note that all the commands run in *run.sh* to produce the results from the paper execute *ctx*
scripts in *src/main/resources/contexts/*.  These scripts are implemented in a domain-specific 
language for specifying machine learning models and features.  The language is implemented in the
[micro-util](https://github.com/forkunited/micro-util/tree/standalone-caevo/) library, which also
contains the underlying Java code that trains the models and computes the features.  If you want 
to understand the details of how the system works, you can get a high-level overview by reading 
through their configuration in the *ctx* scripts.  On the other hand, understand the low-level
implemenations of features and models will require looking through the 
[micro-util](https://github.com/forkunited/micro-util/tree/standalone-caevo/) code that is called
through the *ctx* scripts.

The system is a bit over-engineered and complicated due to ambitions for its use within the context
of a more generic never-ending learning system.  If you're trying to understand the details,
and it seems confusing, then please contact Bill McDowell (forkunited@gmail.com) and he will try
to clarify things.  


