package starlight.bootstrap.objectstorage;

public interface PresignedUrlReader {
    PreSignedUrlResponse getPreSignedUrl(String prefix, String originalFileName);
    public String makePublic(String objectUrl);
}