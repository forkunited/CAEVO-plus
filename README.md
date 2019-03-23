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
You can run all the experiments from the paper using the *test.sh* script.  Look at the contents
of the script for more documentation on the specific commands and scripts that are run.  The results
will be output to *src/main/resources/output/event_string/ExperimentEvaluationOutput*.

Code structure
--------------
Note that all the commands run in *test.sh* to produce the results from the paper execute *ctx*
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

Running on raw text
-------------------
You can run the system on raw text using the *runraw.sh* script after following the instructions
below.  Note that by default, this script will run the F+ version of the model given in the paper,
but you can switch to F+L or any other version by editing it to reference a different *ctx* script.
Also, we apologize that this process is a bit messy, and was quickly hacked together after
requests from multiple people.  Please raise an issue if you run into bugs or problems.

Here are the steps for running on a raw text file:

1. Setup the CAEVO-plus system by following the instructions given above.

2. Setup the original CAEVO system from [here](https://github.com/nchambers/caevo).

3. Download the *mate-tools*  CoNLL2009 English SRL, parsing, POS-tagging, and lemmatizing models from
[here](https://code.google.com/archive/p/mate-tools/downloads?page=2).

4. Point the path variables in *event.properties* to the locations where you downloaded the 
*mate-tools* models.

5. Edit the *runraw.sh* script, and fill in the location of the directory containing the original
[CAEVO](https://github.com/nchambers/caevo) in the *CAEVO_DIR* variable.  

6. Run *runraw.sh <text-file>* on some text file from your system. Output will be stored in
*src/main/resources/data/raw_event_bson/* with links output by CAEVO-plus stored in 
the *plus_tlinks* sub-directory.  Other sub-directories will contain pre-processing output
by the original CAEVO system.

Note that this process will not take into account document-creation times for the input text
documents.  If you want to include these, they will need to be added to the XML output from
the original CAEVO system that the *runraw.sh* uses as input to CAEVO-plus.  Also, note that the
system is memory intensive and slow to initialize due to loading in word-embeddings and
re-training prior to running.  If you want to produce decent temporal annotations using fewer
resources, then we recommend resorting to the original CAEVO system (sorry, this inconvenience
is partially due to hacking this raw-text option together quickly). Also, you may receive
"illegal thread state exceptions" after the system runs due to some threading issue with Maven,
but you can safely ignore these.

