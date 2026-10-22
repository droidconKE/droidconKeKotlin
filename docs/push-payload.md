# Push payload contract

For the backend team. The app reads one field of an FCM **data** message, `payload`, whose value
is a JSON string. `type` names the message; the other fields depend on it. Send data messages, not
notification messages: Firebase shows a notification message itself while the app is in the
background, skipping the user's settings and the app's channels.

```json
{
  "to": "/topics/all_attendees",
  "data": {
    "payload": "{\"type\":\"announcement\",\"title\":\"Lunch is served\",\"body\":\"Hall B\"}"
  }
}
```

Fields the app does not know are ignored, so the backend can add them. A `type` the app does not
know, or a payload that is not valid JSON, is dropped.

| `type` | Fields | Shown on channel | Tap opens |
|---|---|---|---|
| `schedule_change` | `sessionId`, `changeType` (`RoomChanged`, `TimeChanged`, `Cancelled`, `SpeakerChanged`), `message` | Schedule changes | `droidconke://session/<sessionId>` |
| `announcement` | `title`, `body`, `deepLink` (optional, one of the links below) | Announcements | `deepLink`, else the app |
| `feed_post` | `postId`, `preview` | Feed | `droidconke://feed` |
| `milestone` | `title`, `body`, `icon` (optional) | Milestones | the app |
| `sync_request` | none | none | Silent. Starts a fresh sync and posts nothing |

A `schedule_change` also starts a sync whether or not the user sees it, so the reminders of a moved
session move too. The older format, a data message with `title` and `message` keys and no
`payload`, is still read as an announcement while the backend moves over.

A push is dropped when the user has switched its channel off in the app's notification settings,
or when Android has the app's notifications off. `sync_request` is never dropped for either reason.
Each channel is independent: there is no master switch, so a user who mutes everything else can
still receive `schedule_change`.

## Deep links

| Link | Opens |
|---|---|
| `droidconke://session/<id>` | The session. An unknown id shows the "not found" state |
| `droidconke://feed` | The feed |

The source of truth is `PushPayload` in `core:notifications`.
