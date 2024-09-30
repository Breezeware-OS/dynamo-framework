package net.breezeware.dynamo.notification.push.svc.enumeration;

/**
 * Message type.
 */
public enum MessageType {
    GCM("gcm"), APNS("apns"), APNS_SANDBOX("apns_sandbox"), WEB("web");

    public final String type;

    MessageType(String type) {
        this.type = type;
    }

    /**
     * Provides MessageType from given type value.
     * @param  type provides message type to fetch message type values.
     * @return      <code>MessageType</code> as response.
     */
    public static MessageType getType(String type) {
        for (MessageType messageType : values()) {
            if (messageType.type.equals(type)) {
                return messageType;
            }

        }

        return null;
    }
}
