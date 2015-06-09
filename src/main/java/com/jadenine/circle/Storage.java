package com.jadenine.circle;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by linym on 6/2/15.
 */
public class Storage {
    public static final String PARTITION_KEY = "PartitionKey";
    public static final String ROW_KEY = "RowKey";
    public static final String TIMESTAMP = "Timestamp";


    private static final String TABLE_USER = "user";
    private static final String TABLE_USER_AP = "userap";
    private static final String TABLE_TOPIC = "topic";
    private static final String TABLE_MESSAGE = "message";

    // Define the connection-string with your values.
    public static final String storageConnectionString =
            "TableEndpoint=https://circlestorage.table.core.chinacloudapi.cn/;" +
                    "AccountName=circlestorage;" +
                    "AccountKey=SPa+eHBBeJDLV70p3OAH6ldQiObmPDN14QUUXwPbk0yywSwruIff5mwfOBgXurEyo1tUQBcLgKtCgsbDipI/kQ==";

    private final CloudStorageAccount account;
    private final CloudStorageAccount publishAccount;
    private final CloudStorageAccount devAccount;
    private final CloudTableClient tableClient;
    private final CloudTable userTable;
    private final CloudTable userApTable;
    private final CloudTable messageTable;
    private final CloudTable topicTable;

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

    public CloudTable getTopicTable(){
        return topicTable;
    }

    private Storage() throws URISyntaxException, StorageException, InvalidKeyException,
            KeyManagementException, NoSuchAlgorithmException {
        setTrustAllCertInStorage();

        publishAccount = CloudStorageAccount.parse(storageConnectionString);
        devAccount = publishAccount.getDevelopmentStorageAccount();

        account = publishAccount;

        tableClient = account.createCloudTableClient();

        userTable = tableClient.getTableReference(TABLE_USER);
        userApTable = tableClient.getTableReference(TABLE_USER_AP);
        topicTable = tableClient.getTableReference(TABLE_TOPIC);
        messageTable = tableClient.getTableReference(TABLE_MESSAGE);
    }

    private void setTrustAllCertInStorage() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] x509Certificates,
                                           String s) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] x509Certificates,
                                           String s) throws CertificateException {
            }

            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        }};

        SSLContext sc = SSLContext.getInstance("TLS");

        sc.init(null, trustAllCerts, new SecureRandom());

        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }
}