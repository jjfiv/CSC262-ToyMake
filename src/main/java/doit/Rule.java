package doit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * This rule class corresponds to a "makefile"-style rule.
 * @author jfoley
 *
 */
public class Rule {
	/**
	 * How to run commands.
	 */
	public static final String SHELL = "/bin/bash";
	
	/**
	 * What file should be created from this rule.
	 */
	public String target;
	/**
	 * What files should exist for this rule to run.
	 */
	public List<String> sources;
	/**
	 * What command should be run to produce "target" from "sources".
	 */
	public String command;
	
	/**
	 * Has this command been run? If so? What was the return code?
	 */
	public Integer returnCode = null;
	
	/**
	 * A rule is constructed of a target, a list of inputs, and a command that combines them into the target.
	 * @param target - the file to create.
	 * @param sources - the files necessary.
	 * @param command - the "bash" command that creates target from sources.
	 */
	public Rule(String target, List<String> sources, String command) {
		this.target = target;
		this.sources = sources;
		this.command = command;
	}
	
	@Override
	public String toString() {
		return "Rule [target=" + target + ", sources=" + sources + ", command=" + command + "]";
	}
	
	/**
	 * This seems like a nice thing to check...
	 * @return
	 */
	private boolean sourcesExist() {
		// TODO: check to make sure all sources exist.
		return false;
	}
	
	/**
	 * Check to make sure there's actually something to do.
	 * @return does the target file exist?
	 */
	public boolean isNeeded() {
		File target = new File(this.target);
		if (target.exists()) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * How do we run a rule?
	 * 1. create a temporary file.
	 * 2. write the command to it
	 * 3. execute that file with /bin/bash
	 * 4. delete temporary file.
	 * 5. store and return exit code (0 is usually success!)
	 * @return the exit code of the command.
	 */
	public int execRule() {		
		// Don't run multiple times.
		if (this.returnCode != null) {
			return returnCode;
		}
		
		try {
			// 1. create temporary file.
			File tmp = File.createTempFile("rule", ".sh");
			tmp.deleteOnExit();

			// 2. write command to file.
			PrintWriter pw = new PrintWriter(new FileWriter(tmp));
			pw.println(this.command);
			pw.close();
			
			// 3. execute the file:
			ProcessBuilder pb = new ProcessBuilder();
			pb.command(SHELL, tmp.getAbsolutePath());
			pb.inheritIO();
			Process p = pb.start();
			
			// This part could take some time.
			this.returnCode = p.waitFor();
			
			// 4. delete temporary file
			tmp.delete();
			
			// 5. store & return the exit code
			return returnCode;
			
		} catch (IOException e) {
			throw new RuntimeException("IOError", e);
		} catch (InterruptedException e) {
			throw new RuntimeException("Cancelled", e);
		}
	}
}
