# AquaLang

[![Java](https://img.shields.io/badge/Java-8-orange.svg)](https://shields.io/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.8%2B-dark_green.svg)](https://shields.io/)
[![Folia](https://img.shields.io/badge/Folia-Supported-blueviolet.svg)](https://papermc.io/software/folia)
[![JitPack](https://jitpack.io/v/aquatic-studios/AquaLang.svg)](https://jitpack.io/#aquatic-studios/AquaLang)

Multi-language plugin for Bukkit/Spigot servers. It stores a language per player, works with
PlaceholderAPI, can pull language files from a GitHub repository, and ships an API so other plugins
can translate their own messages into each player's language.

> [!IMPORTANT]
> Built for **Java 8**, so a single jar runs on **Minecraft 1.8 up to the latest**. HEX colors
> (`§x§r§r§g§g§b§b`) and MiniMessage only render on **1.16+** — use legacy codes (`&a`, `&b`, ...)
> on older clients.

> [!CAUTION]
> Depend only on the **api** module from JitPack, never the full plugin jar. The implementation
> already ships with the plugin — shading it into your own jar causes class conflicts.

### Features

- A language per player, with locale aliases (`en_US`, `es_ES`, `ja_JP`, ...)
- 21 bundled languages, reloadable from disk
- Languages cached in memory on join — no database query per placeholder
- PlaceholderAPI expansions: `%aqlang_(file)_(key)%`, `%aqualang_language%`, `%aqualang_locale%`
- GitHub sync of language files (content-hash diffing, Zip-Slip protection)
- 3 databases: SQLite (default), MySQL, MariaDB — HikariCP, async writes
- SQLite works out of the box; the MySQL/MariaDB driver is downloaded only if you pick it
- Runs on Folia from the same jar
- HEX, MiniMessage and legacy color codes
- API with `CompletableFuture`, namespaces and cancellable events

### Getting Started

Compile against the `api` module. The implementation lives in the plugin and registers itself on
enable, so the API is ready once the server starts. Available on JitPack:

#### Maven

```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```

```xml
<dependency>
    <groupId>com.github.aquatic-studios.AquaLang</groupId>
    <artifactId>api</artifactId>
    <version>version</version>
    <scope>provided</scope>
</dependency>
```

#### Gradle

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    compileOnly 'com.github.aquatic-studios.AquaLang:api:version'
}
```

Add it to your `plugin.yml`:

```yaml
depend: [AquaLang]   # or softdepend, if optional
```

### Usage

Translate a message in the player's saved language:

```java
String msg = AquaLangAPI.translate(player, "welcome.message");
player.sendMessage(msg);
```

Use your own namespace (a file like `languages/<lang>/myplugin.yml`), force a language, or
translate and send in one call:

```java
AquaLangAPI.translate(player, "myplugin", "menu.title");                  // your namespace
AquaLangAPI.translateInLang("es_ES", "welcome.message");                  // forced language
AquaLangAPI.api().send(player, "shop.bought", "<item>", "Diamond Sword"); // translate + send
```

Read or change a player's language. Reads come from the cache; writes run async:

```java
String current = AquaLangAPI.getLanguage(player);
AquaLangAPI.setLanguageAsync(player.getUniqueId(), "es_ES").thenAccept(ok -> { /* ... */ });
```

Check the API is available (useful with `softdepend`) and require a minimum version:

```java
if (!AquaLangAPI.isAvailable()) return;   // AquaLang isn't installed or enabled
AquaLangAPI.requireVersion(3);            // throws if the API is older than 3
```

Colorize any string, check whether a key exists, or translate with a fallback:

```java
String text = AquaLangAPI.color("&aHi &#FF5555World");                      // hex + MiniMessage + legacy
boolean has = AquaLangAPI.exists("english", "welcome.message");            // does the key exist?
String safe = AquaLangAPI.translateOrDefault(uuid, "myplugin", "x", "N/A"); // translate, or "N/A"
```

List the registered languages (handy for building menus):

```java
Set<String> languages = AquaLangAPI.getRegistered();  // ["english", "spanish", ...]
Set<String> locales   = AquaLangAPI.getLocales();     // ["en_US", "es_ES", ...]
String defaultLang    = AquaLangAPI.getDefault();
boolean valid         = AquaLangAPI.api().isLanguageRegistered("es_ES");
```

### Events

`PlayerLanguageChangeEvent` fires before a change is saved, and can be cancelled or rewritten:

```java
@EventHandler
public void onLangChange(PlayerLanguageChangeEvent event) {
    if (!allowed(event)) event.setCancelled(true);
}
```

### AquaLangAddon

An example plugin in this repository that uses the API to open a language-selection GUI. Use it as
a reference for your own integration.

### GitHub Sync

Language files can be synced from a GitHub repository, which makes it easier to manage translations
across several servers. Set a repository, branch, and folder in `config.yml`, then run
`/aqualang github sync`. Changes are downloaded into the local `languages/` folder, unchanged files
are skipped using content hashes, and languages can reload automatically when the sync finishes.
Private repositories need a token, and downloaded files are checked to prevent path-traversal.

```yaml
github:
  repository:
    name: "owner/repo"
    branch: "main"

  authentication:
    type: "none"              # none or token
    token: ""                 # personal access token, only needed when type is token

  paths:
    remote-root: "languages"  # folder inside the repo
    local-root: "languages"   # folder inside the plugin

  sync:
    create-missing: true      # download files you don't have locally
    overwrite-existing: true  # replace your local files with the repo ones
    delete-missing: false     # delete local files that aren't in the repo
    reload-after-sync: true   # reload the languages once it's done
```

### License

See [LICENSE](LICENSE) for the full terms — Powered by **Aquatic Studios** © 2026.
