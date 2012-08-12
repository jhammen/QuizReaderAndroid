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
package org.quizreader.android.database;

import java.util.ArrayList;
import java.util.List;

public class Entry {

	private String word;
	private int section;
	private int paragraph;
	private List<Definition> definitions = new ArrayList<Definition>();

	private long xmlPosition;

	public Entry(String word) {
		super();
		this.word = word;
	}

	public int getSection() {
		return section;
	}

	public void setSection(int section) {
		this.section = section;
	}

	public int getParagraph() {
		return paragraph;
	}

	public void setParagraph(int paragraph) {
		this.paragraph = paragraph;
	}

	public String getWord() {
		return word;
	}

	public List<Definition> getDefinitions() {
		return definitions;
	}

	public void setDefinitions(List<Definition> definitions) {
		this.definitions = definitions;
	}

	public long getXmlPosition() {
		return xmlPosition;
	}

	public void setXmlPosition(long xmlPosition) {
		this.xmlPosition = xmlPosition;
	}

}
