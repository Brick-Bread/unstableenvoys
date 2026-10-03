package com.unstablemc.unstableenvoys.libraries;

import com.artillexstudios.axapi.libraries.Library;

import java.util.List;

public enum Libraries {
    SLF4J("org.slf4j:slf4j-api:2.0.9"),
    COMMONS_IO("commons-io:commons-io:2.15.0"),
    COMMONS_TEXT("org{}apache{}commons:commons-text:1.11.0");

    private final Library library;

    Libraries(String library) {
        String[] split = library.split(":");

        this.library = new Library(split[0].replace("{}", "."),
                split[1].replace("{}", "."),
                split[2].replace("{}", "."),
                "",
                List.of()
        );
    }

    public Library library() {
        return this.library;
    }
}
