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
package org.quizreader.android;

import java.io.Reader;

import org.quizreader.android.database.DefinitionDao;
import org.quizreader.android.database.QRDatabaseHelper;
import org.quizreader.android.database.QuizWordDao;
import org.quizreader.android.database.Title;
import org.quizreader.android.database.WordDao;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

public class LoadDefinitionsTask extends AsyncTask<Reader, String, Integer> {

	private static final String ATTRIBUTE_TITLE = "title";
	private static final String TAG_DEF = "def";
	private static final String TAG_DEFINITIONS = "definitions";
	private static final String TAG_ENTRY = "entry";
	private static final Object TAG_PARAGRAPH = "paragraph";

	private QRDatabaseHelper databaseHelper;
	protected SQLiteDatabase db;

	private ProgressDialog dialog;
	protected Title title;

	public LoadDefinitionsTask(Context context, Title title) {
		this.title = title;
		databaseHelper = new QRDatabaseHelper(context);

		dialog = new ProgressDialog(context) {
			@Override
			public void onBackPressed() {
				LoadDefinitionsTask.this.cancel(true);
				super.onBackPressed();
			}
		};
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	}

	@Override
	protected void onPreExecute() {
		this.dialog.setMessage("Loading QuizWords...");
		this.dialog.show();
	}

	@Override
	protected Integer doInBackground(Reader... readers) {

		db = databaseHelper.getWritableDatabase();
		db.beginTransaction();
		setup();

		int paragraphCounter = 1;

		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xpp = factory.newPullParser();

			xpp.setInput(readers[0]);

			// delete existing quizwords
			QuizWordDao.deleteQuizWords(db, title.getId(), title.getSection());

			// String language = null;
			long quizWordId = -1;
			int wordCounter = 0;

			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT && !isCancelled()) {
				String name = xpp.getName();
				if (eventType == XmlPullParser.START_TAG) {
					if (TAG_DEFINITIONS.equals(name)) {
						// language = xpp.getAttributeValue(null, ATTRIBUTE_LANGUAGE);
						// if (language == null || language.length() != 2) {
						// dialog.setMessage("Bad language attribute on definitions");
						// return 0;
						// }
					}
					if (TAG_ENTRY.equals(name)) {
						String word = xpp.getAttributeValue(null, ATTRIBUTE_TITLE);
						// see if word exists in db
						long wordId = WordDao.insertOrGetWordId(db, title.getLanguage(), word);
						// create new QuizWord
						String titleId = title.getId();
						int section = title.getSection();
						quizWordId = QuizWordDao.insertQuizWord(db, wordId, titleId, section, paragraphCounter);

					}
					else if (TAG_DEF.equals(name)) {
						xpp.next();
						String text = xpp.getText();
						if (text == null) {
							text = "";
						}
						DefinitionDao.insertDefinition(db, text, quizWordId);
					}
				}
				else if (eventType == XmlPullParser.END_TAG) {
					if (TAG_ENTRY.equals(name)) {
						publishProgress("Loaded " + wordCounter++ + " words");
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
			if (!isCancelled()) {
				db.setTransactionSuccessful();
			}
		} catch (Exception e) {
			publishProgress(e.getClass() + ": " + e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			db.endTransaction();
			db.close();
		}
		return paragraphCounter;
	}

	protected void setup() {
		// for subclasses, default implementation nothing
	}

	@Override
	protected void onProgressUpdate(String... values) {
		for (String value : values) {
			dialog.setMessage(value);
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		if (dialog.isShowing()) {
			dialog.dismiss();
		}
	}

}