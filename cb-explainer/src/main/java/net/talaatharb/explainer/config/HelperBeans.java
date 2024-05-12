package net.talaatharb.explainer.config;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import net.talaatharb.explainer.facade.CBExplainerFacade;
import net.talaatharb.explainer.facade.CBExplainerFacadeImpl;
import net.talaatharb.explainer.service.CBConnectionService;
import net.talaatharb.explainer.service.CBConnectionServiceImpl;
import net.talaatharb.explainer.service.CBExplainerService;
import net.talaatharb.explainer.service.CBExplainerServiceImpl;
import net.talaatharb.explainer.service.GraphBuilderService;
import net.talaatharb.explainer.service.GraphBuilderServiceImpl;

public class HelperBeans {

	private HelperBeans() {
	}

	public static final ObjectMapper buildObjectMapper() {
		return JsonMapper.builder().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS) // ignore case
				.enable(SerializationFeature.INDENT_OUTPUT) // pretty format for json
				.addModule(new JavaTimeModule()) // time module
				.build();
	}

	public static final CBConnectionService buildConnectionService() {
		return new CBConnectionServiceImpl();
	}

	public static final CBExplainerService buildExplainerService() {
		return new CBExplainerServiceImpl();
	}

	public static final CBExplainerFacade buildExplainerFacade(CBConnectionService connectionService,
			CBExplainerService explainerService) {
		return new CBExplainerFacadeImpl(connectionService, explainerService);
	}

	public static final GraphBuilderService buildGraphBuilderService() {
		return new GraphBuilderServiceImpl();
	}

}
