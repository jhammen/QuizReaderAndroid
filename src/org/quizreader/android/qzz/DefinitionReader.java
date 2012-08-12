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

import java.io.BufferedReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipException;

import org.quizreader.android.database.Definition;
import org.quizreader.android.database.Entry;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class DefinitionReader {

	private static final String ATTRIBUTE_TITLE = "title";
	private static final String TAG_DEF = "def";
	private static final String TAG_DEFINITIONS = "definitions";
	private static final String TAG_ENTRY = "entry";
	private static final Object TAG_PARAGRAPH = "paragraph";

	private XmlPullParser xpp;
	private CounterInputStream counterStream;
	private long totalSize;
	private int sectionNumber;
	private int paragraphCounter;
	private Entry currentEntry;

	public DefinitionReader(InputStream inputStream, long size, int section) throws ZipException, IOException, XmlPullParserException {

		this.totalSize = size;
		this.sectionNumber = section;

		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		xpp = factory.newPullParser();

		counterStream = new CounterInputStream(inputStream);
		BufferedReader br = new BufferedReader(new InputStreamReader(counterStream));
		xpp.setInput(br);

		currentEntry = null;
		paragraphCounter = 1; // index is 1-based
	}

	public long getTotalSize() {
		return totalSize;
	}

	public Integer getParagraphCount() {
		return paragraphCounter;
	}

	public int getSectionNumber() {
		return sectionNumber;
	}

	public void close() {
		try {
			counterStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Entry nextEntry() throws IOException, XmlPullParserException {

		int eventType = xpp.getEventType();

		while (eventType != XmlPullParser.END_DOCUMENT) {
			String name = xpp.getName();
			if (eventType == XmlPullParser.START_TAG) {
				if (TAG_ENTRY.equals(name)) {
					String word = xpp.getAttributeValue(null, ATTRIBUTE_TITLE);
					currentEntry = new Entry(word);
					currentEntry.setXmlPosition(counterStream.getCounter());
					currentEntry.setSection(sectionNumber);
					currentEntry.setParagraph(paragraphCounter);
				}
				else if (TAG_DEF.equals(name)) {
					xpp.next();
					String text = xpp.getText();
					if (text == null) {
						text = "";
					}
					Definition definition = new Definition();
					definition.setText(text);
					currentEntry.getDefinitions().add(definition);
				}
			}
			else if (eventType == XmlPullParser.END_TAG) {
				if (TAG_ENTRY.equals(name)) {
					xpp.next();
					return currentEntry;
				}
				if (TAG_DEFINITIONS.equals(name)) {
					break;
				}
				else if (TAG_PARAGRAPH.equals(name)) {
					paragraphCounter++;
				}
			}
			eventType = xpp.next();
		}
		return null;
	}

	public class CounterInputStream extends FilterInputStream {

		private long counter = 0;

		public CounterInputStream(InputStream in) {
			super(in);
		}

		public synchronized long getCounter() {
			return counter;
		}

		@Override
		public synchronized int read() throws IOException {
			counter++;
			return super.read();
		}

		@Override
		public synchronized int read(byte[] b, int off, int len) throws IOException {
			int n = super.read(b, off, len);
			counter += n;
			return n;
		}

	}

}
