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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class QzzFile extends ZipFile {

	private static final String COMMON_XML = "common.xml";
	private static final String META_XML = "meta.xml";
	private static final String TAG_PROPERTY = "property";

	private List<ZipEntry> definitionEntries;
	private List<ZipEntry> xhtmlEntries;
	private Map<String, String> meta;

	public QzzFile(File file) throws IOException, XmlPullParserException {
		super(file);
		loadEntries();
		loadMeta();
	}

	public QzzFile(String filepath) throws IOException, XmlPullParserException {
		super(filepath);
		loadEntries();
		loadMeta();
	}

	private void loadEntries() {
		// scan entries
		definitionEntries = new ArrayList<ZipEntry>();
		xhtmlEntries = new ArrayList<ZipEntry>();
		Enumeration<? extends ZipEntry> entries = entries();
		while (entries.hasMoreElements()) {
			ZipEntry nextElement = entries.nextElement();
			String name = nextElement.getName();
			if (name.endsWith(".xml") && !name.equals(COMMON_XML)) {
				definitionEntries.add(nextElement);
			}
			else if (name.endsWith(".html")) {
				xhtmlEntries.add(nextElement);
			}
		}
	}

	public void loadMeta() throws XmlPullParserException, IOException {
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

	public String getTitle() {
		return meta.get("title");
	}

	public InputStreamReader getCommonDefinitionReader() throws IOException {
		ZipEntry entry = getEntry(COMMON_XML);
		return new InputStreamReader(getInputStream(entry));
	}

	public InputStreamReader getDefinitionReader(int section) throws IOException {
		ZipEntry entry = definitionEntries.get(section);
		return new InputStreamReader(getInputStream(entry));
	}

	public String getHtml(int section, int paragraph) throws IOException {
		ZipEntry entry = xhtmlEntries.get(section);
		InputStreamReader inputStream = new InputStreamReader(getInputStream(entry), "UTF-8");
		int lastbyte = inputStream.read();
		int nbyte = inputStream.read();
		int paraCount = 0;
		StringBuffer buff = new StringBuffer("<");
		while (nbyte != -1) {
			if (lastbyte == '<' && (nbyte == 'p' || nbyte == 'P')) {
				paraCount++;
				if (paraCount > paragraph) {
					break;
				}
			}
			if (paraCount == paragraph) {
				buff.append((char) nbyte);
			}
			lastbyte = nbyte;
			nbyte = inputStream.read();
		}
		inputStream.close();
		buff.deleteCharAt(buff.length() - 1);
		return buff.toString().trim();
	}

}
