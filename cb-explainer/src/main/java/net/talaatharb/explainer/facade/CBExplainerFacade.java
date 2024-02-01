package net.talaatharb.explainer.facade;

public interface CBExplainerFacade {

	boolean connect();

	String explain(String query);
}
