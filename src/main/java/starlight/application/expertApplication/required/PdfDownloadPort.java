package starlight.application.expertApplication.required;

public interface PdfDownloadPort {

    byte[] downloadFromUrl(String url);
}
