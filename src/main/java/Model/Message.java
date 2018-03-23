package Model;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.DocumentCreateEntity;
import com.arangodb.util.MapBuilder;
import lib.ArangoClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Message {
    private static final String COLLECTION_NAME = "messages";

    private static ArangoCollection collection  = null;

    public static HashMap<String, Object>  create(HashMap<String, Object> atrributes) {
        BaseDocument newMessage = new BaseDocument();

        for (String key : atrributes.keySet()) {
            newMessage.addAttribute(key, atrributes.get(key));
        }
        DocumentCreateEntity<BaseDocument> createdMessage = getCollection().insertDocument(newMessage);

        atrributes.put("id", createdMessage.getKey());

        return atrributes;
    }

    public static HashMap<String, Object>  update(String id, HashMap<String, Object> atrributes) {
        BaseDocument updatedMessage = getCollection().getDocument(id, BaseDocument.class);

        for (String key : atrributes.keySet()) {
            updatedMessage.addAttribute(key, atrributes.get(key));
        }

        getCollection().updateDocument(id, updatedMessage);

        atrributes.put("id", id);

        return atrributes;
    }

    public static boolean delete(String id) {
        try {
            getCollection().deleteDocument(id);
        } catch (ArangoDBException e) {
            if (e.getErrorNum() == 1202) {
                System.out.println("document not found");
            }
        }

        return true;
    }


    public static ArrayList<HashMap<String, Object>> getAll(String userId) {
        String query = "FOR message IN messages FILTER message.sender_id == @user_id OR message.receiver_id == @user_id RETURN message";
        Map<String, Object> bindVars = new MapBuilder().put("user_id", userId).get();
        ArangoCursor<BaseDocument> cursor = getCollection().db().query(query, bindVars, null,
                BaseDocument.class);

        ArrayList<HashMap<String, Object>> messages = new ArrayList<HashMap<String, Object>>();
        cursor.forEachRemaining((BaseDocument aDocument) -> {
            Map<String, Object> properties = aDocument.getProperties();
            HashMap<String, Object> message = new HashMap<String, Object>();
            for (String key :  properties.keySet()) {
                message.put(key, aDocument.getAttribute(key));
            }
            message.put("id", aDocument.getKey());
            messages.add(message);
        });

        return messages;
    }

    public static HashMap<String, Object> get(String messageId) {
        BaseDocument aDocument = getCollection().getDocument(messageId, BaseDocument.class);
        Map<String, Object> properties = aDocument.getProperties();
        HashMap<String, Object> message = new HashMap<String, Object>();
        for (String key :  properties.keySet()) {
            message.put(key, aDocument.getAttribute(key));
        }
        message.put("id", aDocument.getKey());

        return message;
    }

    private static ArangoCollection getCollection() {
        if (collection == null) {
            collection = ArangoClient.createOrRetrieveCollection(COLLECTION_NAME);
        }
        return collection;
    }
}
