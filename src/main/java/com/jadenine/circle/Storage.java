package com.jadenine.circle;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

/**
 * Created by linym on 6/2/15.
 */
public class Storage {
    public static final String PARTITION_KEY = "PartitionKey";
    public static final String ROW_KEY = "RowKey";
    public static final String TIMESTAMP = "Timestamp";


    private static final String TABLE_USER = "user";
    private static final String TABLE_USER_AP = "userap";
    private static final String TABLE_MESSAGE = "message";

    // Define the connection-string with your values.
    public static final String storageConnectionString =
            "DefaultEndpointsProtocol=http;" +
                    "TableEndpoint=http://circlestorage.table.core.chinacloudapi.cn/;" +
                    "AccountName=circlestorage;" +
                    "AccountKey=SPa+eHBBeJDLV70p3OAH6ldQiObmPDN14QUUXwPbk0yywSwruIff5mwfOBgXurEyo1tUQBcLgKtCgsbDipI/kQ==";


    final CloudStorageAccount account;
    final CloudStorageAccount publishAccount;
    final CloudStorageAccount devAccount;
    final CloudTableClient tableClient;
    final CloudTable userTable;
    final CloudTable userApTable;
    final CloudTable messageTable;

    private Storage() throws URISyntaxException, StorageException, InvalidKeyException {
        publishAccount = CloudStorageAccount.parse(storageConnectionString);
        devAccount = publishAccount.getDevelopmentStorageAccount();

        account = publishAccount;

        tableClient = account.createCloudTableClient();

        userTable = tableClient.getTableReference(TABLE_USER);
        userApTable = tableClient.getTableReference(TABLE_USER_AP);
        messageTable = tableClient.getTableReference(TABLE_MESSAGE);

    }

    private static Storage sStorage = null;

    public static synchronized Storage getInstance() {
        if (null == sStorage) {
            try {
                sStorage = new Storage();
            } catch (Throwable throwable) {
                throw new RuntimeException("Fail to initialize Storage", throwable);
            }
        }
        return sStorage;
    }

    public CloudTable getUserApTable() {
        return userApTable;
    }

    public CloudTable getUserTable() {
        return userTable;
    }

    public CloudTable getMessageTable() {
        return messageTable;
    }

}