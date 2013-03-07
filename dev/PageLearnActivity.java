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

import java.util.List;

import org.quizreader.android.database.Definition;
import org.quizreader.android.database.QuizWord;
import org.quizreader.android.database.QuizWordDao;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class PageLearnActivity extends BaseQuizReadActivity {

	private List<QuizWord> quizWords;
	private int counter;
	private TextView wordText;
	private TextView defText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_learn);
		wordText = (TextView) findViewById(R.id.wordText);
		defText = (TextView) findViewById(R.id.defText);

		// query for words
		QuizWordDao quizWordDao = new QuizWordDao(this);
		quizWordDao.open();
		quizWords = quizWordDao.getNewQuizWords(title.getId(), title.getSection(), title.getParagraph());
		quizWordDao.close();
		counter = 0;
		if (quizWords.size() == 0) { // no words means nothing to do here
			setResult(RESULT_OK);
			finish();
		}
		else {
			showNextQuizWord();
		}
	}

	public void kontinue(View view) {
		if (counter < quizWords.size()) {
			showNextQuizWord();
		}
		else {
			setResult(RESULT_OK);
			finish();
		}
	}

	private void showNextQuizWord() {
		QuizWord quizWord = quizWords.get(counter++);
		wordText.setText(quizWord.getWord().getToken());
		defText.setText("");
		for (Definition def : quizWord.getDefinitions()) {
			defText.append("\n*" + def.getText());
		}
	}

}
