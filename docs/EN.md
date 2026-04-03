# LinguaChat — Developer Documentation

## Table of Contents

- [Project Structure](#project-structure)
- [Multi-Version Support with Stonecutter](#multi-version-support-with-stonecutter)
- [Compatibility Layers](#compatibility-layers)
- [Building the Project](#building-the-project)
- [Adding or Removing a Minecraft Version](#adding-or-removing-a-minecraft-version)
- [Public API](#public-api)
- [Localization](#localization)

---

## Project Structure

```
LinguaChat/
├── versions/                 # Version-specific build outputs (generated)
│   ├── 1.16.5/
│   ├── 1.18.2/
│   ├── 1.19.2/
│   ├── 1.20.1/
│   ├── 1.21.1/
│   ├── 1.21.4/
│   ├── 1.21.5/
│   └── 1.21.11/
├── src/                      # Shared source code (all versions)
│   └── main/
│       ├── java/com/linguachat/
│       │   ├── compat/       # Compatibility layers for cross-version support
│       │   │   ├── ButtonCompat.java
│       │   │   ├── I18nCompat.java
│       │   │   ├── KeyBindingCompat.java
│       │   │   ├── ScreenCompat.java
│       │   │   └── TextCompat.java
│       │   ├── config/       # Configuration handling
│       │   │   ├── ModConfig.java
│       │   │   └── ConfigValidator.java
│       │   ├── event/        # Event handlers
│       │   │   └── ChatEvents.java
│       │   ├── gui/          # GUI screens
│       │   │   └── ConfigScreen.java
│       │   ├── integration/  # Mod integrations
│       │   │   └── ModMenuIntegration.java
│       │   ├── keybind/      # Keybinding handling
│       │   │   ├── ModKeybinds.java
│       │   │   └── KeybindHandler.java
│       │   ├── mixin/        # Mixin configurations
│       │   │   └── client/
│       │   │       ├── ChatHudMixin.java
│       │   │       ├── ChatScreenMixin.java
│       │   │       ├── ClientPlayNetworkHandlerMixin.java
│       │   │       └── MessageHandlerMixin.java
│       │   ├── translation/  # Core translation logic
│       │   │   ├── providers/
│       │   │   │   ├── GoogleTranslateClient.java
│       │   │   │   ├── DeepLTranslateClient.java
│       │   │   │   └── KagiTranslateClient.java
│       │   │   ├── TranslationManager.java
│       │   │   ├── TranslationDirection.java
│       │   │   ├── TranslationCache.java
│       │   │   ├── TranslationLogger.java
│       │   │   └── MessageStore.java
│       │   ├── util/         # Utility classes
│       │   │   └── MessageBlocker.java
│       │   ├── version/      # Version utilities
│       │   └── LinguaChatMod.java
│       │   └── LinguaChatClient.java
│       └── resources/
│           ├── assets/linguachat/lang/
│           │   ├── en_us.json    # English translations
│           │   └── ru_ru.json    # Russian translations
│           ├── fabric.mod.json
│           └── linguachat.mixins.json
├── build.gradle              # Main build script
├── settings.gradle           # Stonecutter configuration
├── stonecutter.gradle        # Active version selector
└── docs/                     # Documentation
    ├── EN.md
    └── RU.md
```

### Key Directories

| Directory | Purpose |
|-----------|---------|
| `src/main/java/com/linguachat/compat/` | Abstraction layers for Minecraft version differences |
| `src/main/java/com/linguachat/translation/` | Core translation logic and provider clients |
| `src/main/java/com/linguachat/mixin/` | Mixins for intercepting vanilla chat handling |
| `src/main/resources/assets/linguachat/lang/` | Localization files for UI and logs |
| `versions/*/` | Generated build outputs for each Minecraft version |

---

## Multi-Version Support with Stonecutter

### What is Stonecutter?

[Stonecutter](https://stonecutter.kikugie.dev/) is a Gradle plugin that enables multi-version Minecraft mod development from a single codebase. Instead of maintaining separate branches for each Minecraft version, Stonecutter uses preprocessing directives to conditionally include or exclude code blocks.

### How LinguaChat Uses Stonecutter

The project supports **8 Minecraft versions** from a single source tree:
- 1.16.5, 1.18.2, 1.19.2, 1.20.1, 1.21.1, 1.21.4, 1.21.5, 1.21.11

### Stonecutter Directive Syntax

Directives are comment-based preprocessing instructions:

**Basic version block:**
```java
//? if >=1.19 {
import net.minecraft.text.Text;
//?} else {
/*import net.minecraft.text.LiteralText;
*///?}
```

**Elif chains:**
```java
//? if >=1.20 {
return Text.literal(text);
//?} elif >=1.19 {
/*return Text.of(text);
*///?} else {
/*return new LiteralText(text);
*///?}
```

**Nested blocks:**
```java
//? if >=1.19 {
public void method() {
    //? if >=1.20 {
    useNewAPI();
    //?} else {
    /*useOldAPI();
    *///?}
}
//?}
```

**Single-line condition:**
```java
//? if >=1.19
import net.minecraft.text.Text;
```

### Supported Operators

| Operator | Meaning |
|----------|---------|
| `>=` | Greater than or equal |
| `<=` | Less than or equal |
| `>` | Greater than |
| `<` | Less than |
| `==` | Equal to |
| `!=` | Not equal to |

### Example Usage in ConfigScreen.java

```java
//? if >=1.21.11 {
this.sourceLangField = new TextFieldWidget(this.textRenderer, fieldX, currentY, FIELD_WIDTH, ROW_HEIGHT, this.sourceLangField, TextCompat.literal("Source Lang"));
//?} else {
/*this.sourceLangField = new TextFieldWidget(this.textRenderer, fieldX, currentY, FIELD_WIDTH, ROW_HEIGHT, TextCompat.literal("Source Lang"));
*///?}
```

This handles the TextFieldWidget constructor signature change in 1.21.11.

### Switching Active Version

To switch the active version for IDE development:

```bash
./gradlew switchVersion -PtargetVersion=1.21.4
```

Available versions: `1.16.5`, `1.18.2`, `1.19.2`, `1.20.1`, `1.21.1`, `1.21.4`, `1.21.5`, `1.21.11`

After switching, refresh your IDE:
- IntelliJ IDEA: File → Reload All Gradle Projects
- Eclipse: Right-click → Gradle → Refresh Gradle Project

---

## Compatibility Layers

LinguaChat provides compatibility wrappers to abstract version-specific API differences.

### TextCompat

**Problem:** `Text.literal()` (1.19+) vs `new LiteralText()` (1.16-1.18)

**Solution:**
```java
import com.linguachat.compat.TextCompat;

Text text = TextCompat.literal("Hello");
Text translated = TextCompat.translatable("key.linguachat.message");
```

### ScreenCompat

**Problem:** `client.setScreen()` (1.18+) vs `client.openScreen()` (1.16-1.17), `DrawContext` (1.20+) vs `MatrixStack` (1.16-1.19)

**Solution:**
```java
import com.linguachat.compat.ScreenCompat;

// Screen navigation
ScreenCompat.setScreen(client, new ConfigScreen(parent));

// Rendering (1.20+)
public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    ScreenCompat.drawCenteredText(context, textRenderer, text, x, y, color);
}
```

### ButtonCompat

**Problem:** `ButtonWidget.builder()` (1.19+) vs `new ButtonWidget()` (1.16-1.18)

**Solution:**
```java
import com.linguachat.compat.ButtonCompat;

ButtonWidget button = ButtonCompat.create(x, y, width, height, text, onPress);
```

### KeyBindingCompat

**Problem:** KeyBinding category API changed in 1.21.9+

**Solution:**
```java
import com.linguachat.compat.KeyBindingCompat;

KeyBinding key = KeyBindingCompat.register(new KeyBinding(
    "key.linguachat.open_config",
    InputUtil.Type.KEYSYM,
    GLFW.GLFW_KEY_K,
    "category.linguachat"
));
```

### I18nCompat

**Problem:** `Text.translatable()` (1.19+) vs `new TranslatableText()` (1.16-1.18)

**Solution:**
```java
import com.linguachat.compat.I18nCompat;

// Translate string
String text = I18nCompat.translate("linguachat.config.title");

// With arguments
String formatted = I18nCompat.translate("linguachat.config.enabled", "Enabled");

// Create translatable text component
MutableText label = I18nCompat.translatableText("linguachat.hover.original", "Hello");
```

---

## Building the Project

### Requirements

- **JDK 21+** for building all versions
- **Git** for cloning
- Internet connection for dependencies

### ⚠️ Important: Java for Running Gradle

Fabric Loom 1.14.10 requires **JVM 21+** to run Gradle itself. If you have multiple Java versions installed, ensure Gradle uses Java 21:

**Linux/macOS:**
```bash
export JAVA_HOME=/path/to/java-21
./gradlew build
```

**Windows (PowerShell):**
```powershell
$env:JAVA_HOME="C:\path\to\java-21"
.\gradlew.bat build
```

Or run in a single command:
```bash
JAVA_HOME=/path/to/java-21 ./gradlew build
```

> **Note:** Gradle automatically detects JDK 8 and 17 via toolchain for compiling older Minecraft versions. You only need to ensure Gradle itself runs on Java 21+.

### Clone Repository

```bash
git clone https://github.com/Lintech-1/LinguaChat.git
cd LinguaChat
```

### Build All Versions

```bash
./gradlew build
```

### Build Single Version

```bash
./gradlew 1.21.11:build
```

### Output Location

Built JARs are located at:
```
versions/<mc-version>/build/libs/linguachat-<mc-version>-2.0.0.jar
```

### Useful Gradle Flags

| Flag | Purpose |
|------|---------|
| `--no-daemon` | Run without Gradle daemon (CI/CD) |
| `--refresh-dependencies` | Force dependency refresh |
| `--stacktrace` | Show full stack trace on errors |
| `--info` | Verbose build output |

### Clean Build Artifacts

```bash
# All versions
./gradlew clean

# Specific version
./gradlew 1.21.11:clean
```

---

## Adding or Removing a Minecraft Version

### Adding a New Version

1. **Add to `settings.gradle`:**
   ```groovy
   stonecutter {
       create(rootProject) {
           versions "1.16.5", "1.18.2", ..., "1.21.11", "1.22"  // Add new version
       }
   }
   ```

2. **Add dependencies in `build.gradle`:**
   ```groovy
   if (sc.current.parsed >= "1.22") {
       mappings "net.fabricmc:yarn:1.22+build.X:v2"
       modImplementation "net.fabricmc.fabric-api:fabric-api:0.XX.X+1.22"
   }
   ```

3. **Add version-specific code blocks** using Stonecutter directives where needed

4. **Test compatibility layers** with the new version

5. **Update documentation** with the new version

---

## Public API

LinguaChat exposes APIs for other mods to use translation functionality.

### Get TranslationManager

```java
import com.linguachat.LinguaChatMod;
import com.linguachat.translation.TranslationManager;

TranslationManager manager = LinguaChatMod.getTranslationManager();
```

### Translate Text (Synchronous)

```java
import com.linguachat.translation.TranslationDirection;
import net.minecraft.text.Text;

Text original = Text.literal("Hello, world!");
Text translated = manager.translate(
    original,
    TranslationDirection.SERVER_TO_CLIENT
);
```

### Translate Async with Callback

```java
manager.translateAsync(
    original,
    TranslationDirection.SERVER_TO_CLIENT,
    translatedText -> {
        // Called on main thread when complete
        System.out.println("Translated: " + translatedText.getString());
    }
);
```

### TranslationDirection

| Direction | Purpose |
|-----------|---------|
| `SERVER_TO_CLIENT` | Incoming messages (server → client) |
| `CLIENT_TO_SERVER` | Outgoing messages (client → server) |

### MessageStore API

```java
import com.linguachat.translation.MessageStore;

// Store original
MessageStore.storeOriginalMessage("player:message", "Original text");

// Retrieve
String original = MessageStore.getOriginalMessage("player:message");

// Link original and translated
MessageStore.linkMessages("player", "Hello", "Привет");

// Check if linked
boolean linked = MessageStore.isLinkedMessage("player", "Привет");
```

### Configuration API

```java
import com.linguachat.config.ModConfig;

ModConfig config = ModConfig.get();

// Read
boolean enabled = config.isEnabled();
String targetLang = config.getDefaultTargetLang();

// Modify (auto-saves)
config.setTranslateIncoming(false);
config.setDefaultTargetLang("ru");
```

---

## Localization

### Language Files

Location: `src/main/resources/assets/linguachat/lang/`

Files:
- `en_us.json` — English (primary, always available)
- `ru_ru.json` — Russian (full translation)

### Key Structure

```
linguachat.<category>.<key>

Categories:
- config  — Configuration screen text
- log     — Log messages
- hover   — Hover tooltip text
- key     — Keybinding names
```

### Adding a New Language

1. Create `assets/linguachat/lang/<lang>_<country>.json`
2. Copy all keys from `en_us.json`
3. Translate values
4. Test in-game

Example (`fr_fr.json`):
```json
{
  "linguachat.config.title": "Paramètres LinguaChat",
  "linguachat.config.enabled": "Mod: %s",
  "linguachat.config.enabled.on": "§aActivé",
  "linguachat.hover.original": "Original: %s"
}
```

### Using I18nCompat in Code

```java
import com.linguachat.compat.I18nCompat;

// Translate
String text = I18nCompat.translate("linguachat.config.title");

// With arguments
String status = I18nCompat.translate("linguachat.config.enabled", "Enabled");

// Check existence
boolean hasKey = I18nCompat.hasTranslation("linguachat.config.title");
```

### Fallback Behavior

When a key is missing in the current language:
1. Falls back to `en_us.json`
2. Logs warning once (avoids spam)
3. Mod continues normally

---

[↑ Back to Top](#table-of-contents)
