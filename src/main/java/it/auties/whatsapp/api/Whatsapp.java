package it.auties.whatsapp.api;

import it.auties.whatsapp.binary.BinaryArray;
import it.auties.whatsapp.compiler.RegisterListenerScanner;
import it.auties.whatsapp.crypto.SignalHelper;
import it.auties.whatsapp.exchange.GroupResponse;
import it.auties.whatsapp.exchange.HasWhatsappResponse;
import it.auties.whatsapp.exchange.Node;
import it.auties.whatsapp.exchange.StatusResponse;
import it.auties.whatsapp.manager.WhatsappKeys;
import it.auties.whatsapp.manager.WhatsappStore;
import it.auties.whatsapp.protobuf.chat.Chat;
import it.auties.whatsapp.protobuf.chat.GroupAction;
import it.auties.whatsapp.protobuf.chat.GroupPolicy;
import it.auties.whatsapp.protobuf.chat.GroupSetting;
import it.auties.whatsapp.protobuf.contact.Contact;
import it.auties.whatsapp.protobuf.contact.ContactJid;
import it.auties.whatsapp.protobuf.contact.ContactStatus;
import it.auties.whatsapp.protobuf.info.ContextInfo;
import it.auties.whatsapp.protobuf.info.MessageInfo;
import it.auties.whatsapp.protobuf.message.model.ContextualMessage;
import it.auties.whatsapp.protobuf.message.model.Message;
import it.auties.whatsapp.socket.WhatsappSocket;
import it.auties.whatsapp.util.Nodes;
import it.auties.whatsapp.util.Validate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static it.auties.whatsapp.exchange.Node.with;
import static it.auties.whatsapp.exchange.Node.withChildren;
import static it.auties.whatsapp.manager.WhatsappKeys.knownIds;
import static java.util.Map.of;
import static java.util.Objects.requireNonNullElseGet;

