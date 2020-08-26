package me.sjlee.crawling;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsoupLearningTests {
    @DisplayName("JSOUP 학습 테스트 / 스트링 파싱")
    @Test
    void parse_a_document_from_a_String() {
        String html = "<html><head><title>First parse</title></head>"
                + "<body><p>Parsed HTML into a doc.</p></body></html>";

        Document doc = Jsoup.parse(html);
        Document doc2 = Jsoup.parse(html, "www.naver.com");

        assertEquals(doc.toString(), doc2.toString());
        assertThrows(NullPointerException.class, () -> Jsoup.parse(null));

        System.out.println(doc.toString());
    }

    @DisplayName("JSOUP 학습 테스트 / Body 파싱")
    @Test
    void parse_a_body_fragment() {
        String html = "<div><p>Lorem ipsum.</p>";
        Document doc = Jsoup.parseBodyFragment(html);
        Elements p = doc.getElementsByTag("p");

        assertEquals(p.get(0).getClass(), Element.class);
        assertTrue(p.get(0).tagName().equals("p"));
        assertEquals(p.get(0).childNodes().get(0).toString(), "Lorem ipsum.");

        String clean = Jsoup.clean(html, Whitelist.simpleText());
        assertEquals(clean, "Lorem ipsum.");
    }

    @DisplayName("JSOUP 학습 테스트 / URL에서 로드해오기")
    @Test
    void Load_a_Document_from_a_URL() throws IOException {
        Document doc = Jsoup.connect("https://www.melon.com/").get();
        String title = doc.title();
        System.out.println(title);
    }

    String className = null;
    int length = 0;
    String KEYWORD = "싹쓰리";

    @DisplayName("사이트에서 태그이름을 사용하여 태그별로 가져오기")
    @Test
    void Use_DOM_methods_to_navigate_a_document() throws IOException {
        Document doc = Jsoup.connect("https://www.melon.com/chart/day/index.htm").get();
//        Document doc = Jsoup.connect("https://www.genie.co.kr/chart/top200").get();

        Element body = doc.body();

        if(findWrappingTag(body)) {
            System.out.println(className);
        }

        Elements songList = doc.getElementsByClass(className);

        for(Element element : songList) {
            System.out.println(element);
        }
    }

    @DisplayName("Invalid한 문자열을 주는 경우")
    @Test
    void invalid_string_given_to_jsoup() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> Jsoup.connect("invalid").get());
    }

    @DisplayName("Invalid한 문자열(null)을 주는 경우")
    @Test
    void invalid_string_null_given_to_jsoup() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> Jsoup.connect(null).get());
    }

    private boolean findWrappingTag(Element currentTag) {
        if(currentTag.childrenSize() == 0) {
            // TODO 유명한 노래나 가수 어떻게 처리할 것인가?
            if(currentTag.text().contains(KEYWORD)) {
                length = currentTag.text().length();
                initClassName(currentTag);
                return true;
            }
            return false;
        }
        for(int i = 0 ; i < currentTag.childrenSize() ; i++){
            if(findWrappingTag(currentTag.child(i))) {
                initClassName(currentTag);
                return true;
            }
        }
        return false;
    }

    private void initClassName(Element currentTag) {
        if (className == null && (currentTag.className() != null && !currentTag.className().isEmpty())) {
            className = currentTag.className();
        }
    }


}
