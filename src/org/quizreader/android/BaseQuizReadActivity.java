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

import org.quizreader.android.database.Title;
import org.quizreader.android.database.TitleDao;

import android.app.Activity;
import android.os.Bundle;

public class BaseQuizReadActivity extends Activity {

	protected TitleDao titleDao;
	protected Title title;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// get the currently loaded title
		String titleId = getIntent().getStringExtra("titleId");
		titleDao = new TitleDao(this);
		titleDao.open();
		title = titleDao.getTitleById(titleId);
		titleDao.close();
	}

	protected void updateTitle(int section, int paragraph) {
		title.setSection(section);
		title.setParagraph(paragraph);
		titleDao.open();
		titleDao.updateTitle(title);
		titleDao.close();
	}

}
