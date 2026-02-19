package starlight.application.aireport.required;

public interface PdfDownloadPort {

    byte[] downloadFromUrl(String url);
}
