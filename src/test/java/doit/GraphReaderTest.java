package doit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.junit.Test;

/**
 * Making sure that GraphReader does a decent job with some common variations.
 * @author jfoley
 *
 */
public class GraphReaderTest {

	@Test
	public void testSimple() {
		String test = "TARGET <- SRC1 SRC2 : COMMAND";
		GraphReader gr = new GraphReader(test);
		Rule r = gr.readNextRule();
		assertEquals(r.target, "TARGET");
		assertEquals(r.command, "COMMAND");
		assertEquals(r.sources, Arrays.asList("SRC1", "SRC2"));
		assertNull(gr.readNextRule());
	}
	
	@Test
	public void testSimpleLB() {
		String test = "TARGET <- SRC1 SRC2 \n  : COMMAND";
		GraphReader gr = new GraphReader(test);
		Rule r = gr.readNextRule();
		assertEquals(r.target, "TARGET");
		assertEquals(r.command, "COMMAND");
		assertEquals(r.sources, Arrays.asList("SRC1", "SRC2"));
		assertNull(gr.readNextRule());
	}
	
	@Test
	public void testComments() {
		String test = "#this is a comment\n"
				+ "TARGET <- SRC1 SRC2 :"
				+ "# comments can be anywhere \n"
				+ " COMMAND";
		GraphReader gr = new GraphReader(test);
		Rule r = gr.readNextRule();
		assertEquals(r.target, "TARGET");
		assertEquals(r.command, "COMMAND");
		assertEquals(r.sources, Arrays.asList("SRC1", "SRC2"));
		assertNull(gr.readNextRule());
	}
	
	@Test
	public void testLongCommand() {
		String test = "TARGET <- SRC1 SRC2 : ./a b c d e f g";
		GraphReader gr = new GraphReader(test);
		Rule r = gr.readNextRule();
		assertEquals(r.target, "TARGET");
		assertEquals(r.command, "./a b c d e f g");
		assertEquals(r.sources, Arrays.asList("SRC1", "SRC2"));
		assertNull(gr.readNextRule());
	}
	
	@Test
	public void testSingleQuotes() {
		String test = "'TARGET' <- 'SRC1' 'SRC2' : COMMAND";
		GraphReader gr = new GraphReader(test);
		Rule r = gr.readNextRule();
		assertEquals(r.target, "TARGET");
		assertEquals(r.command, "COMMAND");
		assertEquals(r.sources, Arrays.asList("SRC1", "SRC2"));
		assertNull(gr.readNextRule());
	}
	
	@Test
	public void testDoubleQuotes() {
		String test = "\"TARGET\" <- \"SRC1\" \"SRC2\" : COMMAND";
		GraphReader gr = new GraphReader(test);
		Rule r = gr.readNextRule();
		assertEquals(r.target, "TARGET");
		assertEquals(r.command, "COMMAND");
		assertEquals(r.sources, Arrays.asList("SRC1", "SRC2"));
		assertNull(gr.readNextRule());
	}
}
