package doit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Read a file full of rules of the format:
 * 
 * TARGET <- SOURCE SOURCE SOURCE : COMMAND
 * 
 * TARGET and SOURCE may be quoted.
 * 
 * @author jfoley
 *
 */
public class GraphReader {
	private char[] data;
	private int position = 0;

	/**
	 * Read from a string.
	 * @param data - a string containing doit rules.
	 */
	public GraphReader(String data) {
		this.data = data.toCharArray();
	}

	/**
	 * Read from a file.
	 * @param file - the file to open and read doit rules from.
	 */
	public GraphReader(File file) {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			while (true) {
				String line = reader.readLine();
				if (line == null)
					break;
				sb.append(line).append('\n');
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException("No such file: " + file, e);
		} catch (IOException e) {
			throw new RuntimeException("IO Error", e);
		}
		this.data = sb.toString().toCharArray();
	}
	
	/**
	 * Recursive descent parser entry:
	 * @return read a rule (skip whitespace, etc.)
	 */
	public Rule readNextRule() {
		skipWhitespace();
		if (this.peek() == -1) {
			return null;
		}

		String target = readIdentifier();
		if (!readToken("<-")) {
			error("Expected <- after target name.");
		}

		List<String> sources = new ArrayList<>();
		while (!readToken(":")) {
			sources.add(readIdentifier());
		}

		this.skipWhitespace();
		String command = this.consumeRestOfLine();

		return new Rule(target, sources, command);
	}

	/**
	 * Read something (skipping whitespace) next if possible.
	 * @param exact - the thing that probably comes next.
	 * @return true if we found it, false if not.
	 */
	private boolean readToken(String exact) {
		skipWhitespace();
		if (rest().startsWith(exact)) {
			consume(exact.length());
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Read a quoted or regular word.
	 * @return the data read.
	 */
	private String readIdentifier() {
		skipWhitespace();

		switch (peek()) {
		case '"':
		case '\'':
			return consumeQuoted();
		default:
			return consumeUntilBreak();
		}
	}

	/**
	 * What character is next? No change to position.
	 * @return the value or -1 if EOF.
	 */
	private int peek() {
		if (position < data.length) {
			return data[position];
		}
		return -1;
	}

	/**
	 * Read the next character, updating position.
	 * @return the value or -1 if EOF.
	 */
	private int getc() {
		int x = peek();
		position++;
		return x;
	}

	/**
	 * How many characters are left to process?
	 * @return the length of the rest.
	 */
	private int remaining() {
		return data.length - position;
	}

	/**
	 * rest="abcd"
	// then .getc()=> "a"
	// then rest="bcd"
	 * @return the rest of the data as a string, for errors
	 */
	private String rest() {
		if (position >= data.length) {
			return "";
		}
		return new String(data, position, this.remaining());
	}

	/**
	 * Grab the next "amt" characters.
	 * "abcd".consume(2) => "ab", rest="cd"
	 * @param amt - how many
	 * @return the value.
	 */
	private String consume(int amt) {
		String out = new String(data, position, amt);
		position += amt;
		return out;
	}

	/**
	 * Consume until a newline or the EOF.
	 * @return what did we find?
	 */
	private String consumeRestOfLine() {
		StringBuilder sb = new StringBuilder();
		for (; position < data.length; position++) {
			if (data[position] == '\n') {
				position++;
				return sb.toString();
			}
			sb.append(data[position]);
		}
		return sb.toString();
	}

	/**
	 * Consume characters until they're not whitespace anymore. Include # comments.
	 */
	private void skipWhitespace() {
		for (; position < data.length; position++) {
			if (data[position] == '#') {
				consumeRestOfLine();
			}
			if (position == data.length || !Character.isWhitespace(data[position])) {
				break;
			}
		}
	}

	/**
	 * Should we stop now?
	 * @param ch - the charcter to look at.
	 * @return true if it might be the end of a word.
	 */
	private boolean isBreak(char ch) {
		if (Character.isWhitespace(ch)) {
			return true;
		}
		switch (ch) {
		case '-':
		case '#':
		case ':':
			return true;
		}
		return false;
	}

	// "abcds " -> "abcds"
	private String consumeUntilBreak() {
		StringBuilder sb = new StringBuilder();
		for (; position < data.length; position++) {
			if (isBreak(data[position])) {
				break;
			}
			sb.append(data[position]);
		}
		return sb.toString();
	}

	// "hello", 'hello', "hello\n1\n2\n3"
	private String consumeQuoted() {
		char quote = (char) getc();
		StringBuilder sb = new StringBuilder();
		for (; position < data.length; position++) {
			if (data[position] == '\\') {
				getc();

				// what is it escaping?
				int escaped = peek();
				switch (escaped) {
				case -1:
					error("unexpected end of line in escape");
				case '"':
				case '\'':
					sb.append((char) getc());
					break;
				case 'n':
					sb.append('\n');
					getc();
					break;
				case 't':
					sb.append('\t');
					getc();
					break;
				default:
					error("unhandled escape character: '" + (char) escaped + "'");
				}
			}
			if (data[position] == quote) {
				position++;
				break;
			}
			sb.append(data[position]);
		}
		return sb.toString();
	}

	private void error(String msg) {
		throw new RuntimeException(msg + ": " + this.toString());
	}
}
