package org.apache.jena.reasoner.rulesys.impl;

public class RETEPrinter {

	private static final int wsPerLevel = 4;

	private int nrWs = 0;
	private StringBuffer buf = new StringBuffer();

	public String print(RETESourceNode node) {
		if (node.getContinuation() instanceof RETEQueue)
			print((RETEQueue) node.getContinuation());
		else {
			append(node.getId() + "\n--> <T>");
		}

		return buf.toString();
	}

	public void print(RETEQueue node) {
		append("(" + node.getId() + ")");

		RETEQueue queue = node.sibling;
		append(" <> (" + queue.getId() + ")");

		if (queue.getContinuation() != null) {
			newLevel();

			if (queue.continuation instanceof RETEQueue) {
				print((RETEQueue) queue.continuation);
			} else {
				append(" --> <T>");
			}
		}
	}

	private void newLevel() {
		buf.append("\n");
		
		this.nrWs += wsPerLevel;
		for (int i = 0; i < nrWs; i++)
			buf.append(" ");
	}

	private void append(String str) {
		buf.append(str);
	}
}
