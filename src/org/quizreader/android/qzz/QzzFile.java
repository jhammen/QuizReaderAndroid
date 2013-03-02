/*
 This file is part of QuizReader.

 QuizReader is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 QuizReader is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with QuizReader.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quizreader.android.qzz;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.content.res.AssetManager;

public class QzzFile extends ZipFile implements TitleSource {

	private static final String COMMON_XML = "common.xml";
	private static final String META_XML = "meta.xml";
	private static final String TAG_PROPERTY = "property";

	private List<ZipEntry> definitionEntries;
	private List<ZipEntry> xhtmlEntries;
	private Map<String, String> meta;
	private Context context;

	public QzzFile(File file, Context context) throws IOException, XmlPullParserException {
		super(file);
		this.context = context;
		loadEntries();
		loadMeta();
	}

	public QzzFile(String filepath, Context context) throws IOException, XmlPullParserException {
		super(filepath);
		this.context = context;
		loadEntries();
		loadMeta();
	}

	public String getTitle() {
		return meta.get("title");
	}

	public String getAuthor() {
		return meta.get("author");
	}

	private void loadEntries() {
		// scan entries
		definitionEntries = new ArrayList<ZipEntry>();
		xhtmlEntries = new ArrayList<ZipEntry>();
		Enumeration<? extends ZipEntry> entries = entries();
		while (entries.hasMoreElements()) {
			ZipEntry nextElement = entries.nextElement();
			String name = nextElement.getName();
			if (name.endsWith(".xml") && !name.equals(COMMON_XML) && !name.equals(META_XML)) {
				definitionEntries.add(nextElement);
			}
			else if (name.endsWith(".html")) {
				xhtmlEntries.add(nextElement);
			}
		}
		Comparator<ZipEntry> byName = new ZipNameComparator();
		Collections.sort(xhtmlEntries, byName);
	}

	private class ZipNameComparator implements Comparator<ZipEntry> {
		@Override
		public int compare(ZipEntry arg0, ZipEntry arg1) {
			return arg0.getName().compareTo(arg1.getName());
		}
	}

	private void loadMeta() throws XmlPullParserException, IOException {
		meta = new HashMap<String, String>();
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();
		ZipEntry entry = getEntry(META_XML);
		InputStreamReader reader = new InputStreamReader(getInputStream(entry));
		xpp.setInput(reader);
		int eventType = xpp.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			String name = xpp.getName();
			if (eventType == XmlPullParser.START_TAG) {
				if (TAG_PROPERTY.equals(name)) {
					String propertyName = xpp.getAttributeValue(null, "name");
					String propertyValue = xpp.getAttributeValue(null, "value");
					meta.put(propertyName, propertyValue);
				}
			}
			eventType = xpp.next();
		}
	}

	public InputStreamReader getCommonDefinitionReader() throws IOException {
		ZipEntry entry = getEntry(COMMON_XML);
		return new InputStreamReader(getInputStream(entry));
	}

	public InputStreamReader getDefinitionReader(int section) throws IOException {
		ZipEntry entry = definitionEntries.get(section);
		return new InputStreamReader(getInputStream(entry));
	}

	public Reader getHTMLReader(int section) throws IOException {
		ZipEntry entry = xhtmlEntries.get(section);
		return new InputStreamReader(getInputStream(entry), "UTF-8");
	}

	@Override
	public URL getHTML(String titleId, int section) throws IOException {
		unpackResource("qr.js");
		unpackResource("qr.css");
		unpackFolder("images");
		ZipEntry entry;
		try {
			entry = xhtmlEntries.get(section);
		} catch (IndexOutOfBoundsException ex) {
			return null;
		}
		File outputFile = new File(context.getCacheDir(), titleId + "-" + entry.getName());
		if (!outputFile.exists()) {
			copy(getInputStream(entry), new FileOutputStream(outputFile));
		}
		return outputFile.toURL();
	}

	private void unpackFolder(String folderPath) throws IOException {
		File testFolder = new File(context.getCacheDir(), folderPath);
		if (!testFolder.exists()) {
			testFolder.mkdir();
		}
		AssetManager assetManager = context.getAssets();
		for (String fileName : assetManager.list(folderPath)) {
			unpackResource(folderPath + "/" + fileName);
		}
	}

	private void unpackResource(String fileName) throws IOException {
		File cacheFile = new File(context.getCacheDir(), fileName);
		if (!cacheFile.exists()) {
			AssetManager assetManager = context.getAssets();
			copy(assetManager.open(fileName), new FileOutputStream(cacheFile));
		}
	}

	private void copy(InputStream is, OutputStream os) throws IOException {
		byte buf[] = new byte[2048];
		int len;
		while ((len = is.read(buf)) != -1) {
			os.write(buf, 0, len);
		}
		os.close();
		is.close();
	}

}
