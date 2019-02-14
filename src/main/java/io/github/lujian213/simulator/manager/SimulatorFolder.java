package io.github.lujian213.simulator.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import io.github.lujian213.simulator.SimScript;

public class SimulatorFolder {
	public static class SimulatorFile {
		private String name;
		private String content;
		
		public SimulatorFile() throws IOException {
		}

		public SimulatorFile(File file) throws IOException {
			this.name = file.getName();
			loadContent(file);
		}
		
		protected void loadContent(File file) throws IOException {
			InputStream is = new FileInputStream(file);
			loadContent(is);
		}

		protected void loadContent(InputStream is) throws IOException {
			StringBuffer sb = new StringBuffer();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line).append("\n");
				}
			}
			content = sb.toString();
		}

		public String getName() {
			return name;
		}

		public String getContent() {
			return content;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setContent(String content) {
			this.content = content;
		}
	}

	private String name;
	private List<SimulatorFolder> subFolders = new ArrayList<>();
	private List<SimulatorFile> files = new ArrayList<>();
	
	public SimulatorFolder() {
	}
	
	public SimulatorFolder(SimScript script) {
		this.name = script.getMyself().getName();
		Arrays.asList(script.getMyself().listFiles(file -> file.isFile() && (file.getName().endsWith(".sim") || file.getName().equals("init.properties")) || file.isDirectory() && script.getSubScripts().containsKey(file.getName())))
		.stream().forEach(theFile -> {
			if (theFile.isDirectory()) {
				subFolders.add(new SimulatorFolder(script.getSubScripts().get(theFile.getName())));
			} else {
				try {
					files.add(new SimulatorFile(theFile));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	public SimulatorFolder(SimScript script, ZipFile zipFile) throws IOException {
		String fileName = script.getMyself().getName();
		this.name = fileName.substring(0, fileName.length() - 4);
		prepareSimulatorFolder(this, script, zipFile, "");
	}

	protected void prepareSimulatorFolder(SimulatorFolder folder, SimScript script, ZipFile zipFile, String path) throws IOException {
		Enumeration<? extends ZipEntry> it = zipFile.entries();
		while (it.hasMoreElements()) {
			ZipEntry entry = it.nextElement();
			String name = entry.getName();
			if (name.equals(path + "init.properties") || name.startsWith(path) && name.endsWith(".sim") && !name.substring(path.length()).contains("/")) {
				SimulatorFile file = new SimulatorFile();
				file.setName(new File(name).getName());
				file.loadContent(zipFile.getInputStream(entry));
				folder.files.add(file);
			}
		}
		for (Map.Entry<String, SimScript> entry: script.getSubScripts().entrySet()) {
			SimulatorFolder subFolder = new SimulatorFolder();
			subFolder.setName(entry.getKey());
			prepareSimulatorFolder(subFolder, entry.getValue(), zipFile, path + entry.getKey() + "/");
			folder.subFolders.add(subFolder);
		}
	}
	
//	public SimulatorFolder(File dir) {
//		this.name = dir.getName();
//		Arrays.asList(dir.listFiles(file -> file.isDirectory() || file.getName().endsWith(".sim") || file.getName().equals("init.properties")))
//		.stream().forEach(theFile -> {
//			if (theFile.isDirectory()) {
//				subFolders.add(new SimulatorFolder(theFile));
//			} else {
//				try {
//					files.add(new SimulatorFile(theFile));
//				} catch (IOException e) {
//					throw new RuntimeException(e);
//				}
//			}
//		});
//	}

	public String getName() {
		return name;
	}

	public List<SimulatorFolder> getSubFolders() {
		return subFolders;
	}

	public List<SimulatorFile> getFiles() {
		return files;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setSubFolders(List<SimulatorFolder> subFolders) {
		this.subFolders = subFolders;
	}

	public void setFiles(List<SimulatorFile> files) {
		this.files = files;
	}
}
