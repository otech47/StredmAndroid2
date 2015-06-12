package com.setmine.android;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.TaskStackBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParseDeepLinkActivity extends FragmentActivity {
    public static final String PREFERENCE_DEEP_LINK = "/home/preferences";
    public static final String INBOX_DEEP_LINK = "/inbox/messages";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent == null || intent.getData() == null) {
            finish();
        }

        openDeepLink(intent.getData());

        // Finish this activity
        finish();
    }

    private void openDeepLink(Uri deepLink) {
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Treat each path segment of the URI as a deep link
        List<String> segments = deepLink.getPathSegments();

        // Create a task stack from the deep links
        if (segments != null) {
            for (String segment : segments) {
                Intent route = null;
                if ("preferences".equals(segment)) {
                    route = new Intent(getApplicationContext(), PushPreferencesActivity.class);
                } else if ("inbox".equals(segment)) {
                    route = new Intent(getApplicationContext(), InboxActivity.class);
                } else if ("home".equals(segment)) {
                    route = new Intent(getApplicationContext(), MainActivity.class);
                }

                if (route != null) {
                    stackBuilder.addNextIntentWithParentStack(route);
                }
            }
        }


        /android/play/

        // Fall back to the main activity
        if (stackBuilder.getIntentCount() == 0) {
            stackBuilder.addNextIntent(new Intent(this, MainActivity.class));
        }

//        Before launching the activities in the task stack, set the extras on the last intent:

        Bundle extras = parseOptions(deepLink);
        if (extras != null) {
            // Add the extras to the last intent
            stackBuilder.editIntentAt(stackBuilder.getIntentCount() - 1).putExtras(extras);
        }


        // Launch the activities
        stackBuilder.startActivities();
    }

    private Bundle parseOptions(Uri deepLink) {
        Bundle options = new Bundle();
        Map<String, List<String>> queryParameters = UriUtils.getQueryParameters(deepLink);

        if (queryParameters == null) {
            return options;
        }

        for (String key : queryParameters.keySet()) {
            List<String> values = queryParameters.get(key);
            if (values.size() == 1) {
                options.putString(key, values.get(0));
            } else if (values.size() > 1) {
                options.putStringArrayList(key, new ArrayList<String>(values));
            }
        }

        return options;
    }

}