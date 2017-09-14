#!/bin/bash
#
# The commands below run the models to produce the results from
# the McDowell et al (2017) "Event Ordering with a Generalized Model 
# for Sieve Prediction Ranking" paper.  Each command runs a single
# model from the paper, and outputs results in the
# src/main/resources/output/event_string/ExperimentEvaluationOutput
# directory.
#
# The name of the model from the paper run by each command is given
# in the comment in the line immediattely above the command.  
# See the ctx scripts in CONTEXT_DIR run by the commands for the 
# configuration details of each model.  Note that "ctx" is a 
# domain specific model configuration language from the RTW micro-util
# dependency 
# (see https://github.com/forkunited/micro-util/tree/standalone-caevo/)
#
#

CONTEXT_DIR=src/main/resources/contexts/paper/

# Model name: R
mvn exec:java -Dexec.mainClass="edu.psu.ist.acs.micro.event.scratch.RunEventContext" -Dexec.args="${CONTEXT_DIR}/Test_CAEVO_Final.ctx"

# Model name: Ev
mvn exec:java -Dexec.mainClass="edu.psu.ist.acs.micro.event.scratch.RunEventContext" -Dexec.args="${CONTEXT_DIR}/Test_CAEVO_Final_EventAttr.ctx"

# Model name: SRL
mvn exec:java -Dexec.mainClass="edu.psu.ist.acs.micro.event.scratch.RunEventContext" -Dexec.args="${CONTEXT_DIR}/Test_CAEVO_Final_SRL.ctx"

# Model name: W2V
mvn exec:java -Dexec.mainClass="edu.psu.ist.acs.micro.event.scratch.RunEventContext" -Dexec.args="${CONTEXT_DIR}/Test_CAEVO_Final_W2VAll.ctx"

# Model name: R+
mvn exec:java -Dexec.mainClass="edu.psu.ist.acs.micro.event.scratch.RunEventContext" -Dexec.args="${CONTEXT_DIR}/Test_PSM_Final_Full_Evaluation.ctx"

# Model name: F+
mvn exec:java -Dexec.mainClass="edu.psu.ist.acs.micro.event.scratch.RunEventContext" -Dexec.args="${CONTEXT_DIR}/Test_PSM_Final_Full_Evaluation_Fixed.ctx"

# Model name: F+L
mvn exec:java -Dexec.mainClass="edu.psu.ist.acs.micro.event.scratch.RunEventContext" -Dexec.args="${CONTEXT_DIR}/Test_PSM_Final_Full_Evaluation_Ind_Fixed.ctx"

# Model name: F+S
mvn exec:java -Dexec.mainClass="edu.psu.ist.acs.micro.event.scratch.RunEventContext" -Dexec.args="${CONTEXT_DIR}/Test_PSM_Final_Full_EvaluationScore_Fixed.ctx"

# Model name: F+LU
mvn exec:java -Dexec.mainClass="edu.psu.ist.acs.micro.event.scratch.RunEventContext" -Dexec.args="${CONTEXT_DIR}/Test_CAEVO_Final_Full_Ind_Unlabeled_Fixed.ctx"

# Model name: F*
mvn exec:java -Dexec.mainClass="edu.psu.ist.acs.micro.event.scratch.RunEventContext" -Dexec.args="${CONTEXT_DIR}/Test_PSM_Final_Full_Perfect_Fixed.ctx"

