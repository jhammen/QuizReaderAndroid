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
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.xmlpull.v1.XmlPullParserException;

public class QzzFile extends ZipFile {

	private static final String COMMON_XML = "common.xml";
	private List<ZipEntry> definitionEntries;
	private List<ZipEntry> xhtmlEntries;

	public QzzFile(File file) throws IOException {
		super(file);
		loadEntries();
	}

	public QzzFile(String filepath) throws IOException {
		super(filepath);
		loadEntries();
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

	public DefinitionReader getCommonDefinitionReader() throws ZipException, IOException, XmlPullParserException {
		ZipEntry entry = getEntry(COMMON_XML);
		return new DefinitionReader(getInputStream(entry), entry.getSize(), 0);
	}

	public DefinitionReader getDefinitionReader(int section) throws IOException, XmlPullParserException {
		ZipEntry entry = definitionEntries.get(section);
		return new DefinitionReader(getInputStream(entry), entry.getSize(), section);
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
