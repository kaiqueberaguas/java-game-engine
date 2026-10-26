package br.com.beraguasstudio.engine.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.Properties;

class AssetsConfigTest {

    @Test
    void defaultsResolveUnderAssetsFolders() {
        AssetsConfig config = AssetsConfig.defaults();

        assertEquals("assets/sprites/player.png", config.sprite("player.png"));
        assertEquals("assets/sounds/jump.wav", config.sound("jump.wav"));
    }

    @Test
    void propertiesOverrideDefaults() {
        Properties props = new Properties();
        props.setProperty("engine.assets.sprites.path", "/gfx/");
        props.setProperty("engine.assets.sounds.path", "audio");

        AssetsConfig config = AssetsConfig.from(props);

        assertEquals("gfx/player.png", config.sprite("player.png"));
        assertEquals("audio/jump.wav", config.sound("jump.wav"));
    }

    @Test
    void missingOrBlankPropertiesKeepDefaults() {
        Properties props = new Properties();
        props.setProperty("engine.assets.sprites.path", "  ");

        AssetsConfig config = AssetsConfig.from(props);

        assertEquals("assets/sprites/a.png", config.sprite("a.png"));
        assertEquals("assets/sounds/a.wav", config.sound("a.wav"));
    }

    @Test
    void nameMaySpanSubfoldersAndLeadingSlashIsIgnored() {
        assertEquals("assets/sprites/enemies/bug.png", AssetsConfig.defaults().sprite("/enemies/bug.png"));
    }
}
