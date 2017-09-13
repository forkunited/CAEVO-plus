CONTEXT_DIR=src/main/resources/contexts

mvn exec:java -Dexec.mainClass="edu.psu.ist.acs.micro.event.scratch.RunEventContext" -Dexec.args="${CONTEXT_DIR}/tlinkType/experiment/final/Test_CAEVO_Final.ctx"
