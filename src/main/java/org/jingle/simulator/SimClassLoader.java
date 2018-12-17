package org.jingle.simulator;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

public class SimClassLoader extends URLClassLoader {
	public SimClassLoader(File dir, ClassLoader parent) throws IOException {
		super(new URL[] {dir.toURI().toURL()}, parent);
		if (dir.isDirectory()) {
			Arrays.asList(dir.listFiles((dir_file, name)-> name.endsWith(".jar")||name.endsWith(".zip")))
			.stream().forEach((file)->{
				try {
					this.addURL(file.toURI().toURL());
				} catch (MalformedURLException e) {
				}
			});
		}
	}
	
	
}
