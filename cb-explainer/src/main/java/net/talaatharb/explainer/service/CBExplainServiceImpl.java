package net.talaatharb.explainer.service;

import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.stereotype.Service;

import com.couchbase.client.java.query.QueryResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CBExplainServiceImpl implements CBExplainService {

	@Override
	public String explain(CouchbaseTemplate template, String query) {
		log.info("Explaining...");
		final String explainQuery = "EXPLAIN " + query;

		// Execute the EXPLAIN query
		final QueryResult result = template.getCouchbaseClientFactory().getCluster().query(explainQuery);

		// Assuming the result is not null and has content
		if (result != null && !result.rowsAsObject().isEmpty()) {
			// Convert the result to a String for simplicity
			return result.rowsAsObject().get(0).toString();
		}

		return "No explain plan available";
	}

}
