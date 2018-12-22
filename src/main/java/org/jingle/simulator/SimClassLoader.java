package org.jingle.simulator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class SimClassLoader extends URLClassLoader {
	private boolean supportNestedtJar = false;
	
	public SimClassLoader(URL[] urls, ClassLoader parent) throws IOException {
		super(urls, parent);
		supportNestedtJar = true;
	}
	
	public SimClassLoader(File libDir, ClassLoader parent) throws IOException {
		super(new URL[0], parent);
		Arrays.asList(libDir.listFiles((dir_file, name)-> name.endsWith(".jar")||name.endsWith(".zip")))
		.stream().forEach((f)->{
			try {
				this.addURL(f.toURI().toURL());
			} catch (MalformedURLException e) {
			}
		});
	}

	@Override
	protected Class<?> findClass(String className) throws ClassNotFoundException {
		if (supportNestedtJar) {
			String str = className.replace('.', '/').concat(".class");
			URL[] urls = this.getURLs();
			for (URL url : urls) {
				try {
				byte[] bytes = readFromNestedJar(url, str);
				if (bytes != null) {
					return this.defineClass(null, bytes, 0, bytes.length);
				}
				} catch (IOException e) {
				}
			}
			throw new ClassNotFoundException();
		} else {
			return super.findClass(className);
		}
	}
	
	protected static byte[] readFromNestedJar(URL url, String name) throws IOException {
		try (JarInputStream jis = new JarInputStream(url.openStream()); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			JarEntry entry = null;
			byte[] bytes = null;
			while ((entry = jis.getNextJarEntry()) != null) {
				if (name.equals(entry.getName())) {
					byte[] buffer = new byte[10 * 1024];
					int ret = -1;
					try {
						while ((ret = jis.read(buffer)) != -1) {
							bos.write(buffer, 0, ret);
						}
						bos.flush();
						bytes = bos.toByteArray();
					} finally {
						jis.closeEntry();
					}
				}
			}
			return bytes;
		}
	}
}
