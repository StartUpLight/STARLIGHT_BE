package starlight.adapter.spellcheck.spellcheck.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import starlight.adapter.spellcheck.spellcheck.dto.Finding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class SpellCheckUtil {

    public List<String> splitByLength(String s, String delimiters, int maxLen) {
        if (s.length() <= maxLen) {
            return List.of(s);
        };

        Set<Character> sep = new HashSet<>();
        for (char c : delimiters.toCharArray()) sep.add(c);

        List<String> out = new ArrayList<>();
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            buf.append(s.charAt(i));
            if (buf.length() >= maxLen) {
                int cut = -1;
                for (int j = buf.length() - 1; j >= Math.max(0, buf.length() - 50); j--) {
                    if (sep.contains(buf.charAt(j))) { cut = j + 1; break; }
                }
                if (cut == -1) cut = buf.length();
                out.add(buf.substring(0, cut));
                buf.delete(0, cut);
            }
        }
        if (buf.length() > 0) out.add(buf.toString());
        return out;
    }


    public List<Finding> parseToFinding(String html) {
        Document doc = Jsoup.parse(html);
        List<Finding> list = new ArrayList<>();

        for (Element el : doc.select("a.txt_spell[data-error-type]")) {
            String type = decode(el.attr("data-error-type"));
            String token = decode(el.attr("data-error-input"));
            String out   = decode(el.attr("data-error-output"));
            String ctx   = decode(el.attr("data-error-context"));

            String visible = null;
            String severity = "normal";
            Element word = el.selectFirst("span.txt_word");
            if (word != null) {
                word.select("button").remove(); // 버튼 제거
                visible = decode(word.text()).trim();
                var classes = word.classNames();
                if (classes.contains("txt_error"))  severity = "error";      // 확정 오류
                else if (classes.contains("txt_error2")) severity = "doubt"; // 오류 의심
            }

            String original = null;
            Element inner = el.selectFirst("span.inner_spell");
            if (inner != null) original = decode(inner.text()).trim();

            String help = null;
            List<String> examples = new ArrayList<>();
            Element contents = el.selectFirst("span[name=contents]");
            if (contents != null) {
                Element helpUl = contents.selectFirst("ul#help");
                if (helpUl != null) {
                    help = decode(helpUl.text()).trim();
                    if ("도움말이 없습니다.".equals(help)) help = null;
                } else {
                    String raw = decode(contents.text());
                    raw = raw.replaceAll("\\t", "")
                            .replaceAll("\\n{3,}", "\n(예)\n")
                            .replaceAll("\\n+$", "")
                            .replaceAll("^[ \\n]+", "")
                            .trim();
                    if (!raw.isBlank()) help = raw;
                }
                for (Element li : contents.select("div.lst ul#examples li")) {
                    String ex = decode(li.text()).trim();
                    if (!ex.isBlank()) examples.add(ex);
                }
            }

            List<String> suggestions = new ArrayList<>();
            if (out != null && !out.isBlank()) suggestions.add(out.trim());

            list.add(Finding.of(type, severity, token, suggestions, visible, original, ctx, help, examples));
        }
        return list;
    }

    private static String decode(String s) {
        if (s == null) return null;
        return Jsoup.parse(s).text();
    }
}
