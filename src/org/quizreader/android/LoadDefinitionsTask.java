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

import org.quizreader.android.database.Entry;
import org.quizreader.android.database.QuizWord;
import org.quizreader.android.database.QuizWordDao;
import org.quizreader.android.database.Title;
import org.quizreader.android.database.Word;
import org.quizreader.android.database.WordDao;
import org.quizreader.android.qzz.DefinitionReader;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class LoadDefinitionsTask extends AsyncTask<DefinitionReader, Integer, Integer> {

	private WordDao wordDao;
	private QuizWordDao quizWordDao;
	private ProgressDialog dialog;
	private Title title;

	public LoadDefinitionsTask(Context context, Title title) {
		this.title = title;
		wordDao = new WordDao(context);
		quizWordDao = new QuizWordDao(context);
		dialog = new ProgressDialog(context);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	}

	protected void onPreExecute() {
		this.dialog.setMessage("Loading QuizWords...");
		this.dialog.show();
	}

	@Override
	protected Integer doInBackground(DefinitionReader... readers) {
		DefinitionReader definitionReader = readers[0];
		try {
			wordDao.open();
			quizWordDao.open();
			// delete existing quizwords
			quizWordDao.deleteQuizWords(title.getId(), definitionReader.getSectionNumber());
			Entry entry = definitionReader.nextEntry();
			while (entry != null) {
				// create if word does not exist for this language
				Word word = wordDao.getWord(entry.getWord(), title.getLanguage());
				if (word == null) {
					word = new Word();
					word.setLanguage(title.getLanguage());
					word.setToken(entry.getWord());
					wordDao.save(word);
				}
				// create new QuizWord
				QuizWord quizWord = new QuizWord();
				quizWord.setWord(word);
				quizWord.setTitleId(title.getId());
				quizWord.setSection(entry.getSection());
				quizWord.setParagraph(entry.getParagraph());
				quizWord.setDefinitions(entry.getDefinitions());
				quizWordDao.save(quizWord);

				// publish progress and grab next entry
				long progress = entry.getXmlPosition() * 100 / definitionReader.getTotalSize();
				publishProgress((int) progress);
				entry = definitionReader.nextEntry();
			}
		} catch (Exception e) {
			// TODO: report error
			e.printStackTrace();
		} finally {
			wordDao.close();
			quizWordDao.close();
			definitionReader.close();
		}
		return definitionReader.getParagraphCount();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		for (int value : values) {
			dialog.setProgress(value);
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		if (dialog.isShowing()) {
			dialog.dismiss();
		}
	}

}