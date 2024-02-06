package net.talaatharb.explainer.service;

import com.couchbase.client.core.deps.com.google.common.collect.Lists;
import com.fasterxml.jackson.databind.JsonNode;
import com.mxgraph.view.mxGraph;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GraphBuilderServiceImpl implements GraphBuilderService {

	private static final String OPERATOR = "#operator";

	private static final int VERTICAL_STEP = 50;

	private static final double CHAR_TO_WIDTH_FACTOR = 6.5;

	@Override
	public mxGraph buildGraphFromJsonPlan(final JsonNode node) {
		final mxGraph graph = new mxGraph();
		final Object parent = graph.getDefaultParent();

		final int y = 20;
		final int x = 20;

		graph.getModel().beginUpdate();
		try {
			handleNode(graph, parent, node.get("plan"), x, y);
			// graph.insertEdge(parent, null, "Edge", v1, v2);
		} finally {
			graph.getModel().endUpdate();
		}
		return graph;
	}

	private int handleNode(final mxGraph graph, final Object parent, final JsonNode node, int x, int y) {
		final String operator = node.get(OPERATOR).asText();
		if ("Sequence".equals(operator)) {
			y = drawSequenceChildren(graph, parent, node, x, y);
		} else if ("Parallel".equals(operator)) {
			y = drawParallelChildren(graph, parent, node, x, y);
		} else if ("IntersectScan".equals(operator)) {
			drawIntersectScan(graph, parent, node, x, y);
			y += VERTICAL_STEP;
		} else if ("UnionScan".equals(operator)) {
			y = drawUnionScan(graph, parent, node, x, y);
		} else {
			drawSimpleNode(graph, parent, node, x, y);
			y += VERTICAL_STEP;
		}
		return y;
	}

	private int drawUnionScan(mxGraph graph, Object parent, JsonNode node, int x, int y) {
		final var scans = Lists.newArrayList(node.get("scans").elements());
		// draw union node
		// draw scans
		for (final var scan : scans) {
			y = handleNode(graph, parent, scan, x, y);
		}

		graph.insertVertex(parent, null, "UnionScan", x, y, 9 * CHAR_TO_WIDTH_FACTOR, 40);

		y += VERTICAL_STEP;
		return y;
	}

	private int drawIntersectScan(mxGraph graph, Object parent, JsonNode node, int x, int y) {
		final var scans = Lists.newArrayList(node.get("scans").elements());
		for (final var scan : scans) {
			x += drawSimpleNode(graph, parent, scan, x, y) + 10;
		}
		return VERTICAL_STEP;
	}

	private double drawSimpleNode(mxGraph graph, Object parent, JsonNode node, int x, int y) {
		String operator = node.get(OPERATOR).asText();
		final StringBuilder builder = new StringBuilder(operator);
		double width = operator.length() * CHAR_TO_WIDTH_FACTOR;

		if (operator.startsWith("DistinctScan")) {
			node = node.get("scan");
			builder.append(": ");
			operator = node.get(OPERATOR).asText();
			builder.append(operator);
			width += 2 + operator.length();
		}

		if (operator.startsWith("IndexScan")) {
			final String indexName = node.get("index").asText();

			builder.append("\n");
			builder.append(indexName);

			width = Math.max(width, indexName.length() * CHAR_TO_WIDTH_FACTOR);
		}

		if (operator.startsWith("KeyScan")) {
			final String keys = node.get("keys").asText();
			builder.append("\n");
			builder.append(keys);

			width = Math.max(width, keys.length() * CHAR_TO_WIDTH_FACTOR);
		}

		if (operator.startsWith("Filter")) {
			final String condition = node.get("condition").asText();
			builder.append("\n");
			builder.append(condition);

			width = Math.max(width, condition.length() * CHAR_TO_WIDTH_FACTOR);
		}

		graph.insertVertex(parent, null, builder.toString(), x, y, width, 40);

		return width;
	}

	private int drawParallelChildren(mxGraph graph, Object parent, JsonNode node, int x, int y) {
		return handleNode(graph, parent, node.get("~child"), x, y);
	}

	private int drawSequenceChildren(mxGraph graph, Object parent, JsonNode node, int x, int y) {
		final var children = Lists.newArrayList(node.get("~children").elements());

		for (final var child : children) {
			y = handleNode(graph, parent, child, x, y);
		}

		return y;
	}
}
