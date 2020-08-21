package me.sjlee.crawling.service;

import me.sjlee.crawling.domain.Song;
import me.sjlee.crawling.exception.DifferentSizeArtistAndTitleException;
import me.sjlee.crawling.exception.NoMatchingResultException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScrappingService {

    public List<Song> getSongList(Document doc, String artist, String title) {
        return scrapUrl(doc, artist, title);
    }

    /**
     *  artistList(List<String>)와 titleList(List<String>) 를 SongList(List<Song>) 객체로 변환
     */
    // TODO Exception 처리
    private List<Song> scrapUrl(Document doc, String artist, String title) {
        List<String> artistList = scrapUrlBykeyword(doc, artist);
        List<String> titleList = scrapUrlBykeyword(doc, title);
        if(artistList.size() != titleList.size()) {
            throw new DifferentSizeArtistAndTitleException();
        }

        List<Song> songList = new ArrayList<>();
        for (int i = 0; i < Math.min(artistList.size(), titleList.size()); i++) {
            songList.add(new Song(artistList.get(i), titleList.get(i)));
        }

        return songList;
    }

    /**
     *  keyword 기반으로 html페이지를 스크래핑 한 뒤 결과를 가져온다.
     */
    private List<String> scrapUrlBykeyword(Document doc, String keyword) {
        Element matchingWrappingTag = findWrappingTag(doc.body(), keyword);
        if(matchingWrappingTag == null) {
            throw new NoMatchingResultException();
        }
        String hierarchicalPath = findHierarchicalPath(matchingWrappingTag);
        List<String> nodes = findNodes(doc, hierarchicalPath);

        return nodes;
    }

    /**
     *  DFS를 이용하여 BODY태그부터 훑어가며 keyword에 처음으로 맞는 키워드 찾음
     */
    private Element findWrappingTag(Element currentTag, String keyword) {
        if(currentTag.childrenSize() == 0) {
            // TODO 유명한 노래나 가수 어떻게 처리할 것인가?
            if(currentTag.text().contains(keyword)) {
                return currentTag;
            }
            return null;
        }
        for(int i = 0 ; i < currentTag.childrenSize() ; i++){
            Element ret = findWrappingTag(currentTag.child(i), keyword);
            if(ret != null){
                return ret;
            }
        }
        return null;
    }

    /**
     *  같은 부모가 없는 태그들을 반환
     */
    private List<String> findNodes(Document doc, String hierarchicalPath) {
        Elements elements = doc.select(hierarchicalPath);

        List<String> nodes = new ArrayList<>();
        elements.stream()
                .reduce(new Element("init"), (e1, e2) -> {
                    if(e1.parent() != e2.parent()) nodes.add(e2.text());
                    return e2;
                });

        return nodes;
    }

    /**
     *  어떤 태그의 계층적인 구조를 String으로 변환하여 반환
     */
    private String findHierarchicalPath(Element currentTag) {
        List<String> hierarchy = new ArrayList<>();
        while(currentTag.parent() != null) {
            if(hasId(currentTag)) {
                hierarchy.add('#' + currentTag.id());
            } else if(hasClassname(currentTag)) {
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

    /**
     *  Class명이 있는 경우 환 반환
     */
    private boolean hasClassname(Element currentTag) {
        if(currentTag.className() == null || currentTag.className().isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     *  id가 있는 경우 true를 반환
     */
    private boolean hasId(Element currentTag) {
        if(currentTag.id() == null || currentTag.id().isEmpty()) {
            return false;
        }
        return true;
    }
}
