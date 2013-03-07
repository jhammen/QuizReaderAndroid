package org.quizreader.android;

import java.util.List;

import org.quizreader.android.database.QuizWord;
import org.quizreader.android.database.QuizWordDao;
import org.quizreader.android.database.Title;
import org.quizreader.android.database.WordDao;

import android.content.Context;

public class WordService {

	private QuizWordDao quizWordDao;
	private Title title;
	private WordDao wordDao;

	public WordService(Context context, Title title) {
		quizWordDao = new QuizWordDao(context);
		wordDao = new WordDao(context);
		this.title = title;
	}

	public List<QuizWord> getQuizWords() {
		quizWordDao.open();
		List<QuizWord> quizWords = quizWordDao.getNewQuizWords(title.getId(), title.getSection(), title.getParagraph());
		// do we have enough?
		int minWords = 4;
		if (quizWords.size() < minWords) {
			int count = minWords - quizWords.size();
			List<QuizWord> moreQuizWords = quizWordDao.getMoreQuizWords(title.getId(), title.getSection(), title.getParagraph(), count);
			quizWords.addAll(moreQuizWords);
		}
		quizWordDao.close();
		return quizWords;
	}
}
