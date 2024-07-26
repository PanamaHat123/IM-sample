enum MessageCommand {

    MSG_P2P = 0x44F,

    MSG_P2P_SYNC = 0x454,

    //Message read sent   1106
    MSG_READED = 0x452,

    //Message received ack
    MSG_RECIVE_ACK = 1107,

    //P2p ACK 1046
    MSG_ACK = 0x416,

    // Message recalled 1050
    MSG_RECALL = 0x41A,

    //Message withdrawal notification. 1052
    MSG_RECALL_NOTIFY = 0x41C,

    // Message recall acknowledgment packet. 1051
    MSG_RECALL_ACK = 0x41B,

    // Message read notification. 1053
    MSG_READED_NOTIFY = 0x41D,

}

enum FriendShipCommand{
    //Add friend. 3000
    FRIEND_ADD = 0xbb8,

    //Update friend. 3001
    FRIEND_UPDATE = 0xbb9,

    //Delete friend. 3002
    FRIEND_DELETE = 0xbba,

    //Friend request. 3003
    FRIEND_REQUEST = 0xbbb,

    //Friend request read 3004
    FRIEND_REQUEST_READ = 0xbbc,

    //Friend request approval. 3005
    FRIEND_REQUEST_APPROVER = 0xbbd,

    //Add to blacklist. 3010
    FRIEND_BLACK_ADD = 0xbc2,

    //Remove from blacklist. 3011
    FRIEND_BLACK_DELETE = 0xbc3,

    //Create new friend group. 3012
    FRIEND_GROUP_ADD = 0xbc4,

    //Delete friend group. 3013
    FRIEND_GROUP_DELETE = 0xbc5,

    //Add members to friend group. 3014
    FRIEND_GROUP_MEMBER_ADD = 0xbc6,

    //Remove members from friend group. 3015
    FRIEND_GROUP_MEMBER_DELETE = 0xbc7,

}

enum GroupCommand{
    /**
     * Push group admission application notification. 2000
     */
    JOIN_GROUP = 0x7d0,

    /**
     * Push add group member notification. 2001，Notify all administrators and the individual concerned.
     */
    ADDED_MEMBER = 0x7d1,

    /**
     * Push create group notification. 2002，notify all
     */
    CREATED_GROUP = 0x7d2,

    /**
     * Push update group notification. 2003，all
     */
    UPDATED_GROUP = 0x7d3,

    /**
     *  2004
     */
    EXIT_GROUP = 0x7d4,

    /**
     *  2005
     */
    UPDATED_MEMBER = 0x7d5,

    /**
     *  2006，all
     */
    DELETED_MEMBER = 0x7d6,

    /**
     *  2007
     */
    DESTROY_GROUP = 0x7d7,

    /**
     * Push transfer group ownership. 2008
     */
    TRANSFER_GROUP = 0x7d8,

    /**
     * 2009，all
     */
    MUTE_GROUP = 0x7d9,

    /**
     * 2010 Mute/Unmute group member, notify the administrators and the person being operated on.
     */
    SPEAK_GOUP_MEMBER = 0x7da,

    //Group chat message sending and receiving.   2104
    MSG_GROUP = 0x838,

    //Group chat message send and receive synchronization message.   2105
    MSG_GROUP_SYNC = 0x839,

    // 2047
    GROUP_MSG_ACK = 0x7ff,
}

enum SystemCommand{

    //Heartbeat 9999
    PING = 0x270f,

    // 9000
    LOGIN = 0x2328,

    //login ack  9001
    LOGINACK = 0x2329,

    //Offline notification for multi-end mutual exclusion.  9002
    MUTUALLOGIN = 0x232a,

    //  9003
    LOGOUT = 0x232b,
}

enum UserEventCommand{
    //4000
    USER_MODIFY = 0xfa0,

    //4001
    USER_ONLINE_STATUS_CHANGE = 0xfa1,

    //4002 Online status subscription.
    USER_ONLINE_STATUS_SUBSCRIBE = 0xfa2,

    //4003 Fetch the online status of subscribed friends, send only to the requesting client.
    PULL_USER_ONLINE_STATUS = 0xfa3,

    //4004 User online status notification message.
    USER_ONLINE_STATUS_CHANGE_NOTIFY = 0xfa4,
}

enum ConversationEventCommand{
    //5000 Conversation deletion.
    CONVERSATION_DELETE = 0x1388,
    //5001 Conversation modification
    CONVERSATION_UPDATE = 0x1389,
}

export {
    MessageCommand,
    FriendShipCommand,
    GroupCommand,
    SystemCommand,
    UserEventCommand,
    ConversationEventCommand
};