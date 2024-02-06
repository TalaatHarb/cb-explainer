package net.talaatharb.explainer.dtos;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GraphBlockDto {

	private final Object startNode;
	private final Object endNode;

	private final int startX;
	private final int startY;
	private final int endX;
	private final int endY;
}
