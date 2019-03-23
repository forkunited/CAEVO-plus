#!/bin/bash
#
# runraw.sh <text-file>
#

export MAVEN_OPTS=-Xmx8g

CAEVO_PLUS_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
CAEVO_DIR=[PATH TO DIRECTORY FOR THE ORIGINAL CAEVO SYSTEM] # ../caevo
STORAGE=RawEventBson
EMPTY_TLINKS=tlinks_placeholder.in

# CAEVO plus model path.  Note that this presently runs the F+ model from the paper.
# You can change to the F+L model by changing the TRAINING_CTX_PATH to
# src/main/resources/contexts/paper/Run_PSM_Final_Full_Evaluation_Ind_Fixed.ctx
TRAINING_CTX_PATH=src/main/resources/contexts/paper/Run_PSM_Final_Full_Evaluation_Fixed.ctx
MODEL_SUBCTX=modelsTlinkTypePS
MODEL_NAME=ps

# Run original caevo on input
cd ${CAEVO_DIR}
${CAEVO_DIR}/runcaevoraw.sh $@
cd ${CAEVO_PLUS_DIR}

# Replace root element with broken URL in
# original CAEVO output
sed -i '' -e 's/^<root.*/<root>/' ${CAEVO_DIR}/sieve-output.xml

# Remove tlinks from original CAEVO output
#sed -i '' -e 's/<tlink.*//' ${CAEVO_DIR}/sieve-output.xml

# Hack: create empty tlinks file to make parsing of 
# original-CAEVO output run smoothly
touch ${EMPTY_TLINKS}

# Parse output from original CAEVO into format used by the
# new system, and store in STORAGE_DIR
mvn exec:java -Dexec.mainClass="edu.psu.ist.acs.micro.event.scratch.ConstructTimeBankDense" -Dexec.args="${CAEVO_DIR}/sieve-output.xml ${EMPTY_TLINKS} ${STORAGE}"

# Remove placeholder file
rm ${EMPTY_TLINKS}

# Run CAEVO-plus on CAEVO output
mvn exec:java -Dexec.mainClass="edu.psu.ist.acs.micro.event.scratch.TrainAndPredictTLinks" -Dexec.args="${TRAINING_CTX_PATH} ${MODEL_SUBCTX} ${MODEL_NAME}"

