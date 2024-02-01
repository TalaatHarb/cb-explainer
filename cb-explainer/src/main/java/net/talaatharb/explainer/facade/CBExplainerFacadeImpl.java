package net.talaatharb.explainer.facade;

import org.springframework.data.couchbase.core.CouchbaseTemplate;

import lombok.RequiredArgsConstructor;
import net.talaatharb.explainer.service.CBConnectionService;
import net.talaatharb.explainer.service.CBExplainerService;

@RequiredArgsConstructor
public class CBExplainerFacadeImpl implements CBExplainerFacade {

	private final CBConnectionService connectionService;
	private final CBExplainerService explainService;

	private CouchbaseTemplate template;

	@Override
	public boolean connect() {
		template = connectionService.connect();
		return template != null;
	}

	@Override
	public String explain(String query) {
		return explainService.explain(template, query);
	}

}