/**
 * A class used to interface a user to WhatsappWeb's WebSocket.
 * It provides various functionalities, including the possibility to query, set and modify data associated with the loaded session of whatsapp.
 * It can be configured using a default configuration or a custom one.
 * Multiple instances of this class can be initialized, though it is not advisable as; is a singleton and cannot distinguish between the data associated with each session.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Whatsapp {
    private WhatsappSocket socket;

    public Whatsapp(int id){
        this(new WhatsappSocket(WhatsappOptions.defaultOptions(), new WhatsappStore(), WhatsappKeys.fromMemory(id)));
        RegisterListenerScanner.scan(this)
                .forEach(this::registerListener);
    }

    public Whatsapp(){
        this(requireNonNullElseGet(knownIds().peekFirst(), SignalHelper::randomRegistrationId));
    }

    /**
     * Returns the store associated with this session
     *
     * @return a non-null WhatsappStore
     */
    public WhatsappStore store(){
        return socket.store();
    }

    /**
     * Returns the keys associated with this session
     *
     * @return a non-null WhatsappKeys
     */
    public WhatsappKeys keys(){
        return socket.keys();
    }

    /**
     * Registers a listener manually
     *
     * @param listener the listener to register
     * @return the same instance
     * @throws IllegalArgumentException if the {@code listener} cannot be added
     */
    public Whatsapp registerListener(@NonNull WhatsappListener listener) {
        Validate.isTrue(socket.store().listeners().add(listener),
                "WhatsappAPI: Cannot add listener %s", listener.getClass().getName());
        return this;
    }

    /**
     * Removes a listener manually
     *
     * @param listener the listener to remove
     * @return the same instance
     * @throws IllegalArgumentException if the {@code listener} cannot be added
     */
    public Whatsapp removeListener(@NonNull WhatsappListener listener) {
        Validate.isTrue(socket.store().listeners().remove(listener),
                "WhatsappAPI: Cannot remove listener %s", listener.getClass().getName());
        return this;
    }

    /**
     * Opens a connection with Whatsapp Web's WebSocket if a previous connection doesn't exist
     *
     * @return the same instance
     */
    public Whatsapp connect() {
        socket.connect();
        return this;
    }

    /**
     * Disconnects from Whatsapp Web's WebSocket if a previous connection exists
     *
     * @return the same instance
     */
    public Whatsapp disconnect() {
        socket.disconnect();
        return this;
    }

    /**
     * Disconnects from Whatsapp Web's WebSocket and logs out of WhatsappWeb invalidating the previous saved credentials
     * The next endTimeStamp the API is used, the QR code will need to be scanned again
     *
     * @return the same instance
     */
    public Whatsapp logout() {
        if(keys().hasCompanion()) {
            var metadata = of("jid", keys().companion(), "reason", "user_initiated");
            var device = with("remove-companion-device", metadata, null);
            socket.sendQuery("set", "md", device);
        }

        return this;
    }

    /**
     * Disconnects and reconnects to Whatsapp Web's WebSocket if a previous connection exists
     *
     * @return the same instance
     */
    public Whatsapp reconnect() {
        socket.reconnect();
        return this;
    }

    /**
     * Sends a request to Whatsapp in order to receive updates when the status of a contact changes.
     * These changes include the last known presence and the endTimeStamp the contact was last seen.
     * To listen to these updates implement;.
     *
     * @param contact the contact whose status the api should receive updates on
     * @return a CompletableFuture    
     */
    public CompletableFuture<?> subscribeToContactPresence(@NonNull Contact contact) {
        var metadata = of("to", contact.jid(), "type", "subscribe");
        return socket.sendWithNoResponse(with("presence", metadata, null));
    }

    /**
     * Builds and sends a message from a chat and a message
     *
     * @param chat    the chat where the message should be sent
     * @param message the message to send
     * @return a CompletableFuture 
     */
    public CompletableFuture<?> sendMessage(@NonNull Chat chat, @NonNull String message) {
        return null;
    }

    /**
     * Builds and sends a message from a chat, a message and a quoted message
     *
     * @param chat          the chat where the message should be sent
     * @param message       the message to send
     * @param quotedMessage the message that; should quote
     * @return a CompletableFuture 
     */
    public CompletableFuture<?> sendMessage(@NonNull Chat chat, @NonNull String message, @NonNull MessageInfo quotedMessage) {
        return null;
    }

    /**
     * Builds and sends a message from a chat and a message
     *
     * @param chat    the chat where the message should be sent
     * @param message the message to send
     * @return a CompletableFuture 
     */
    public CompletableFuture<?> sendMessage(@NonNull Chat chat, @NonNull Message message) {
        return null;
    }

    /**
     * Builds and sends a message from a chat, a message and a quoted message
     *
     * @param chat          the chat where the message should be sent
     * @param message       the message to send
     * @param quotedMessage the message that; should quote
     * @return a CompletableFuture 
     */
    public CompletableFuture<?> sendMessage(@NonNull Chat chat, @NonNull ContextualMessage message, @NonNull MessageInfo quotedMessage) {
        return null;
    }

    /**
     * Builds and sends a message from a chat, a message and a context
     *
     * @param chat        the chat where the message should be sent
     * @param message     the message to send
     * @param contextInfo the context of the message to send
     * @return a CompletableFuture 
     */
    public CompletableFuture<?> sendMessage(@NonNull Chat chat, @NonNull ContextualMessage message, @NonNull ContextInfo contextInfo) {
        return null;
    }

    /**
     * Sends a message info to a chat
     *
     * @param message the message to send
     * @return a CompletableFuture 
     */
    public CompletableFuture<?> sendMessage(@NonNull MessageInfo message) {
        return null;
    }

    /**
     * Executes a query to determine whether any number of jids have an account on Whatsapp
     *
     * @param jids the contacts to check
     * @return a CompletableFuture that wraps a non-null list of HasWhatsappResponse
     */
    public CompletableFuture<List<HasWhatsappResponse>> hasWhatsapp(@NonNull ContactJid... jids) {
        var contactNodes = Arrays.stream(jids)
                .map(jid -> with("contact", "+%s".formatted(jid.user())))
                .toArray(Node[]::new);
        return socket.sendQuery(with("contact"), withChildren("user", contactNodes))
                .thenApplyAsync(nodes -> nodes.stream().map(HasWhatsappResponse::new).toList());
    }

    /**
     * Queries the block list
     *
     * @return a CompletableFuture that wraps a non-null list of ContactId
     */
    public CompletableFuture<List<ContactJid>> queryBlockList() {
        return socket.sendQuery("get", "blocklist", (Node) null)
                .thenApplyAsync(this::parseBlockList);
    }

    private List<ContactJid> parseBlockList(Node result) {
        return result.findNode("list")
                .findNodes("item")
                .stream()
                .map(item -> item.attributes().getJid("jid").orElseThrow())
                .toList();
    }

    /**
     * Queries the written whatsapp status of a Contact
     *
     * @param contact the target contact
     * @return a CompletableFuture that wraps a non-null list of StatusResponse
     */
    public CompletableFuture<List<StatusResponse>> queryUserStatus(@NonNull Contact contact) {
        var query = with("status");
        var body = with("user", of("jid", contact.jid()), null);
        return socket.sendQuery(query, body)
                .thenApplyAsync(response -> Nodes.findAll(response, "status"))
                .thenApplyAsync(nodes -> nodes.stream().map(StatusResponse::new).toList());
    }

    /**
     * Queries the profile picture of a chat.
     *
     * @param chat the chat to query
     * @return a CompletableFuture that wraps nullable jpg url hosted on Whatsapp's servers
     */
    public CompletableFuture<String> queryChatPicture(@NonNull Chat chat) {
        return queryChatPicture(chat.jid());
    }

    /**
     * Queries the profile picture of a chat.
     *
     * @param contact the contact to query
     * @return a CompletableFuture that wraps nullable jpg url hosted on Whatsapp's servers
     */
    public CompletableFuture<String> queryChatPicture(@NonNull Contact contact) {
        return queryChatPicture(contact.jid());
    }

    /**
     * Queries the profile picture of a chat.
     *
     * @param jid the jid of the chat to query
     * @return a CompletableFuture that wraps nullable jpg url hosted on Whatsapp's servers
     */
    public CompletableFuture<String> queryChatPicture(@NonNull ContactJid jid) {
        var body = Node.with("picture", of("query", "url"), null);
        return socket.sendQuery("get", "w:profile:picture", of("target", jid), body)
                .thenApplyAsync(this::parseChatPicture);
    }

    private String parseChatPicture(Node result) {
        return Optional.ofNullable(result.findNode("picture"))
                .map(picture -> picture.attributes().getString("url", null))
                .orElse(null);
    }

    /**
     * Queries the metadata of a group
     *
     * @param chat the target group
     * @return a CompletableFuture     
     * @throws IllegalArgumentException if the provided chat is not a group
     */
    public CompletableFuture<GroupResponse> queryGroupMetadata(@NonNull Chat chat) {
        var query = with("query", of("request", "interactive"), null);
        return socket.sendQuery(chat.jid(), "get", "w:g2", query)
                .thenApplyAsync(result -> result.findNode("group"))
                .thenApplyAsync(GroupResponse::new);
    }

    /**
     * Queries the invite code of a group
     *
     * @param chat the target group
     * @return a CompletableFuture     
     * @throws IllegalArgumentException if the provided chat is not a group
     */
    public CompletableFuture<String> queryGroupInviteCode(@NonNull Chat chat) {
        return socket.sendQuery(chat.jid(), "get", "w:g2", with("invite"))
                .thenApplyAsync(result -> result.findNode("invite").attributes().getString("code"));
    }

    /**
     * Queries the groups in common with a contact
     *
     * @param contact the target contact
     * @return a CompletableFuture 
     */
    public CompletableFuture<?> queryGroupsInCommon(@NonNull Contact contact) {
        // Unknown
        return null;
    }

    /**
     * Queries a specified amount of starred/favourite messages in a chat, including ones not in memory
     *
     * @param chat  the target chat
     * @param count the amount of messages
     * @return a CompletableFuture 
     */
    public CompletableFuture<?> queryFavouriteMessagesInChat(@NonNull Chat chat, int count) {
        // Unknown
        return null;
    }

    /**
     * Changes your presence for everyone on Whatsapp
     *
     * @param presence the new status
     * @return a CompletableFuture 
     */
    @Unsupported
    public CompletableFuture<?> changePresence(@NonNull ContactStatus presence) {
        return socket.sendWithNoResponse(with("presence", of("type", presence.data()), null));
    }

    /**
     * Changes your presence for a specific chat
     *
     * @param chat     the target chat
     * @param presence the new status
     * @return a CompletableFuture 
     */
    public CompletableFuture<?> changePresence(@NonNull Chat chat, @NonNull ContactStatus presence) {
        return socket.sendWithNoResponse(with("presence", of("to", chat.jid(), "type", presence.data()), null));
    }

    /**
     * Promotes any number of contacts to admin in a group
     *
     * @param group    the target group
     * @param contacts the target contacts
     * @return a CompletableFuture     
     * @throws IllegalArgumentException if the provided chat is not a group
     */
    public CompletableFuture<?> promote(@NonNull Chat group, @NonNull Contact... contacts) {
        return null;
    }

    /**
     * Demotes any number of contacts to admin in a group
     *
     * @param group    the target group
     * @param contacts the target contacts
     * @return a CompletableFuture     
     * @throws IllegalArgumentException if the provided chat is not a group
     */
    public CompletableFuture<?> demote(@NonNull Chat group, @NonNull Contact... contacts) {
        return null;
    }

    /**
     * Adds any number of contacts to a group
     *
     * @param group    the target group
     * @param contacts the target contact/s
     * @return a CompletableFuture     
     * @throws IllegalArgumentException if the provided chat is not a group
     */
    public CompletableFuture<?> add(@NonNull Chat group, @NonNull Contact... contacts) {
        return null;
    }

    /**
     * Removes any number of contacts from group
     *
     * @param group    the target group
     * @param contacts the target contact/s
     * @return a CompletableFuture     
     * @throws IllegalArgumentException if the provided chat is not a group
     */
    public CompletableFuture<?> remove(@NonNull Chat group, @NonNull Contact... contacts) {
        return null;
    }

    /**
     * Executes an action on any number of contacts represented by a raw list of WhatsappNodes
     *
     * @param group  the target group
     * @param action the action to execute
     * @param jids   the raw WhatsappNodes representing the contacts the action should be executed on
     * @return a CompletableFuture     
     * @throws IllegalArgumentException if the provided chat is not a group
     * @throws IllegalArgumentException if no jids are provided
     */
    public CompletableFuture<?> executeActionOnGroupParticipant(@NonNull Chat group, @NonNull GroupAction action, @NonNull List<Node> jids) {
        return null;
    }

    /**
     * Changes the name of a group
     *
     * @param group   the target group
     * @param newName the new name for the group
     * @return a CompletableFuture     
     * @throws IllegalArgumentException if the provided chat is not a group
     * @throws IllegalArgumentException if the provided new name is empty or blank
     */
    public CompletableFuture<?> changeGroupName(@NonNull Chat group, @NonNull String newName) {
        return null;
    }

    /**
     * Changes the description of a group
     *
     * @param group          the target group
     * @param newDescription the new name for the group
     * @return a CompletableFuture     
     * @throws IllegalArgumentException if the provided chat is not a group
     */
    public CompletableFuture<?> changeGroupDescription(@NonNull Chat group, @NonNull String newDescription) {
        return null;
    }

    /**
     * Changes which category of users can send messages in a group
     *
     * @param group  the target group
     * @param policy the new policy to enforce
     * @return a CompletableFuture     
     * @throws IllegalArgumentException if the provided chat is not a group
     */
    public CompletableFuture<?> changeWhoCanSendMessagesInGroup(@NonNull Chat group, @NonNull GroupPolicy policy) {
        return null;
    }

    /**
     * Changes which category of users can edit the group's settings
     *
     * @param group  the target group
     * @param policy the new policy to enforce
     * @return a CompletableFuture     
     * @throws IllegalArgumentException if the provided chat is not a group
     */
    public CompletableFuture<?> changeWhoCanEditGroupInfo(@NonNull Chat group, @NonNull GroupPolicy policy) {
        return null;
    }

    /**
     * Enforces a new policy for a setting in a group
     *
     * @param group   the target group
     * @param setting the target setting
     * @param policy  the new policy to enforce
     * @return a CompletableFuture     
     * @throws IllegalArgumentException if the provided chat is not a group
     */
    public CompletableFuture<?> changeGroupSetting(@NonNull Chat group, @NonNull GroupSetting setting, @NonNull GroupPolicy policy) {
        return null;
    }

    /**
     * Changes the picture of a group
     * This is still in beta
     *
     * @param group the target group
     * @param image the new image
     * @return a CompletableFuture     
     * @throws IllegalArgumentException if the provided chat is not a group
     */
    public CompletableFuture<?> changeGroupPicture(@NonNull Chat group, byte @NonNull [] image) {
        return null;
    }

    /**
     * Removes the picture of a group
     *
     * @param group the target group
     * @return a CompletableFuture     
     * @throws IllegalArgumentException if the provided chat is not a group
     */
    public CompletableFuture<?> removeGroupPicture(@NonNull Chat group) {
        return null;
    }

    /**
     * Leaves a group
     *
     * @param group the target group
     * @throws IllegalArgumentException if the provided chat is not a group
     */
    public CompletableFuture<?> leave(@NonNull Chat group) {
        return null;
    }

    /**
     * Mutes a chat indefinitely
     *
     * @param chat the target chat
     * @return a CompletableFuture 
     */
    public CompletableFuture<?> mute(@NonNull Chat chat) {
        return null;
    }

    /**
     * Mutes a chat until a specific date
     *
     * @param chat  the target chat
     * @param until the date the mute ends
     * @return a CompletableFuture 
     */
    public CompletableFuture<?> mute(@NonNull Chat chat, @NonNull ZonedDateTime until) {
        return null;
    }

    /**
     * Mutes a chat until a specific date expressed in seconds since the epoch
     *
     * @param chat           the target chat
     * @param untilInSeconds the date the mute ends expressed in seconds since the epoch
     * @return a CompletableFuture 
     */
    public CompletableFuture<?> mute(@NonNull Chat chat, long untilInSeconds) {
        return null;
    }

    /**
     * Unmutes a chat
     *
     * @param chat the target chat
     * @return a CompletableFuture 
     */
    public CompletableFuture<?> unmute(@NonNull Chat chat) {
        return null;
    }

    /**
     * Blocks a contact
     *
     * @param contact the target contact
     * @return a CompletableFuture 
     */
    public CompletableFuture<?> block(@NonNull Contact contact) {
        return null;
    }

    /**
     * Enables ephemeral messages in a chat, this means that messages will be automatically cancelled in said chat after a week
     *
     * @param chat the target chat
     * @return a CompletableFuture 
     */
    public CompletableFuture<?> enableEphemeralMessages(@NonNull Chat chat) {
        return null;
    }

    /**
     * Disables ephemeral messages in a chat, this means that messages sent in said chat will never be cancelled
     *
     * @param chat the target chat
     * @return a CompletableFuture 
     */
    public CompletableFuture<?> disableEphemeralMessages(@NonNull Chat chat) {
        return null;
    }

    /**
     * Changes the ephemeral status of a chat, this means that messages will be automatically cancelled in said chat after the provided endTimeStamp
     *
     * @param chat the target chat
     * @param time the endTimeStamp to live for a message expressed in seconds
     * @return a CompletableFuture 
     */
    public CompletableFuture<?> changeEphemeralStatus(@NonNull Chat chat, int time) {
        return null;
    }

    /**
     * Marks a chat as unread
     *
     * @param chat the target chat
     * @return a CompletableFuture 
     */
    public CompletableFuture<?> markAsUnread(@NonNull Chat chat) {
        return null;
    }

    /**
     * Marks a chat as read
     *
     * @param chat the target chat
     * @return a CompletableFuture 
     */
    public CompletableFuture<?> markAsRead(@NonNull Chat chat) {
        return null;
    }

    /**
     * Marks a chat with a flag represented by an integer.
     * If this chat has no history, an attempt to load the chat's history is made.
     * If no messages can be found after said attempt, the request will fail automatically.
     * If the request is successful, sets the number of unread messages to;.
     *
     * @param chat    the target chat
     * @param flag    the flag represented by an int
     * @param newFlag the new flag represented by an int
     * @return a CompletableFuture 
     */
    public CompletableFuture<?> markChat(@NonNull Chat chat, int flag, int newFlag) {
        return null;
    }

    /**
     * Marks a chat with a flag represented by an integer.
     * If the request is successful, sets the number of unread messages to;.
     *
     * @param chat        the target chat
     * @param lastMessage the real last message in this chat
     * @param flag        the flag represented by an int
     * @param newFlag     the new flag represented by an int
     * @return a CompletableFuture 
     */
    public CompletableFuture<?> markChat(@NonNull Chat chat, @NonNull MessageInfo lastMessage, int flag, int newFlag) {
        return null;
    }

    /**
     * Pins a chat to the top.
     * A maximum of three chats can be pinned to the top.
     * This condition can be checked using;.
     *
     * @param chat the target chat
     * @return a CompletableFuture 
     */
    public CompletableFuture<?> pin(@NonNull Chat chat) {
        return null;
    }

    /**
     * Unpins a chat from the top
     *
     * @param chat the target chat
     * @return a CompletableFuture 
     */
    public CompletableFuture<?> unpin(@NonNull Chat chat) {
        return null;
    }

    /**
     * Archives a chat.
     * If said chat is pinned, it will be unpinned.
     *
     * @param chat the target chat
     * @return a CompletableFuture 
     */
    public CompletableFuture<?> archive(@NonNull Chat chat) {
        return null;
    }

    /**
     * Unarchives a chat
     *
     * @param chat the target chat
     * @return a CompletableFuture 
     */
    public CompletableFuture<?> unarchive(@NonNull Chat chat) {
        return null;
    }

    /**
     * Creates a new group with the provided name and with at least one contact
     *
     * @param subject  the new group's name
     * @param contacts at least one contact to add to the group
     * @return a CompletableFuture
     */
    public CompletableFuture<GroupResponse> createGroup(@NonNull String subject, @NonNull Contact... contacts) {
        var participants = Arrays.stream(contacts)
                .map(contact -> with("participant", of("jid", contact.jid()), null))
                .toArray(Node[]::new);
        var body = withChildren("create", of("subject", subject, "key", BinaryArray.random(12).toHex()), participants);
        return socket.sendQuery(ContactJid.ofServer(ContactJid.Server.GROUP), "set", "w:g2", body)
                .thenApplyAsync(response -> response.findNode("group"))
                .thenApplyAsync(GroupResponse::new);
    }

    private void starMessagePlaceholder() {
        // Sent Binary Message Node[description=iq, attributes={xmlns=w:sync:app:state, to=s.whatsapp.net, id=54595.12796-297, type=set}, content=[Node[description=sync, attributes={}, content=[Node[description=collection, attributes={name=regular_high, return_snapshot=false, version=13}, content=[Node[description=patch, attributes={}, content=[B@1cd3e518]]]]]]]
        // Received Binary Message Node[description=iq, attributes={from=s.whatsapp.net, id=54595.12796-297, type=result}, content=[Node[description=sync, attributes={}, content=[Node[description=collection, attributes={name=regular_high, version=14}, content=null]]]]]
    }

    private void unstarMessagePlaceholder() {
        // Sent Binary Message Node[description=iq, attributes={xmlns=w:sync:app:state, to=s.whatsapp.net, id=54595.12796-301, type=set}, content=[Node[description=sync, attributes={}, content=[Node[description=collection, attributes={name=regular_high, return_snapshot=false, version=14}, content=[Node[description=patch, attributes={}, content=[B@73ce9a0b]]]]]]]
        // Received Binary Message Node[description=iq, attributes={from=s.whatsapp.net, id=54595.12796-301, type=result}, content=[Node[description=sync, attributes={}, content=[Node[description=collection, attributes={name=regular_high, version=15}, content=null]]]]]
    }

    private void createMediaConnectionPlaceholder(){
        // Sent Binary Message Node[description=iq, attributes={xmlns=w:m, to=s.whatsapp.net, id=54595.12796-319, type=set}, content=[Node[description=media_conn, attributes={}, content=null]]]
        //Received Binary Message Node[description=iq, attributes={from=s.whatsapp.net, id=54595.12796-319, type=result}, content=[Node[description=media_conn, attributes={auth=AWQkTwWMKc8evmNls99lMA2th8gPHKz474hwoYbOXBhVv3KcKKE_aCC1mew, auth_ttl=21600, max_buckets=12, ttl=300}, content=[Node[description=host, attributes={hostname=media-mxp2-1.cdn.whatsapp.net, fallback_class=pop, fallback_hostname=media-fco2-1.cdn.whatsapp.net, fallback_ip6=2a03:2880:f269:c1:face:b00c:0:167, fallback_ip4=157.240.231.60, type=primary, ip4=157.240.203.60, class=pop, ip6=2a03:2880:f26d:c2:face:b00c:0:167}, content=[Node[description=download, attributes={}, content=null]]], Node[description=host, attributes={hostname=media.fmxp9-1.fna.whatsapp.net, fallback_class=pop, fallback_hostname=media-mxp1-1.cdn.whatsapp.net, fallback_ip6=2a03:2880:f208:c5:face:b00c:0:167, fallback_ip4=31.13.86.51, type=primary, ip4=91.81.217.226, class=fna, ip6=2a01:8d0:7:15:face:b00c:3333:7020}, content=[Node[description=upload, attributes={}, content=null], Node[description=download, attributes={}, content=[Node[description=video, attributes={}, content=null], Node[description=image, attributes={}, content=null], Node[description=gif, attributes={}, content=null], Node[description=sticker, attributes={}, content=null]]], Node[description=download_buckets, attributes={}, content=[Node[description=0, attributes={}, content=null]]]]], Node[description=host, attributes={hostname=mmg.whatsapp.net, type=fallback, class=pop}, content=null]]]]]
    }
}
