package me.sjlee.crawling.controller;

import me.sjlee.crawling.constant.ScrappingConstant;
import me.sjlee.crawling.domain.Song;
import me.sjlee.crawling.exception.InvalidUrlGivenException;
import me.sjlee.crawling.service.ScrappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ScrappingController {

    @Autowired
    ScrappingService scrappingService;

    @GetMapping("/")
    public String mainPage() {
        return "index";
    }

    @GetMapping(value = "/chart/{siteName}", produces = {"application/json; charset=UTF-8"})
    @ResponseBody
    @Transactional
    public List<Song> getChartData(@PathVariable String siteName) {
        String url = "";
        if("melon".equals(siteName)) {
            url = ScrappingConstant.MELON_SITE_URL;
        } else if("genie".equals(siteName)) {
            url = ScrappingConstant.GENIE_SITE_URL;
        } else if("bugs".equals(siteName)) {
            url = ScrappingConstant.BUGS_SITE_URL;
        } else {
            url = siteName;
        }
        List<Song> songList = new ArrayList<>();
        try {
            songList = scrappingService.getSongList(url, ScrappingConstant.FAMOUS_ARTIST, ScrappingConstant.FAMOUS_TITLE);
        } catch (InvalidUrlGivenException e) {
            return songList;
        }

        return songList;
    }
}
