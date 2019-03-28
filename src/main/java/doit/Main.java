package doit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {
	public static void main(String[] args) {
		// Use a default filename...
		String fileName = "doit";
		if (args.length >= 1) {
			fileName = args[0];
		}

		// Reading a graph file into a list of rules:
		GraphReader reader = new GraphReader(new File(fileName));
		List<Rule> rules = new ArrayList<Rule>();
		while (true) {
			Rule r = reader.readNextRule();
			if (r == null)
				break;
			rules.add(r);
		}

		// Print out the list of rules.
		System.out.println(rules);

		// Run the command for every rule (too simplified).
		for (Rule r : rules) {
			if (r.isNeeded()) {
				System.out.println(r.command);
				r.execRule();
			}
		}
	}
}
