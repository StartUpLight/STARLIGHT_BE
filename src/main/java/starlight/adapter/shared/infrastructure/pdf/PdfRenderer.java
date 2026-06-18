package starlight.adapter.shared.infrastructure.pdf;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.svgsupport.BatikSVGDrawer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import starlight.adapter.shared.infrastructure.pdf.mapper.AiReportPdfViewMapper;
import starlight.adapter.shared.infrastructure.pdf.view.AiReportPdfView;
import starlight.application.aireport.provided.dto.AiReportResult;
import starlight.application.aireport.required.AiReportPdfRenderPort;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class PdfRenderer implements AiReportPdfRenderPort {

    private static final String FONT_FAMILY = "PdfReportFont";
    private static final List<String> CLASSPATH_FONTS = List.of(
            "fonts/NotoSansKR-Regular.ttf"
    );
    private static final List<Path> SYSTEM_FONTS = List.of(
            Path.of("C:\\Windows\\Fonts\\malgun.ttf"),
            Path.of("/usr/share/fonts/truetype/nanum/NanumGothic.ttf"),
            Path.of("/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc"),
            Path.of("/usr/share/fonts/google-noto-cjk/NotoSansCJK-Regular.ttc")
    );
    private static final ConcurrentHashMap<String, File> CLASSPATH_FONT_CACHE = new ConcurrentHashMap<>();

    private final SpringTemplateEngine templateEngine;
    private final AiReportPdfViewMapper aiReportPdfViewMapper;

    @PostConstruct
    void warmUpClasspathFontCache() {
        log.info("[AI_REPORT_PDF] warming up classpath font cache");
        for (String classpathFont : CLASSPATH_FONTS) {
            try {
                ClassPathResource resource = new ClassPathResource(classpathFont);
                if (resource.exists()) {
                    getCachedClasspathFont(classpathFont, resource);
                }
            } catch (Exception e) {
                log.error("[AI_REPORT_PDF] font warm-up failed: {}", classpathFont, e);
            }
        }
    }

    @Override
    public byte[] render(AiReportResult report) {
        try {
            AiReportPdfView view = aiReportPdfViewMapper.toView(report);
            Context context = new Context();
            context.setVariable("totalScore", view.totalScore());
            context.setVariable("sections", view.sections());
            context.setVariable("strengths", view.strengths());
            context.setVariable("weaknesses", view.weaknesses());
            context.setVariable("radarChartSvg", view.radarChartSvg());

            String html = templateEngine.process("pdf-ai-report", context);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useSVGDrawer(new BatikSVGDrawer());
            configureFont(builder);
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("[AI_REPORT_PDF] html to pdf render failed", e);
            throw new IllegalStateException("AI 리포트 PDF 생성에 실패했습니다.", e);
        }
    }

    private void configureFont(PdfRendererBuilder builder) {
        for (String classpathFont : CLASSPATH_FONTS) {
            if (registerClasspathFont(builder, classpathFont)) {
                return;
            }
        }
        for (Path systemFont : SYSTEM_FONTS) {
            if (registerFileFont(builder, systemFont)) {
                log.info("[AI_REPORT_PDF] using system font: {}", systemFont);
                return;
            }
        }
        throw new IllegalStateException(
                "한글 PDF 폰트를 찾을 수 없습니다. classpath(" + CLASSPATH_FONTS + ") 또는 OS 폰트를 확인하세요."
        );
    }

    private boolean registerClasspathFont(PdfRendererBuilder builder, String classpathLocation) {
        try {
            ClassPathResource resource = new ClassPathResource(classpathLocation);
            if (!resource.exists()) {
                return false;
            }
            File fontFile = getCachedClasspathFont(classpathLocation, resource);
            if (fontFile == null) {
                log.warn("[AI_REPORT_PDF] skipping unsupported classpath font: {}", classpathLocation);
                return false;
            }
            builder.useFont(fontFile, FONT_FAMILY, 400, BaseRendererBuilder.FontStyle.NORMAL, true);
            return true;
        } catch (Exception e) {
            log.warn("[AI_REPORT_PDF] failed to load classpath font: {}", classpathLocation, e);
            return false;
        }
    }

    private File getCachedClasspathFont(String classpathLocation, ClassPathResource resource) {
        try {
            return CLASSPATH_FONT_CACHE.computeIfAbsent(classpathLocation, key -> loadClasspathFont(classpathLocation, resource));
        } catch (FontCacheMissException e) {
            return null;
        }
    }

    private File loadClasspathFont(String classpathLocation, ClassPathResource resource) {
        try {
            File fontFile = prepareClasspathFontFile(resource);
            if (!isPdfBoxLoadableTrueType(fontFile.toPath())) {
                log.warn("[AI_REPORT_PDF] unsupported or corrupted font format: {}", classpathLocation);
                if (fontFile.exists()) {
                    fontFile.delete();
                }
                throw new FontCacheMissException();
            }
            log.info("[AI_REPORT_PDF] successfully cached classpath font: {}", classpathLocation);
            return fontFile;
        } catch (FontCacheMissException e) {
            throw e;
        } catch (Exception e) {
            log.error("[AI_REPORT_PDF] failed to prepare classpath font file: {}", classpathLocation, e);
            throw new FontCacheMissException(e);
        }
    }

    private static final class FontCacheMissException extends RuntimeException {
        private FontCacheMissException() {
            super();
        }

        private FontCacheMissException(Throwable cause) {
            super(cause);
        }
    }

    private boolean registerFileFont(PdfRendererBuilder builder, Path fontPath) {
        if (!Files.isRegularFile(fontPath) || !isPdfBoxLoadableTrueType(fontPath)) {
            return false;
        }
        try {
            builder.useFont(fontPath.toFile(), FONT_FAMILY, 400, BaseRendererBuilder.FontStyle.NORMAL, true);
            return true;
        } catch (Exception e) {
            log.warn("[AI_REPORT_PDF] failed to load system font: {}", fontPath, e);
            return false;
        }
    }

    private boolean isPdfBoxLoadableTrueType(Path fontPath) {
        try {
            if (!Files.isRegularFile(fontPath)) {
                return false;
            }
            byte[] header = Files.readAllBytes(fontPath);
            if (header.length < 4) {
                return false;
            }
            if (header[0] == 'O' && header[1] == 'T' && header[2] == 'T' && header[3] == 'O') {
                return false;
            }
            try (PDDocument document = new PDDocument()) {
                PDFont font = PDType0Font.load(document, fontPath.toFile());
                return font != null;
            }
        } catch (Exception e) {
            log.debug("[AI_REPORT_PDF] font not loadable: {}", fontPath, e);
            return false;
        }
    }

    private File prepareClasspathFontFile(ClassPathResource resource) throws Exception {
        String suffix = resource.getFilename() != null && resource.getFilename().endsWith(".ttf")
                ? ".ttf"
                : ".font";
        File temp = Files.createTempFile("pdf-report-font-", suffix).toFile();
        temp.deleteOnExit();
        try (InputStream in = resource.getInputStream()) {
            Files.copy(in, temp.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        return temp;
    }
}
