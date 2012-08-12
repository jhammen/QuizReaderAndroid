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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class TitleDao extends BaseDao {

	static final String TABLE_TITLES = "title";
	static final String FIELD_FILEPATH = "filepath";
	static final String FIELD_NAME = "name";
	static final String FIELD_ID = "_id";
	static final String FIELD_LANGUAGE = "language";
	static final String FIELD_SECTION = "section";
	static final String FIELD_TOTAL_SECTIONS = "total_sections";
	static final String FIELD_PARAGRAPH = "paragraph";
	static final String FIELD_TOTAL_PARAGRAPHS = "total_paragraphs";

	public TitleDao(Context context) {
		super(context);
	}

	public void saveTitle(Title title) {
		ContentValues cv = new ContentValues();
		cv.put(FIELD_NAME, title.getName());
		cv.put(FIELD_LANGUAGE, title.getLanguage());
		cv.put(FIELD_FILEPATH, title.getFilepath());
		cv.put(FIELD_SECTION, title.getSection());
		cv.put(FIELD_TOTAL_SECTIONS, title.getTotalSections());
		cv.put(FIELD_PARAGRAPH, title.getParagraph());
		cv.put(FIELD_TOTAL_PARAGRAPHS, title.getTotalParagraphs());
		if (title.getId() == null) {
			long id = db.insert(TABLE_TITLES, null, cv);
			title.setId(Long.toString(id));
		}
		else {
			db.update(TABLE_TITLES, cv, FIELD_ID + " =  ?", new String[] { title.getId() });
		}
	}

	public List<Title> getAllTitles() {
		Cursor cursor = db.query(TABLE_TITLES, null, null, null, null, null, null);
		cursor.moveToFirst();
		List<Title> titles = new ArrayList<Title>();
		while (cursor.isAfterLast() == false) {
			titles.add(cursorToTitle(cursor));
			cursor.moveToNext();
		}
		cursor.close();
		return titles;
	}

	public Title getTitleById(String titleId) {
		Cursor cursor = db.query(TABLE_TITLES, null, FIELD_ID + " = ?", new String[] { titleId }, null, null, null);
		cursor.moveToFirst();
		Title ret = cursorToTitle(cursor);
		cursor.close();
		return ret;
	}

	private Title cursorToTitle(Cursor cursor) {
		Title title = new Title();
		title.setId(cursor.getString(cursor.getColumnIndex(FIELD_ID)));
		title.setName(cursor.getString(cursor.getColumnIndex(FIELD_NAME)));
		title.setLanguage(cursor.getString(cursor.getColumnIndex(FIELD_LANGUAGE)));
		title.setSection(cursor.getInt(cursor.getColumnIndex(FIELD_SECTION)));
		title.setTotalSections(cursor.getInt(cursor.getColumnIndex(FIELD_TOTAL_SECTIONS)));
		title.setParagraph(cursor.getInt(cursor.getColumnIndex(FIELD_PARAGRAPH)));
		title.setTotalParagraphs(cursor.getInt(cursor.getColumnIndex(FIELD_TOTAL_PARAGRAPHS)));
		title.setFilepath(cursor.getString(cursor.getColumnIndex(FIELD_FILEPATH)));
		return title;
	}

}
