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
import org.quizreader.android.database.Title;
import org.quizreader.android.database.WordDao;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

public class LoadDefinitionsTask extends AsyncTask<Reader, String, Integer> {

	private static final String ATTRIBUTE_ROOTS = "root";
	private static final String ATTRIBUTE_TITLE = "title";
	private static final String TAG_DEF = "def";
	private static final String TAG_DEFINITIONS = "definitions";
	private static final String TAG_ENTRY = "entry";

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
		this.dialog.setMessage("Loading Definitions...");
		this.dialog.show();
	}

	@Override
	protected Integer doInBackground(Reader... readers) {

		int wordCounter = 0;

		db = databaseHelper.getWritableDatabase();
		db.beginTransaction();
		setup();

		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xpp = factory.newPullParser();
			xpp.setInput(readers[0]);

			// String language = null;
			long wordId = -1;

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
						// get word id if already in db, otherwise insert
						wordId = WordDao.insertOrGetWordId(db, title.getLanguage(), word, 0);
					}
					else if (TAG_DEF.equals(name)) {
						String roots = xpp.getAttributeValue(null, ATTRIBUTE_ROOTS);
						xpp.next();
						String text = xpp.getText();
						if (text == null) {
							text = "";
						}
						Long rootId = null;
						if (roots != null && roots.length() > 0) {
							String root = roots.split(",")[0]; // take only the first root if multiple
							rootId = WordDao.insertOrGetWordId(db, title.getLanguage(), root, 0);
						}
						DefinitionDao.insertDefinition(db, text, title.getId(), wordId, rootId);
					}
				}
				else if (eventType == XmlPullParser.END_TAG) {
					if (TAG_ENTRY.equals(name)) {
						publishProgress("Loaded " + wordCounter++ + " words");
					}
					if (TAG_DEFINITIONS.equals(name)) {
						break;
					}
				}
				eventType = xpp.next();
			}
			if (!isCancelled()) {
				afterLoad(db);
				db.setTransactionSuccessful();
			}
		} catch (Exception e) {
			e.printStackTrace();
			publishProgress(e.getClass() + ": " + e.getLocalizedMessage());
		} finally {
			db.endTransaction();
			db.close();
		}
		return wordCounter;
	}

	protected void setup() {
		// for subclasses, default implementation nothing
	}

	protected void afterLoad(SQLiteDatabase db) {
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