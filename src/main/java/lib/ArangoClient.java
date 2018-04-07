package lib;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;

public class ArangoClient {
    private static final String DB_NAME = "messages_service";
    private static final ArangoDB.Builder arangoDBBuilder = new ArangoDB.Builder();

    public static ArangoCollection createOrRetrieveCollection(String collectionName) {
        ArangoDatabase db = createOrRetrieveDB();

        try {
            db.createCollection(collectionName);
            System.out.println("Collection created: " + collectionName);
        } catch (ArangoDBException e) {
            if (e.getErrorNum() == 1207) { // 1207 -> Duplicate name error no.
                System.out.println("Collection is already created");
            } else {
                System.err.println("Failed to create collection: " + collectionName + "; " + e.getMessage());
            }
        } finally {
            ArangoCollection collection = db.collection(collectionName);
            System.out.println("Collection retrieved: " + collectionName);
            return collection;
        }
    }

    public static ArangoDatabase createOrRetrieveDB() {
        ArangoDB arangoDB = arangoDBConnection();

        try {
            arangoDB.createDatabase(DB_NAME);
            System.out.println("Database created: " + DB_NAME);
        } catch (ArangoDBException e) {
            if (e.getErrorNum() == 1207) { // 1207 -> Duplicate name error no.
                System.out.println("Database is already created");
            } else {
                System.err.println("Failed to create database: " + DB_NAME + "; " + e.getMessage());
            }
        } finally {
            ArangoDatabase db = arangoDB.db(DB_NAME);
            System.out.println("Database retrieved: " + DB_NAME);
            return db;
        }
    }

    private static ArangoDB arangoDBConnection() {
        // here you can set all the specific configuration
        arangoDBBuilder.password("blabla");
        return arangoDBBuilder.build();
    }
}
