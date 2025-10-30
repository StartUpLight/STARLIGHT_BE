package starlight.application.infrastructure.provided;

public interface ContextRetriever {

    String retrieveContext(String subSectionTag, String content, int topK);
}



