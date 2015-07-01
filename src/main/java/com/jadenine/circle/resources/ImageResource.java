package com.jadenine.circle.resources;

import com.jadenine.circle.Storage;
import com.jadenine.circle.entity.Image;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by linym on 6/30/15.
 */
@Path("/image")
public class ImageResource {

    public static final int EXPIRE_TIME_IN_MINUTE = 5;

    @POST
    @Path("/requestWritableSas")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWritableSas() {
        //TODO limit the size of image upload each time, and daily upload limit.

        String mediaId = String.valueOf(AutoDecrementIdGenerator.getNextId());

        Image image = new Image();
        image.setMediaId(mediaId);
        try {
            CloudBlobContainer imageBlobContainer = Storage.getInstance().getImageBlobContainer();
            CloudBlockBlob imageBlob = imageBlobContainer.getBlockBlobReference(mediaId);

            image.setWritableSas(getWritableImageSas(imageBlob));
            image.setReadableSas(getReadableImageSas(imageBlob));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (StorageException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.ok().entity(image).build();
    }

    private String getWritableImageSas(CloudBlockBlob imageBlob) throws URISyntaxException,
            StorageException, InvalidKeyException {

        String sharedAccessSignature = imageBlob.generateSharedAccessSignature(Storage
                .getInstance().getWritePolicy(EXPIRE_TIME_IN_MINUTE), null);

        return joinSasUrl(imageBlob, sharedAccessSignature);
    }

    private String getReadableImageSas(CloudBlockBlob imageBlob) throws URISyntaxException,
            StorageException, InvalidKeyException {
        String sharedAccessSignature = imageBlob.generateSharedAccessSignature(null,
                Storage.getInstance().getReadPolicyIdentifier());

        return joinSasUrl(imageBlob, sharedAccessSignature);
    }

    private String joinSasUrl(CloudBlockBlob imageBlob, String sharedAccessSignature) {
        return imageBlob.getUri() + "?" + sharedAccessSignature;
    }
}
