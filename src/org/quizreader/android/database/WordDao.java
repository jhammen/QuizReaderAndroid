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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class WordDao extends BaseDao {

	static final String TABLE_WORD = "word";
	static final String FIELD_ID = "_id";
	static final String FIELD_LANGUAGE = "language";
	static final String FIELD_TOKEN = "token";
	static final String FIELD_QUIZ_LEVEL = "quiz_level";

	private DefinitionDao defDao;

	public WordDao(Context context) {
		super(context);
		defDao = new DefinitionDao(context);
	}

	public void open() {
		super.open();
		defDao.open(getDatabase());
	}

	public void open(SQLiteDatabase database) {
		super.open(database);
		defDao.open(database);
	}

	public void close() {
		super.close();
		defDao.close();
	}

	public long insert(Word word) {
		return insertOrGetWordId(database, word.getLanguage(), word.getToken(), word.getQuizLevel());
	}

	public static long insertOrGetWordId(SQLiteDatabase db, String language, String word, int quizLevel) {
		long wordId;
		String query = FIELD_TOKEN + " = ? AND " + FIELD_LANGUAGE + " = ?";
		Cursor cursor = db.query(TABLE_WORD, null, query, new String[] { word, language }, null, null, null);
		cursor.moveToFirst();
		if (cursor.isAfterLast()) {
			cursor.close();
			ContentValues cv = new ContentValues();
			cv.put(FIELD_LANGUAGE, language);
			cv.put(FIELD_TOKEN, word);
			cv.put(FIELD_QUIZ_LEVEL, quizLevel);
			wordId = db.insert(TABLE_WORD, null, cv);
		}
		else {
			wordId = cursor.getLong(cursor.getColumnIndex(FIELD_ID));
			cursor.close();
		}
		return wordId;
	}

	public Word getWord(String id) {
		String query = FIELD_ID + " = ?";
		Cursor cursor = database.query(TABLE_WORD, null, query, new String[] { id }, null, null, null);
		cursor.moveToFirst();
		if (cursor.isAfterLast()) {
			cursor.close();
			return null;
		}
		Word ret = cursorToWord(cursor);
		cursor.close();
		return ret;
	}

	public Word getWord(String word, String language) {
		String query = FIELD_TOKEN + " = ? AND " + FIELD_LANGUAGE + " = ?";
		Cursor cursor = database.query(TABLE_WORD, null, query, new String[] { word, language }, null, null, null);
		cursor.moveToFirst();
		if (cursor.isAfterLast()) {
			cursor.close();
			return null;
		}
		Word ret = cursorToWord(cursor);
		cursor.close();
		return ret;
	}

	private String randomQueryString() {
		String query = "SELECT " + TABLE_WORD + "." + FIELD_ID + ",";
		query += FIELD_LANGUAGE + "," + FIELD_TOKEN + ",";
		query += FIELD_QUIZ_LEVEL + " FROM ";
		query += TABLE_WORD + "," + DefinitionDao.TABLE_DEFINITIONS;
		query += " WHERE " + DefinitionDao.TABLE_DEFINITIONS + ".";
		query += DefinitionDao.FIELD_WORD_ID + "=" + TABLE_WORD + "." + FIELD_ID;
		query += " AND " + FIELD_LANGUAGE + "=? AND " + FIELD_TOKEN + "!=?";
		return query;
	}

	public List<Word> getWordAndRoots(String token, String language) {
		List<Word> ret = new ArrayList<Word>();
		Word word = getWord(token, language);
		ret.add(word);
		//Set<String> rootIds = new HashSet<String>();
		Map<String, Word> wordMap = new HashMap<String, Word>();
		for (Definition def : word.getDefinitions()) {
			String rootId = def.getRootId();
			if (rootId != null) {
				Word rootWord = wordMap.get(rootId);
				if (rootWord == null) {
					rootWord = getWord(rootId);
					wordMap.put(rootId, rootWord);
					ret.add(rootWord);
				}
				def.setRoot(rootWord.getToken());
			}
		}
		return ret;
	}

	public Word getRandomQuizWord(String language, String token, String token2) {
		String query = randomQueryString();
		query += " AND " + FIELD_TOKEN + "!=?";
		query += " ORDER BY RANDOM() LIMIT 1";
		Cursor cursor = database.rawQuery(query, new String[] { language, token, token2 });
		cursor.moveToFirst();
		Word quizWord = cursorToWord(cursor);
		cursor.close();
		return quizWord;
	}

	public Word getQuizWordLike(String language, String token, String like) {
		String query = randomQueryString();
		query += " AND " + WordDao.FIELD_TOKEN + " LIKE '" + like + "'";
		query += " ORDER BY RANDOM() LIMIT 1";
		Cursor cursor = database.rawQuery(query, new String[] { language, token });
		cursor.moveToFirst();
		if (cursor.isAfterLast()) {
			cursor.close();
			return null;
		}
		Word quizWord = cursorToWord(cursor);
		cursor.close();
		return quizWord;
	}

	// used by BackupWordsTask
	public List<Word> getLearnedWords() {
		String query = FIELD_QUIZ_LEVEL + " > 0";
		Cursor cursor = database.query(TABLE_WORD, null, query, null, null, null, null);
		cursor.moveToFirst();
		List<Word> words = new ArrayList<Word>();
		while (cursor.isAfterLast() == false) {
			words.add(cursorToWord(cursor));
			cursor.moveToNext();
		}
		cursor.close();
		return words;
	}

	private Word cursorToWord(Cursor cursor) {
		Word word = new Word();
		word.setId(cursor.getString(cursor.getColumnIndex(FIELD_ID)));
		word.setLanguage(cursor.getString(cursor.getColumnIndex(FIELD_LANGUAGE)));
		word.setToken(cursor.getString(cursor.getColumnIndex(FIELD_TOKEN)));
		word.setQuizLevel(cursor.getInt(cursor.getColumnIndex(FIELD_QUIZ_LEVEL)));

		word.setDefinitions(defDao.getDefinitions(word.getId()));

		return word;
	}

	public void updateLevel(String token, int level) {
		ContentValues cv = new ContentValues();
		cv.put(FIELD_QUIZ_LEVEL, level);
		database.update(TABLE_WORD, cv, FIELD_TOKEN + " =  ?", new String[] { token });
	}

}
