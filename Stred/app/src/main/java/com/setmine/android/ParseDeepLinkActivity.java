package com.setmine.android;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.util.List;

public class ParseDeepLinkActivity extends FragmentActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("create", " parse started");

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
                if ("play".equals(segment)) {
                    route = new Intent(getApplicationContext(), SetMineMainActivity.class);
                    Log.d("abc", "true");

                    if (route != null) {
                        stackBuilder.addNextIntentWithParentStack(route);
                    }
                }
            }

            // Fall back to the main activity
            if (stackBuilder.getIntentCount() == 0) {
                stackBuilder.addNextIntent(new Intent(this, SetMineMainActivity.class));
            }

        // Before launching the activities in the task stack, set the extras on the last intent:

//            Bundle extras = parseOptions(deepLink);
//            if (extras != null) {
//                // Add the extras to the last intent
//                stackBuilder.editIntentAt(stackBuilder.getIntentCount() - 1).putExtras(extras);
//            }


            // Launch the activities
            stackBuilder.startActivities();
        }
    }

//    private Bundle parseOptions(Uri deepLink) {
//        Bundle options = new Bundle();
//        Map<String, List<String>> queryParameters = UriUtils.getQueryParameters(deepLink);
//
//        if (queryParameters == null) {
//            return options;
//        }
//
//        for (String key : queryParameters.keySet()) {
//            List<String> values = queryParameters.get(key);
//            if (values.size() == 1) {
//                options.putString(key, values.get(0));
//            } else if (values.size() > 1) {
//                options.putStringArrayList(key, new ArrayList<String>(values));
//            }
//        }
//
//        return options;
//    }

}