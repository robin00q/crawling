package me.sjlee.crawling.service;

import me.sjlee.crawling.constant.ScrappingConstant;
import me.sjlee.crawling.domain.Song;
import me.sjlee.crawling.domain.SongChart;
import me.sjlee.crawling.exception.DifferentSizeArtistAndTitleException;
import me.sjlee.crawling.exception.InvalidUrlGivenException;
import me.sjlee.crawling.exception.NoMatchingResultException;
import me.sjlee.crawling.repository.SongChartRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ScrappingService {

    @Autowired
    SongChartRepository songChartRepository;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Cacheable(value = "songChart", key = "#url")
    public List<Song> getSongList(String url, String artist, String title) throws InvalidUrlGivenException {
        List<Song> chartedList = new ArrayList<>();
        try {
            chartedList = scrapUrl(Jsoup.connect(url).get(), artist, title);
            SongChart save = songChartRepository.save(new SongChart(url, chartedList));
        } catch(IOException | IllegalArgumentException e) {
            throw new InvalidUrlGivenException();
        }
        return chartedList;
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
        for (int i = 0; i < Math.min(ScrappingConstant.MAX_SONG_LENGTH, titleList.size()); i++) {
            songList.add(new Song(i+1, titleList.get(i), artistList.get(i)));
        }

        return songList;
    }

    /**
     *  keyword 기반으로 html페이지를 스크래핑 한 뒤 결과를 가져온다.
     */
    private List<String> scrapUrlBykeyword(Document doc, String keyword) {
        Element matchingWrappingTag = findWrappingTag(doc, doc.body(), keyword);
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
    private Element findWrappingTag(Document doc, Element currentTag, String keyword) {
        if(currentTag.childrenSize() == 0) {
            // TODO 유명한 노래나 가수 어떻게 처리할 것인가?
            if(currentTag.text().contains(keyword)) {
                String hierarchicalPath = findHierarchicalPath(currentTag);
                Elements elements = doc.select(hierarchicalPath);
                if(elements.size() < ScrappingConstant.MAX_SONG_LENGTH) {
                    return null;
                }
                return currentTag;
            }
            return null;
        }
        for(int i = 0 ; i < currentTag.childrenSize() ; i++){
            Element ret = findWrappingTag(doc, currentTag.child(i), keyword);
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
