package de.unistuttgart.einf.moviemanager.model;

import java.util.List;

/**
 * The genre of a media item.
 */
public enum Genre {

    ACTION("action", "fight", "martial arts", "explosion", "heist", "war"),
    ADVENTURE("adventure", "quest", "exploration", "survival", "treasure"),
    ANIMATION("animation", "animated", "cartoon", "pixar", "dreamworks"),
    ANIME("anime", "manga", "otaku", "japanese animation"),
    COMEDY("comedy", "funny", "humor", "sitcom", "romcom", "parody"),
    CRIME("crime", "mafia", "gang", "murder", "investigation", "police"),
    DOCUMENTARY("documentary", "true story", "biography", "real life"),
    DRAMA("drama", "tragic", "emotional", "character study", "tearjerker"),
    FAMILY("family", "kids", "children", "parenting", "heartwarming"),
    FANTASY("fantasy", "magic", "wizard", "dragon", "myth", "fairy tale"),
    HISTORY("history", "historical", "period piece", "biography", "war", "politics"),
    HORROR("horror", "scary", "ghost", "haunting", "slasher", "zombie"),
    KIDS("kids", "children", "family", "animated", "cartoon"),
    MUSIC("music", "musical", "concert", "band", "singer", "song"),
    MYSTERY("mystery", "suspense", "thriller", "crime", "detective", "whodunit"),
    NEWS("news", "current events", "journalism", "reporting", "investigation"),
    POLITICS("politics", "government", "election", "campaign", "policy", "diplomacy"),
    REALITY("reality", "reality show", "competition", "dating show", "talent show"),
    ROMANCE("romance", "love story", "dating", "relationship", "heartbreak"),
    SCIENCE_FICTION("sci fi", "scifi", "science fiction", "space", "alien", "time travel", "future", "dystopia"),
    SOAP("soap", "soap opera", "melodrama", "family drama", "romance"),
    TALK("talk", "talk show", "interview", "panel", "late night"),
    THRILLER("thriller", "suspense", "mystery", "crime", "detective", "serial killer", "psychological"),
    TV_MOVIE("tv movie", "television", "made for tv", "tv film"),
    WAR("war", "military", "battle", "soldier", "combat", "world war"),
    WESTERN("western", "cowboy", "outlaw", "sheriff", "gunslinger", "wild west");

    private final List<String> keywords;

    Genre(String... keywords) {
        this.keywords = List.of(keywords);
    }

    public List<String> getKeywords() {
        return keywords;
    }

}
