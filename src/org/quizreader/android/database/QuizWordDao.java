/*
 This file is part of QuizReader.

 Quiimport java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
oundation, either version 3 of the License, or
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

public class QuizWordDao extends BaseDao {

	private static final String TABLE_QUIZ_WORDS = "quizword";
	private static final String FIELD_ID = "_id";
	private static final String FIELD_TITLE_ID = "title_id";
	private static final String FIELD_WORD_ID = "word_id";
	private static final String FIELD_PARAGRAPH = "paragraph";
	private static final String FIELD_SECTION = "section";

	private DefinitionDao defDao;

	public QuizWordDao(Context context) {
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

	public static long insertQuizWord(SQLiteDatabase db, long wordId, String titleId, int section, int paragraphCounter) {
		ContentValues cv = new ContentValues();
		cv.put(FIELD_TITLE_ID, titleId);
		cv.put(FIELD_WORD_ID, wordId);
		cv.put(FIELD_PARAGRAPH, paragraphCounter);
		cv.put(FIELD_SECTION, section);
		return db.insert(QuizWordDao.TABLE_QUIZ_WORDS, null, cv);
	}

	public List<QuizWord> getQuizWords(String titleId, int section, int paragraph) {
		String query = joinQueryString();
		query += " AND " + FIELD_TITLE_ID + "=? AND " + FIELD_SECTION + "=? AND " + FIELD_PARAGRAPH + "=?";
		String[] queryArgs = new String[] { titleId, Integer.toString(section), Integer.toString(paragraph) };
		Cursor cursor = database.rawQuery(query, queryArgs);
		// db.query(TABLE_QUIZ_WORDS, null, query, queryArgs, null, null, null);
		return cursorToQuizWords(cursor);
	}

	public QuizWord getRandomQuizWord(String titleId, String quizWordId) {
		String query = joinQueryString();
		query += " AND " + FIELD_TITLE_ID + "=? AND " + TABLE_QUIZ_WORDS + "." + FIELD_ID + "!=?";
		query += " ORDER BY RANDOM() LIMIT 1";
		Cursor cursor = database.rawQuery(query, new String[] { titleId, quizWordId });
		cursor.moveToFirst();
		QuizWord quizWord = cursorToQuizWord(cursor);
		cursor.close();
		quizWord.setDefinitions(defDao.getDefinitions(quizWord.getWord().getId()));
		return quizWord;
	}

	public List<QuizWord> getQuizWordsLike(String titleId, String quizWordId, String like) {
		String query = joinQueryString();
		query += " AND " + FIELD_TITLE_ID + "=? AND " + TABLE_QUIZ_WORDS + "." + FIELD_ID + "!=?";
		query += " AND " + WordDao.FIELD_TOKEN + " LIKE '" + like + "'";
		Cursor cursor = database.rawQuery(query, new String[] { titleId, quizWordId });
		return cursorToQuizWords(cursor);
	}

	private String joinQueryString() {
		String query = "SELECT " + TABLE_QUIZ_WORDS + "." + FIELD_ID + ",";
		query += FIELD_WORD_ID + "," + WordDao.FIELD_LANGUAGE + "," + WordDao.FIELD_TOKEN + ",";
		query += FIELD_TITLE_ID + "," + FIELD_SECTION + "," + FIELD_PARAGRAPH;
		query += " FROM " + TABLE_QUIZ_WORDS + ", " + WordDao.TABLE_WORD;
		query += " WHERE " + FIELD_WORD_ID + "= " + WordDao.TABLE_WORD + "." + WordDao.FIELD_ID;
		return query;
	}

	public void deleteQuizWord(String wordId, String titleId) {
		String query = FIELD_WORD_ID + "=? AND " + FIELD_TITLE_ID + "=?";
		database.delete(TABLE_QUIZ_WORDS, query, new String[] { wordId, titleId });
	}

	public static void deleteQuizWords(SQLiteDatabase db, String titleId, int section) {
		String query = FIELD_TITLE_ID + "=? AND " + FIELD_SECTION + "=?";
		db.delete(TABLE_QUIZ_WORDS, query, new String[] { titleId, Integer.toString(section) });
	}

	private List<QuizWord> cursorToQuizWords(Cursor cursor) {
		cursor.moveToFirst();
		List<QuizWord> quizWords = new ArrayList<QuizWord>();
		while (cursor.isAfterLast() == false) {
			quizWords.add(cursorToQuizWord(cursor));
			cursor.moveToNext();
		}
		cursor.close();

		for (QuizWord quizWord : quizWords) {
			quizWord.setDefinitions(defDao.getDefinitions(quizWord.getWord().getId()));
		}

		return quizWords;
	}

	private QuizWord cursorToQuizWord(Cursor cursor) {
		QuizWord quizWord = new QuizWord();
		quizWord.setId(cursor.getString(cursor.getColumnIndex(FIELD_ID)));
		quizWord.setTitleId(cursor.getString(cursor.getColumnIndex(FIELD_TITLE_ID)));
		Word word = new Word();
		word.setId(cursor.getString(cursor.getColumnIndex(FIELD_WORD_ID)));
		word.setToken(cursor.getString(cursor.getColumnIndex(WordDao.FIELD_TOKEN)));
		word.setLanguage(cursor.getString(cursor.getColumnIndex(WordDao.FIELD_LANGUAGE)));
		quizWord.setWord(word);
		quizWord.setSection(cursor.getInt(cursor.getColumnIndex(FIELD_SECTION)));
		quizWord.setParagraph(cursor.getInt(cursor.getColumnIndex(FIELD_PARAGRAPH)));
		return quizWord;
	}

}
