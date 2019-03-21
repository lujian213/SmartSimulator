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
import io.github.lujian213.simulator.util.ZipFileVisitor;
import io.github.lujian213.simulator.util.ZipFileVisitor.EntryWrapper;
import io.github.lujian213.simulator.util.ZipFileVisitor.ZipFileVisitorHandler;

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
		
		public void loadContent(File file) throws IOException {
			try (InputStream is = new FileInputStream(file)) {
				loadContent(is);
			}
		}

		public void loadContent(InputStream is) throws IOException {
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
	
	public SimulatorFolder(SimScript script) throws IOException {
		String fileName = script.getMyself().getName();
		if (script.getMyself().isFile() && fileName.endsWith(SimScript.ZIP_EXT)) {
			this.name = fileName.substring(0, fileName.length() - SimScript.ZIP_EXT.length());
			prepareSimulatorFolder(this, script, new ZipFile(script.getMyself()), "");
		} else {
			this.name = script.getMyself().getName();
			Arrays.asList(script.getMyself().listFiles(file -> file.isFile() && (file.getName().endsWith(SimScript.SCRIPT_EXT) || file.getName().equals(SimScript.INIT_FILE)) || file.isDirectory() && script.getSubScripts().containsKey(file.getName())))
			.stream().forEach(theFile -> {
				if (theFile.isDirectory()) {
					try {
						subFolders.add(new SimulatorFolder(script.getSubScripts().get(theFile.getName())));
					} catch (IOException e) {
					}
				} else {
					try {
						files.add(new SimulatorFile(theFile));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			});
		}
	}

	protected void prepareSimulatorFolder(SimulatorFolder folder, SimScript script, ZipFile zipFile, String path) throws IOException {
		Enumeration<? extends ZipEntry> it = zipFile.entries();
		while (it.hasMoreElements()) {
			ZipEntry entry = it.nextElement();
			String name = entry.getName();
			if (name.equals(path + SimScript.INIT_FILE) || name.startsWith(path) && name.endsWith(SimScript.SCRIPT_EXT) && !name.substring(path.length()).contains("/")) {
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
	
	public SimulatorFolder(File afile) throws IOException {
		if (afile.isDirectory()) {
			this.name = afile.getName();
			Arrays.asList(afile.listFiles(file -> file.isDirectory() || file.getName().endsWith(SimScript.SCRIPT_EXT) || file.getName().equals(SimScript.INIT_FILE) || file.getName().endsWith(SimScript.IGNORE_EXT)))
			.stream().forEach(theFile -> {
				if (theFile.isDirectory()) {
					try {
						subFolders.add(new SimulatorFolder(theFile));
					} catch (IOException e) {
					}
				} else {
					try {
						files.add(new SimulatorFile(theFile));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			});
		} else if(afile.isFile() && afile.getName().endsWith(SimScript.ZIP_EXT)){
			ZipFile zf = new ZipFile(afile);
			ZipFileVisitorHandler<SimulatorFolder> handler = new ZipFileVisitorHandler<SimulatorFolder>() {

				private EntryWrapper<SimulatorFolder> parent;
				@Override
				public void setParent(EntryWrapper<SimulatorFolder> parent) {
					this.parent = parent;
				}

				@Override
				public EntryWrapper<SimulatorFolder> handleDir(ZipEntry entry) {
					SimulatorFolder folder = new SimulatorFolder();
					folder.setName(new File(entry.getName()).getName());
					parent.getTarget().getSubFolders().add(folder);
					return new EntryWrapper<SimulatorFolder>(entry, folder);
				}

				@Override
				public void handleFile(ZipEntry entry) throws IOException {
					SimulatorFolder folder = parent.getTarget();
					String name = new File(entry.getName()).getName();
					if (name.equals(SimScript.INIT_FILE) || name.endsWith(SimScript.SCRIPT_EXT) || name.endsWith(SimScript.IGNORE_EXT)) {
						SimulatorFile file = new SimulatorFile();
						file.setName(name);
						try (InputStream is = zf.getInputStream(entry)) {
							file.loadContent(is);
						}
						folder.getFiles().add(file);
					}
				}
			};
			ZipFileVisitor<SimulatorFolder> visitor = new ZipFileVisitor<SimulatorFolder>(zf, this, handler);
			visitor.visit();
		} else {
			throw new IOException(afile + " is not a valid folder or zip file");
		}
	}
	
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
