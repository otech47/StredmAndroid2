package com.setmine.android;

import java.util.List;

public interface OnTaskCompleted<T> {
	void startTask();

	void cancelTask();

	void onTaskCompleted(List<T> list);

	void onTaskFailed();
}