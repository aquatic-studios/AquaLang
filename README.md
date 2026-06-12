# AquaLang

[![Java](https://img.shields.io/badge/Java-8-orange.svg)](https://shields.io/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.8%2B-dark_green.svg)](https://shields.io/)
[![Folia](https://img.shields.io/badge/Folia-Supported-blueviolet.svg)](https://papermc.io/software/folia)
[![JitPack](https://jitpack.io/v/aquatic-studios/AquaLang.svg)](https://jitpack.io/#aquatic-studios/AquaLang)

Multi-language plugin for Minecraft with PlaceholderAPI integration, GitHub-based synchronization
of language files and a small, professional API so any other plugin can translate its messages
through the player's chosen language.

> [!IMPORTANT]
> Compiled to **Java 8 bytecode**, so a single jar runs from **Minecraft 1.8 up to the latest**.
> HEX colors (`§x§r§r§g§g§b§b`) and MiniMessage only render on **1.16+** — on older clients use
> legacy codes (`&a`, `&b`, ...).

> [!CAUTION]
> Only depend on the **API** module from JitPack, never on the full plugin jar. The plugin already
> ships the implementation — shading it inside your own jar will cause class conflicts.

The whole point of the API is to stay simple: one static facade, async by default, a fluent
service if you want to keep a reference, and Bukkit events for reactive integrations. No
configuration, no `onEnable` boilerplate.

### Features

- Per-player language with locale aliases (`en_US`, `es_ES`, `ja_JP`, ...)
- **21 bundled languages** out of the box, hot-reloadable from disk
- In-memory cache loaded on `AsyncPlayerPreLoginEvent` — no DB hit per placeholder call
- PlaceholderAPI expansions: `%aqlang_(file)_(key)%`, `%aqualang_language%`, `%aqualang_locale%`
- GitHub sync of language files (with Zip-Slip protection and content-hash diffing)
- **5 databases** — H2, SQLite, MySQL, MariaDB, PostgreSQL — via HikariCP, fully async writes
- Driver for the chosen database is downloaded on demand, so the jar stays light
- **Folia support** out of the box (region/async schedulers, single jar)
- MiniMessage + legacy + HEX color support
- Public API with `CompletableFuture`, namespaces and cancellable events

### Getting Started

The API targets **Java 8** and uses `CompletableFuture` for any operation that touches the
database, so calling it from the main thread is safe. You only need to depend on the `api`
module — the implementation lives inside the plugin and registers itself on enable.

You can drop it into your project with JitPack:

#### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

```xml
<dependency>
    <groupId>com.github.aquatic-studios.AquaLang</groupId>
    <artifactId>api</artifactId>
    <version>version</version>
    <scope>provided</scope>
</dependency>
```

#### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.github.aquatic-studios.AquaLang:api:version")
}
```

#### Gradle (Groovy)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.aquatic-studios.AquaLang:api:version'
}
```

#### plugin.yml

```yaml
depend: [AquaLang]
# or, if AquaLang is optional at runtime:
# softdepend: [AquaLang]
```

### Usage

The simplest case is translating a message for a player using their saved language:

```java
String msg = AquaLangAPI.translate(player, "welcome.message");
player.sendMessage(msg);
```

UUIDs work the same way:

```java
String msg = AquaLangAPI.translate(uuid, "welcome.message");
```

If you ship your own language files under a namespace (a folder like
`languages/<lang>/myplugin.yml`), pass it explicitly:

```java
String msg = AquaLangAPI.translate(player, "myplugin", "menu.title");
```

To force a specific language regardless of the player's choice:

```java
String msg = AquaLangAPI.translateInLang("es_ES", "welcome.message");
```

Or translate, replace placeholders, colorize and send in one call:

```java
// pairs: "<search>", "replacement"
AquaLangAPI.api().send(player, "shop.bought", "<item>", "Diamond Sword", "<price>", "500");
```

### Player language

`getLanguage` returns from the in-memory cache, no database hit:

```java
String current = AquaLangAPI.getLanguage(player);

AquaLangAPI.setLanguageAsync(player.getUniqueId(), "es_ES")
        .thenAccept(ok -> getLogger().info("Switched: " + ok));
```

### Compatibility detection

If your addon ships translations for a subset of locales, AquaLang tells you which ones
overlap with the server's registered languages:

```java
Set<String> mine = new HashSet<>(Arrays.asList("en_US", "es_ES", "ja_JP"));
Set<String> compatible = AquaLangAPI.api().compatibleLanguages(mine);
```

### Events

`PlayerLanguageChangeEvent` is fired before any switch is persisted and can be cancelled or
rewritten:

```java
@EventHandler
public void onLangChange(PlayerLanguageChangeEvent event) {
    if (event.getNewLanguage().equals("ja_JP") && !hasPermission(event)) {
        event.setCancelled(true);
    }
}
```

### Fluent style

The static facade is just a wrapper around the registered `AquaLangAPI` instance, so you can grab
it once if you prefer the fluent style:

```java
AquaLangAPI api = AquaLangAPI.api();

String msg  = api.message(player, "welcome.message");
String def  = api.getDefaultLanguage();
Set<String> locales = api.getRegisteredLocales();
```

You can also guard against older AquaLang versions during your `onEnable`:

```java
AquaLangAPI.requireVersion(3);
```

### GitHub sync

Instead of editing language files on every server by hand, AquaLang can keep them in sync with a
**GitHub repository**. You maintain your translations in one repo, and each server pulls the latest
version with a single command — handy for networks running many servers, or for crowd-sourced
translations through pull requests.

**How it works:** point AquaLang at a repo, branch and folder, then run `/aqualang github sync`
(console or in-game). It downloads that folder and updates your local `languages/` directory.
Changes are detected by **content hash**, so a sync with nothing new returns `NO_CHANGES` without
touching the disk, and languages can be reloaded automatically when it finishes — no restart needed.

```yaml
github:
  repository:
    name: "owner/repo"        # the repo holding your language files
    branch: "main"
  authentication:
    type: "none"              # "none" for public repos, "token" for private ones
    token: ""                 # GitHub personal access token (only when type: token)
  paths:
    remote-root: "languages"  # folder inside the repo to pull from
    local-root: "languages"   # folder inside the plugin to write to
  sync:
    create-missing: true      # download files you don't have yet
    overwrite-existing: true  # replace local files with the repo version
    delete-missing: false     # delete local files that were removed from the repo
    reload-after-sync: true   # reload languages automatically when done
```

Private repositories need a token (`type: "token"`). Downloaded files are validated against the
local folder to block path-traversal (Zip-Slip), so a malicious archive can't write outside
`languages/`.

### Notes

- All database writes go through an async pool, so calling `setLanguageAsync` from the main thread is safe.
- Works on Bukkit/Spigot, Paper and Folia from the same jar — schedulers are picked at runtime.
- The cache is keyed by `UUID` and shared across every API caller.
- Locale lookups (`getLocaleOf`, `compatibleLanguages`) are O(1) — backed by a reverse map built at load time.
- Player language is preloaded on `AsyncPlayerPreLoginEvent` and dropped on `PlayerQuitEvent`.
- HEX color support requires Minecraft 1.16+. Use legacy codes (`&a`, `&b`, ...) on older clients.
- Do **not** bundle the API module inside your plugin jar — the implementation is already on the server.

### License

See [LICENSE](LICENSE) for the full terms. Powered by **Aquatic Studios** © 2026.
