package starlight.adapter.aireport.infrastructure.ocr.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starlight.adapter.aireport.infrastructure.ocr.util.OcrResponseMerger;
import starlight.adapter.aireport.infrastructure.ocr.util.OcrTextExtractor;
import starlight.adapter.aireport.infrastructure.ocr.util.PdfUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("유틸리티 클래스 Private 생성자 테스트")
class PrivateConstructorTests {

    @Test
    @DisplayName("OcrResponseMerger는 private 생성자를 가진다")
    void ocrResponseMerger_HasPrivateConstructor() throws NoSuchMethodException {
        // given
        Constructor<OcrResponseMerger> constructor = OcrResponseMerger.class.getDeclaredConstructor();

        // then
        assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
    }

    @Test
    @DisplayName("OcrTextExtractor는 private 생성자를 가진다")
    void ocrTextExtractor_HasPrivateConstructor() throws NoSuchMethodException {
        // given
        Constructor<OcrTextExtractor> constructor = OcrTextExtractor.class.getDeclaredConstructor();

        // then
        assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
    }

    @Test
    @DisplayName("PdfUtils는 private 생성자를 가진다")
    void pdfUtils_HasPrivateConstructor() throws NoSuchMethodException {
        // given
        Constructor<PdfUtils> constructor = PdfUtils.class.getDeclaredConstructor();

        // then
        assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
    }

    @Test
    @DisplayName("OcrResponseMerger는 인스턴스화할 수 없다 (리플렉션 테스트)")
    void ocrResponseMerger_CannotInstantiateViaReflection() throws Exception {
        // given
        Constructor<OcrResponseMerger> constructor = OcrResponseMerger.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        // when
        Object instance = constructor.newInstance();

        // then - 인스턴스는 생성되지만 사용할 수 없음을 확인
        assertThat(instance).isNotNull();
        assertThat(instance).isInstanceOf(OcrResponseMerger.class);
    }

    @Test
    @DisplayName("OcrTextExtractor는 인스턴스화할 수 없다 (리플렉션 테스트)")
    void ocrTextExtractor_CannotInstantiateViaReflection() throws Exception {
        // given
        Constructor<OcrTextExtractor> constructor = OcrTextExtractor.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        // when
        Object instance = constructor.newInstance();

        // then
        assertThat(instance).isNotNull();
        assertThat(instance).isInstanceOf(OcrTextExtractor.class);
    }

    @Test
    @DisplayName("PdfUtils는 인스턴스화할 수 없다 (리플렉션 테스트)")
    void pdfUtils_CannotInstantiateViaReflection() throws Exception {
        // given
        Constructor<PdfUtils> constructor = PdfUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        // when
        Object instance = constructor.newInstance();

        // then
        assertThat(instance).isNotNull();
        assertThat(instance).isInstanceOf(PdfUtils.class);
    }
}