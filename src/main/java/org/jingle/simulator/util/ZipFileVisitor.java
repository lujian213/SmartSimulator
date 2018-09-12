package org.jingle.simulator.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ZipFileVisitor<T> {
	public static class EntryWrapper<T> {
		ZipEntry entry;
		T target;
		public EntryWrapper(ZipEntry entry, T target) {
			this.entry = entry;
			this.target = target;
		}
		public ZipEntry getEntry() {
			return entry;
		}
		public T getTarget() {
			return target;
		}
		public String getName() {
			if (entry != null) {
				return entry.getName();
			} else {
				return null;
			}
		}
		
		public boolean isDirectory() {
			if (entry != null) {
				return entry.isDirectory();
			} else {
				return true;
			}
		}
	}
	
	public interface ZipFileVisitorHandler<T> {
		public void setParent(EntryWrapper<T> parent);
		public EntryWrapper<T> handleDir(ZipEntry entry);
		public void handleFile(ZipEntry entry) throws IOException;
	}
	
	private ZipFile file;
	private T target;
	private ZipFileVisitorHandler<T> handler;
	
	public ZipFileVisitor(ZipFile file, T target, ZipFileVisitorHandler<T> handler) {
		this.file = file;
		this.target = target;
		this.handler = handler;
	}
	
	public void visit() throws ZipException, IOException {
		try (ZipFile zfile = file) {
			Enumeration<? extends ZipEntry> entries = zfile.entries();
			List<ZipEntry> entryList = new ArrayList<>();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				entryList.add(entry);
			}
			handleOneLevelDown(new EntryWrapper[] { new EntryWrapper<T>(null, target) }, entryList);
		}
	}
	
	protected void handleOneLevelDown(EntryWrapper<T>[] roots, List<ZipEntry> entryList) throws IOException {
		for (EntryWrapper<T> root: roots) {
			List<EntryWrapper<T>> dirList = new ArrayList<>();
			File rootFile = (root.getName() == null ? null : new File(root.getName()));
			handler.setParent(root);
			Iterator<ZipEntry> it = entryList.iterator();
			List<ZipEntry> tempDirList = new ArrayList<>();
			while (it.hasNext()) {
				ZipEntry entry = it.next();
				File entryFile = new File(entry.getName());
				if (entryFile.getParentFile() == rootFile || entryFile.getParentFile().equals(rootFile)) {
					if (entry.isDirectory()) {
						tempDirList.add(entry);
					} else {
						handler.handleFile(entry);
					}
					it.remove();
				}
			}
			for (ZipEntry dir: tempDirList) {
				dirList.add(handler.handleDir(dir));
			}
			handleOneLevelDown(dirList.toArray(new EntryWrapper[0]), entryList);
		}
	}
}
