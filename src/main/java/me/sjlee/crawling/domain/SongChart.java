package me.sjlee.crawling.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@RedisHash("songChart")
public class SongChart {

    @Id
    private String id;

    private String url;

    private List<Song> songList = new ArrayList<>();

    public SongChart(String url, List<Song> songList) {
        this.url = url;
        this.songList = songList;
    }
}
