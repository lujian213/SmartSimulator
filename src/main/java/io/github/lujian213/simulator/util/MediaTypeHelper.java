package io.github.lujian213.simulator.util;

import javax.ws.rs.core.MediaType;

public class MediaTypeHelper {
	private static String[] textTypeArray = new String[] {
		MediaType.APPLICATION_ATOM_XML,	
		MediaType.APPLICATION_JSON,	
		MediaType.APPLICATION_SVG_XML,	
		MediaType.APPLICATION_XHTML_XML,	
		MediaType.APPLICATION_XML,
		MediaType.TEXT_HTML,
		MediaType.TEXT_PLAIN,
		MediaType.TEXT_XML
	};
	
	public static boolean isText(String type) {
		if (type == null)
			return true;
		for (String textType: textTypeArray) {
			if (type.contains(textType))
				return true;
		}
		return false;
	}
}
