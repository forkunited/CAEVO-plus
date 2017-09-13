package edu.psu.ist.acs.micro.event.scratch;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
/*
import org.joda.time.LocalDate;
import org.joda.time.Partial;
import org.joda.time.YearMonth;
*/
/*
import BIDMat.FMat;
import BIDMat.Mat;
import YADLL.Estimators.BP;
import YADLL.Estimators.Estimator;
import YADLL.FunctionGraphs.FunctionGraph;
import YADLL.FunctionGraphs.Functions.Function;
import YADLL.FunctionGraphs.Functions.Variable;
import YADLL.Optimizers.GradOpt;
*/

public class Scratch {
	public static void main(String[] args) {
		/*YearMonth ym = new YearMonth(1999, 12);
		LocalDate ld = new LocalDate(1999, 12, 10);
		System.out.println(ym.toString());
		Partial p = new Partial(ym);
		System.out.println(p.toString());
		System.out.println(ld.toString());
		System.out.println(YearMonth.parse(ld.toString()).toString());
		System.out.println(LocalDate.parse(ym.toString()).toString());*/
	
		DateTimeFormatter dateParser = DateTimeFormat.forPattern("MMMM dd, yyyy E");
		DateTimeFormatter dateOutputFormat = DateTimeFormat.forPattern("yyyy-MM-dd");
		DateTime date = dateParser.parseDateTime("january 27, 2016 wednesday");
		System.out.println(date.toString(dateOutputFormat));
	}
}
