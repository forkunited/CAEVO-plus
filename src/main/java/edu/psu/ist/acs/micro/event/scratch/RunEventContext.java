package edu.psu.ist.acs.micro.event.scratch;

import java.io.File;

import edu.cmu.ml.rtw.generic.data.Context;
import edu.cmu.ml.rtw.generic.util.OutputWriter;
import edu.psu.ist.acs.micro.event.data.EventDataTools;
import edu.psu.ist.acs.micro.event.util.EventProperties;

public class RunEventContext {
	public static void main(String[] args) {
		Context.run(new EventDataTools(new OutputWriter(), new EventProperties(), 0), new File(args[0]));
	}
}
