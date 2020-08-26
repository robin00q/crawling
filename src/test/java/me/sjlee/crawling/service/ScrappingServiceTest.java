package me.sjlee.crawling.service;

import me.sjlee.crawling.constant.ScrappingConstant;
import me.sjlee.crawling.domain.Song;
import me.sjlee.crawling.exception.InvalidUrlGivenException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class ScrappingServiceTest {

    @Autowired
    ScrappingService scrappingService;

    @Autowired
    ResourceLoader resourceLoader;

    /**
     * 파일에서 리소스 읽기
     */
    Document getBodyReadByTestResource(String filePath) throws IOException {
        Resource resource = resourceLoader.getResource(filePath);

        File file = resource.getFile();
        Document doc = Jsoup.parse(file, "UTF-8");

        return doc;
    }

    @DisplayName("스크래핑 url을 사용한 종합 테스트")
    @Test
    void scarppingTestUsingURL() throws InvalidUrlGivenException {
        List<Song> songList = null;
        songList = scrappingService.getSongList(ScrappingConstant.GENIE_SITE_URL, "싹쓰리", "다시 여기 바닷가");

        assertNotNull(songList.size(), () -> "SongList가 Null입니다.");
        for (Song s : songList) {
            System.out.println(s.getTitle() + " " + s.getArtist());
        }
    }

    @DisplayName("현재 태그에서 최고 부모까지의 Hierarchy를 가져온다, 이 테스트는 서비스 메서드 작성을 위한 테스트이다.")
    @Test
    void getTagHierarchy() throws IOException {
        Document doc = Jsoup.connect(ScrappingConstant.BUGS_SITE_URL).get();
        Element wrappingTag = testFindWrappingTag(doc.body(), "싹쓰리");
        String pathHierarchy = findHierarchy(wrappingTag);

        System.out.println(pathHierarchy);

        Elements elements = doc.select(pathHierarchy);
        List<String> ret = new ArrayList<>();

        elements.stream()
                .reduce(new Element("init"), (e1, e2) -> {
                    if (e1.parent() != e2.parent()) {
                        ret.add(e2.text());
                    }
                    return e2;
                });

        for (String s : ret) {
            System.out.println(s);
        }

        System.out.println(ret.size());
    }

    private String findHierarchy(Element currentTag) {
        List<String> hierarchy = new ArrayList<>();
        while (currentTag.parent() != null) {
            if (hasId(currentTag)) {
                hierarchy.add('#' + currentTag.id());
            } else if (hasClassname(currentTag)) {
                String className = Arrays.stream(currentTag.className().split(" "))
                        .map(s -> '.' + s)
                        .collect(Collectors.joining());
                hierarchy.add(className);
            } else {
                hierarchy.add(currentTag.tagName());
            }
            currentTag = currentTag.parent();
        }
        Collections.reverse(hierarchy);
        return hierarchy.stream().collect(Collectors.joining(" > "));
    }

    private boolean hasClassname(Element currentTag) {
        if (currentTag.className() == null || currentTag.className().isEmpty()) {
            return false;
        }
        return true;
    }

    private boolean hasId(Element currentTag) {
        if (currentTag.id() == null || currentTag.id().isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * DFS를 이용하여 BODY태그부터 훑어가며 keyword에 처음으로 맞는 키워드 찾음
     */
    private Element testFindWrappingTag(Element currentTag, String keyword) {
        if (currentTag.childrenSize() == 0) {
            // TODO 유명한 노래나 가수 어떻게 처리할 것인가?
            if (currentTag.text().contains(keyword)) {
                return currentTag;
            }
            return null;
        }
        for (int i = 0; i < currentTag.childrenSize(); i++) {
            Element ret = testFindWrappingTag(currentTag.child(i), keyword);
            if (ret != null) {
                return ret;
            }
        }
        return null;
    }


}