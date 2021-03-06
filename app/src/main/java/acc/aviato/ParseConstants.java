package acc.aviato;

/**
 * Created by Michael on 11/22/15.
 */
public final class ParseConstants {

    //class name in Parse
    public static final String CLASS_EVENTS = "Event";
    public static final String CLASS_TAGS = "Tag";

    //field names - match what is already in parse and established from iOS
    public static final String KEY_EVENT_ID = "objectId";   // Note: use getObjectId() to get ID of a specific object
    public static final String KEY_EVENT_NAME = "eventName";
    public static final String KEY_EVENT_TAG = "eventTags";
    public static final String KEY_EVENT_VOTES = "votes";
    public static final String KEY_EVENT_DESCRIPTION = "eventDescription";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_FRIENDS_RELATION = "friendsRelation";
    public static final String KEY_FILE = "file";
    public static final String KEY_EVENT_IMAGE = "eventImage";
    public static final String KEY_FAVORITE_EVENTS_REALATION = "favoriteEvents";
    public static final String KEY_EVENT_TAG_ID = "eventTagIds";

    public static final String KEY_TAG_ID = "tagId";
    public static final String KEY_TAG_NAME = "tagName";
    public static final String KEY_TAG_USAGE = "tagUsageCount";

    public static final String KEY_EVENT_LOCATION = "eventLocation";
    public static final String KEY_EVENT_DATE = "eventDate";
    public static final String KEY_EVENT_DATE_TIME = "eventDateTime";

}
