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

public class DefinitionDao extends BaseDao {

	private static final String TABLE_DEFINITIONS = "definition";
	private static final String FIELD_ID = "_id";
	private static final String FIELD_TEXT = "text";
	private static final String FIELD_WORD_ID = "quizword_id";

	public DefinitionDao(Context context) {
		super(context);
	}

	public void save(Definition definition) {
		ContentValues cv = new ContentValues();
		String def = definition.getText();
		if (def.length() > 128) {
			def = def.substring(0, 125) + "...";
		}
		cv.put(FIELD_TEXT, def);
		cv.put(FIELD_WORD_ID, definition.getQuizWordId());
		if (definition.getId() == null) {
			long id = db.insert(TABLE_DEFINITIONS, null, cv);
			definition.setId(Long.toString(id));
		}
		else {
			db.update(TABLE_DEFINITIONS, cv, FIELD_ID + " =  ?", new String[] { definition.getId() });
		}
	}

	public List<Definition> getDefinitions(String quizWordId) {
		Cursor cursor = db.query(TABLE_DEFINITIONS, null, FIELD_WORD_ID + " = ?", new String[] { quizWordId }, null, null, null);
		cursor.moveToFirst();
		List<Definition> quizWords = new ArrayList<Definition>();
		while (cursor.isAfterLast() == false) {
			quizWords.add(cursorToDefinition(cursor));
			cursor.moveToNext();
		}
		cursor.close();
		return quizWords;
	}

	private Definition cursorToDefinition(Cursor cursor) {
		Definition def = new Definition();
		def.setId(cursor.getString(cursor.getColumnIndex(FIELD_ID)));
		def.setQuizWordId(cursor.getString(cursor.getColumnIndex(FIELD_WORD_ID)));
		def.setText(cursor.getString(cursor.getColumnIndex(FIELD_TEXT)));
		return def;
	}

}
