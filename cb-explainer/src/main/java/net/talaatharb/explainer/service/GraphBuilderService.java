package net.talaatharb.explainer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.mxgraph.view.mxGraph;

public interface GraphBuilderService {
	mxGraph buildGraphFromJsonPlan(final JsonNode node);
}
