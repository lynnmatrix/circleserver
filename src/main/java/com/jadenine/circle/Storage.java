package com.jadenine.circle;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageUri;
import com.microsoft.azure.storage.blob.BlobContainerPermissions;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.HashMap;

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
    private static final String IMAGES_CONTAINER_NAME = "image";

    private static final String ACCOUNT_NAME = "circlestorage";
    private static final String ACCOUNT_KEY =
            "SPa+eHBBeJDLV70p3OAH6ldQiObmPDN14QUUXwPbk0yywSwruIff5mwfOBgXurEyo1tUQBcLgKtCgsbDipI/kQ==";

    private static final String TABLE_END_POINT = "https://" + ACCOUNT_NAME + ".table.core.chinacloudapi.cn/";
    private static final String BLOB_END_POINT = "https://" + ACCOUNT_NAME + ".blob.core.chinacloudapi.cn/";

    private static final StorageUri BLOB_URI = new StorageUri(URI.create(BLOB_END_POINT));
    private static final StorageUri TABLE_URI = new StorageUri(URI.create(TABLE_END_POINT));

    public static final String READ_POLICY_IDENTIFIER = "READ_POLICY_IDENTIFIER";

    // Define the connection-string with your values.
    public static final String storageConnectionString
            = "TableEndpoint=" + TABLE_END_POINT
            + ";AccountName=" + ACCOUNT_NAME
            + ";AccountKey=" + ACCOUNT_KEY;


    private static final int READ_EXPIRE_IN_YEAR = 10; //10年内有效

    private final CloudStorageAccount account;
    private final CloudStorageAccount publishAccount;
    private final CloudStorageAccount devAccount;
    private final CloudTableClient tableClient;
    private final CloudTable userTable;
    private final CloudTable userApTable;
    private final CloudTable messageTable;
    private final CloudTable topicTable;
    private final CloudBlobContainer imageContainer;

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

    public CloudBlobContainer getImageBlobContainer() {
        return imageContainer;
    }

    public String getReadPolicyIdentifier(){
        return READ_POLICY_IDENTIFIER;
    }

    public SharedAccessBlobPolicy getWritePolicy(int minute){
        SharedAccessBlobPolicy readWritePolicy = new SharedAccessBlobPolicy();
        readWritePolicy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ,
                SharedAccessBlobPermissions.WRITE));
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, minute);//minute分钟内有效
        readWritePolicy.setSharedAccessExpiryTime(calendar.getTime());
        return readWritePolicy;
    }

    private Storage() throws URISyntaxException, StorageException, InvalidKeyException,
            KeyManagementException, NoSuchAlgorithmException {
        setTrustAllCertInStorage();

        StorageCredentials credentials = new StorageCredentialsAccountAndKey(ACCOUNT_NAME, ACCOUNT_KEY);
        publishAccount = new CloudStorageAccount(credentials, BLOB_URI, null, TABLE_URI) ;
        /*CloudStorageAccount.parse (storageConnectionString);*/

        devAccount = publishAccount.getDevelopmentStorageAccount();

        account = publishAccount;

        tableClient = account.createCloudTableClient();

        userTable = tableClient.getTableReference(TABLE_USER);
        userApTable = tableClient.getTableReference(TABLE_USER_AP);
        topicTable = tableClient.getTableReference(TABLE_TOPIC);
        messageTable = tableClient.getTableReference(TABLE_MESSAGE);

        CloudBlobClient cloudBlobClient = account.createCloudBlobClient();
        imageContainer = cloudBlobClient.getContainerReference(IMAGES_CONTAINER_NAME);
        imageContainer.createIfNotExists();

        setBlobShareAccessPolicies();
    }

    private void setBlobShareAccessPolicies() throws StorageException {
        BlobContainerPermissions permissions = imageContainer.downloadPermissions();
        HashMap<String, SharedAccessBlobPolicy> sharedAccessPolicies = permissions
                .getSharedAccessPolicies();

        if (!sharedAccessPolicies.containsKey(READ_POLICY_IDENTIFIER)) {
            SharedAccessBlobPolicy readPolicy = new SharedAccessBlobPolicy();
            readPolicy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ));

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, READ_EXPIRE_IN_YEAR);
            readPolicy.setSharedAccessExpiryTime(calendar.getTime());
            sharedAccessPolicies.put(READ_POLICY_IDENTIFIER, readPolicy);

            imageContainer.uploadPermissions(permissions);
        }
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