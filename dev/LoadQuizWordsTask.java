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
import java.util.HashSet;
import java.util.Set;

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

public class LoadQuizWordsTask extends AsyncTask<Reader, String, Integer> {

	private static final String TAG_TOKEN = "A";
	private static final String TAG_PARAGRAPH = "P";

	private QRDatabaseHelper databaseHelper;
	protected SQLiteDatabase db;

	private ProgressDialog dialog;
	protected Title title;

	public LoadQuizWordsTask(Context context, Title title) {
		this.title = title;
		databaseHelper = new QRDatabaseHelper(context);

		dialog = new ProgressDialog(context) {
			@Override
			public void onBackPressed() {
				LoadQuizWordsTask.this.cancel(true);
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

		int paragraphCounter = 1;

		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xpp = factory.newPullParser();

			xpp.setInput(readers[0]);

			// delete existing quizwords
			QuizWordDao.deleteQuizWords(db, title.getId(), title.getSection());

			int wordCounter = 0;
			Set<String> paraSet = new HashSet<String>();

			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT && !isCancelled()) {
				String name = xpp.getName();
				if (eventType == XmlPullParser.START_TAG) {
					if (TAG_TOKEN.equalsIgnoreCase(name)) {
						xpp.next();
						String word = xpp.getText();
						if (!paraSet.contains(word)) {
							paraSet.add(word);
							// see if word exists in db
							long wordId = WordDao.insertOrGetWordId(db, title.getLanguage(), word, 0);
							// create new QuizWord
							QuizWordDao.insertQuizWord(db, wordId, title.getId(), title.getSection(), paragraphCounter);
						}
					}
				}
				else if (eventType == XmlPullParser.END_TAG) {
					if (TAG_TOKEN.equalsIgnoreCase(name)) {
						publishProgress("Loaded " + wordCounter++ + " quiz words");
					}
					else if (TAG_PARAGRAPH.equalsIgnoreCase(name)) {
						paraSet.clear();
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