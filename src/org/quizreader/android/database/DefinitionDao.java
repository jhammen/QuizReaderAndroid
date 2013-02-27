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
import android.database.sqlite.SQLiteDatabase;

public class DefinitionDao extends BaseDao {

	private static final String TABLE_DEFINITIONS = "definition";
	private static final String FIELD_ID = "_id";
	private static final String FIELD_ROOT_ID = "root_id";
	private static final String FIELD_TEXT = "text";
	private static final String FIELD_TITLE_ID = "title_id";
	private static final String FIELD_WORD_ID = "word_id";

	public DefinitionDao(Context context) {
		super(context);
	}

	public static long insertDefinition(SQLiteDatabase db, String def, String titleId, long wordId, Long rootId) {
		ContentValues cv = new ContentValues();
		if (def.length() > 128) {
			def = def.substring(0, 125) + "...";
		}
		cv.put(FIELD_TEXT, def);
		cv.put(FIELD_TITLE_ID, titleId);
		cv.put(FIELD_WORD_ID, wordId);
		cv.put(FIELD_ROOT_ID, rootId);
		return db.insert(TABLE_DEFINITIONS, null, cv);
	}

	public List<Definition> getDefinitions(String wordId) {
		Cursor cursor = database.query(TABLE_DEFINITIONS, null, FIELD_WORD_ID + " = ?", new String[] { wordId }, null, null, null);
		cursor.moveToFirst();
		List<Definition> quizWords = new ArrayList<Definition>();
		while (cursor.isAfterLast() == false) {
			quizWords.add(cursorToDefinition(cursor));
			cursor.moveToNext();
		}
		cursor.close();
		return quizWords;
	}

	public List<Definition> getDefinitions(String titleId, String word, String language) {
		String query = "SELECT " + TABLE_DEFINITIONS + "." + FIELD_ID + ",";
		query += FIELD_TEXT + "," + FIELD_TITLE_ID + ',' + FIELD_WORD_ID;
		query += " FROM " + TABLE_DEFINITIONS + ", " + WordDao.TABLE_WORD;
		query += " WHERE " + FIELD_WORD_ID + "= " + WordDao.TABLE_WORD + "." + WordDao.FIELD_ID;
		query += " AND " + FIELD_TITLE_ID + "=? ";
		query += " AND " + WordDao.TABLE_WORD + "." + WordDao.FIELD_TOKEN + "=?";
		query += " AND " + WordDao.TABLE_WORD + "." + WordDao.FIELD_LANGUAGE + "=?";

		Cursor cursor = database.rawQuery(query, new String[] { titleId, word, language });

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
		def.setTitleId(cursor.getString(cursor.getColumnIndex(FIELD_TITLE_ID)));
		def.setWordId(cursor.getString(cursor.getColumnIndex(FIELD_WORD_ID)));
		def.setText(cursor.getString(cursor.getColumnIndex(FIELD_TEXT)));
		return def;
	}

}
