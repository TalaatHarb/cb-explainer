package net.talaatharb.explainer.service;

import com.couchbase.client.core.deps.com.google.common.collect.Lists;
import com.fasterxml.jackson.databind.JsonNode;
import com.mxgraph.view.mxGraph;

import lombok.RequiredArgsConstructor;
import net.talaatharb.explainer.dtos.GraphBlockDto;

@RequiredArgsConstructor
public class GraphBuilderServiceImpl implements GraphBuilderService {

	private static final String DISTINCT_SCAN = "DistinctScan";

	private static final String INDEX_SCAN = "IndexScan";

	private static final String KEY_SCAN = "KeyScan";

	private static final String UNION_SCAN = "UnionScan";

	private static final String INTERSECT_SCAN = "IntersectScan";

	private static final String PARALLEL = "Parallel";

	private static final String SEQUENCE = "Sequence";

	private static final int STEP = 10;

	private static final int DEFAULT_BLOCK_HEIGHT = 40;

	private static final String OPERATOR = "#operator";

	private static final int VERTICAL_STEP_PLUS_HEIGHT = DEFAULT_BLOCK_HEIGHT + STEP;

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

	private GraphBlockDto handleNode(final mxGraph graph, final Object parent, final JsonNode node, int x, int y) {
		final String operator = node.get(OPERATOR).asText();
		GraphBlockDto result;

		switch (operator) {
		case SEQUENCE:
			result = drawSequenceChildren(graph, parent, node, x, y);
			break;
		case PARALLEL:
			result = drawParallelChildren(graph, parent, node, x, y);
			break;
		case INTERSECT_SCAN:
			result = drawIntersectScan(graph, parent, node, x, y);
			break;
		case UNION_SCAN:
			result = drawUnionScan(graph, parent, node, x, y);
			break;
		default:
			result = drawSimpleNode(graph, parent, node, x, y);
			break;
		}

		return new GraphBlockDto(result.getStartNode(), result.getEndNode(), result.getStartX(), result.getStartY(),
				result.getEndX(), result.getEndY() + STEP);
	}

	private GraphBlockDto drawUnionScan(mxGraph graph, Object parent, JsonNode node, int x, int y) {
		final var scans = Lists.newArrayList(node.get("scans").elements());
		// draw union node
		// draw scans
		for (final var scan : scans) {
			final GraphBlockDto result = handleNode(graph, parent, scan, x, y);
			y = result.getEndY() + STEP;
		}

		y += VERTICAL_STEP_PLUS_HEIGHT;
		graph.insertVertex(parent, null, UNION_SCAN, x, y, 9 * CHAR_TO_WIDTH_FACTOR, DEFAULT_BLOCK_HEIGHT);
		y += DEFAULT_BLOCK_HEIGHT;

		return new GraphBlockDto(null, null, 0, 0, 0, y);
	}

	private GraphBlockDto drawIntersectScan(mxGraph graph, Object parent, JsonNode node, int x, int y) {
		final var scans = Lists.newArrayList(node.get("scans").elements());
		for (final var scan : scans) {
			x = drawSimpleNode(graph, parent, scan, x, y).getEndX() + STEP;
		}
		return new GraphBlockDto(null, null, 0, 0, x, VERTICAL_STEP_PLUS_HEIGHT);
	}

	private GraphBlockDto drawSimpleNode(mxGraph graph, Object parent, JsonNode node, int x, int y) {
		String operator = node.get(OPERATOR).asText();
		final StringBuilder builder = new StringBuilder(operator);
		double width = operator.length() * CHAR_TO_WIDTH_FACTOR;

		if (operator.startsWith(DISTINCT_SCAN)) {
			node = node.get("scan");
			builder.append(": ");
			operator = node.get(OPERATOR).asText();
			builder.append(operator);
			width += 2 + operator.length();
		}

		if (operator.startsWith(INDEX_SCAN)) {
			final String indexName = node.get("index").asText();

			builder.append("\n");
			builder.append(indexName);

			width = Math.max(width, indexName.length() * CHAR_TO_WIDTH_FACTOR);
		}

		if (operator.startsWith(KEY_SCAN)) {
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

		final var vertex = graph.insertVertex(parent, null, builder.toString(), x, y, width,
				GraphBuilderServiceImpl.DEFAULT_BLOCK_HEIGHT);

		return new GraphBlockDto(vertex, vertex, x, y, (int) (x + width), y + DEFAULT_BLOCK_HEIGHT);
	}

	private GraphBlockDto drawParallelChildren(mxGraph graph, Object parent, JsonNode node, int x, int y) {
		return handleNode(graph, parent, node.get("~child"), x, y);
	}

	private GraphBlockDto drawSequenceChildren(mxGraph graph, Object parent, JsonNode node, int x, int y) {
		final var children = Lists.newArrayList(node.get("~children").elements());

		for (final var child : children) {
			y = handleNode(graph, parent, child, x, y).getEndY();
		}

		return new GraphBlockDto(null, null, 0, 0, 0, y);
	}
}
