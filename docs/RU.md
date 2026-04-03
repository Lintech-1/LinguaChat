# LinguaChat — Документация для разработчиков

## Содержание

- [Структура проекта](#структура-проекта)
- [Мульти-версионная поддержка с Stonecutter](#мульти-версионная-поддержка-s-stonecutter)
- [Слои совместимости](#слои-совместимости)
- [Сборка проекта](#сборка-проекта)
- [Добавление или удаление версии Minecraft](#добавление-или-удаление-версии-minecraft)
- [Публичный API](#публичный-api)
- [Локализация](#локализация)

---

## Структура проекта

```
LinguaChat/
├── versions/                 # Версионно-специфичные сборки (генерируются)
│   ├── 1.16.5/
│   ├── 1.18.2/
│   ├── 1.19.2/
│   ├── 1.20.1/
│   ├── 1.21.1/
│   ├── 1.21.4/
│   ├── 1.21.5/
│   └── 1.21.11/
├── src/                      # Общий исходный код (все версии)
│   └── main/
│       ├── java/com/linguachat/
│       │   ├── compat/       # Слои совместимости для кросс-версионной поддержки
│       │   │   ├── ButtonCompat.java
│       │   │   ├── I18nCompat.java
│       │   │   ├── KeyBindingCompat.java
│       │   │   ├── ScreenCompat.java
│       │   │   └── TextCompat.java
│       │   ├── config/       # Обработка конфигурации
│       │   │   ├── ModConfig.java
│       │   │   └── ConfigValidator.java
│       │   ├── event/        # Обработчики событий
│       │   │   └── ChatEvents.java
│       │   ├── gui/          # GUI экраны
│       │   │   └── ConfigScreen.java
│       │   ├── integration/  # Интеграции с модами
│       │   │   └── ModMenuIntegration.java
│       │   ├── keybind/      # Обработка клавиш
│       │   │   ├── ModKeybinds.java
│       │   │   └── KeybindHandler.java
│       │   ├── mixin/        # Конфигурации миксинов
│       │   │   └── client/
│       │   │       ├── ChatHudMixin.java
│       │   │       ├── ChatScreenMixin.java
│       │   │       ├── ClientPlayNetworkHandlerMixin.java
│       │   │       └── MessageHandlerMixin.java
│       │   ├── translation/  # Основная логика перевода
│       │   │   ├── providers/
│       │   │   │   ├── GoogleTranslateClient.java
│       │   │   │   ├── DeepLTranslateClient.java
│       │   │   │   └── KagiTranslateClient.java
│       │   │   ├── TranslationManager.java
│       │   │   ├── TranslationDirection.java
│       │   │   ├── TranslationCache.java
│       │   │   ├── TranslationLogger.java
│       │   │   └── MessageStore.java
│       │   ├── util/         # Утилиты
│       │   │   └── MessageBlocker.java
│       │   ├── version/      # Утилиты версий
│       │   └── LinguaChatMod.java
│       │   └── LinguaChatClient.java
│       └── resources/
│           ├── assets/linguachat/lang/
│           │   ├── en_us.json    # Переводы на английский
│           │   └── ru_ru.json    # Переводы на русский
│           ├── fabric.mod.json
│           └── linguachat.mixins.json
├── build.gradle              # Основной скрипт сборки
├── settings.gradle           # Конфигурация Stonecutter
├── stonecutter.gradle        # Селектор активной версии
└── docs/                     # Документация
    ├── EN.md
    └── RU.md
```

### Основные директории

| Директория | Назначение |
|------------|------------|
| `src/main/java/com/linguachat/compat/` | Слои абстракции для различий версий Minecraft |
| `src/main/java/com/linguachat/translation/` | Основная логика перевода и клиенты провайдеров |
| `src/main/java/com/linguachat/mixin/` | Миксины для перехвата ванильной обработки чата |
| `src/main/resources/assets/linguachat/lang/` | Файлы локализации для UI и логов |
| `versions/*/` | Сгенерированные сборки для каждой версии Minecraft |

---

## Мульти-версионная поддержка с Stonecutter

### Что такое Stonecutter?

[Stonecutter](https://stonecutter.kikugie.dev/) — это Gradle-плагин, который позволяет разрабатывать моды для нескольких версий Minecraft из единой кодовой базы. Вместо поддержки отдельных веток для каждой версии, Stonecutter использует директивы препроцессинга для условного включения или исключения блоков кода.

### Как LinguaChat использует Stonecutter

Проект поддерживает **8 версий Minecraft** из одного дерева исходников:
- 1.16.5, 1.18.2, 1.19.2, 1.20.1, 1.21.1, 1.21.4, 1.21.5, 1.21.11

### Синтаксис директив Stonecutter

Директивы — это инструкции препроцессинга на основе комментариев:

**Базовый блок версии:**
```java
//? if >=1.19 {
import net.minecraft.text.Text;
//?} else {
/*import net.minecraft.text.LiteralText;
*///?}
```

**Цепочки elif:**
```java
//? if >=1.20 {
return Text.literal(text);
//?} elif >=1.19 {
/*return Text.of(text);
*///?} else {
/*return new LiteralText(text);
*///?}
```

**Вложенные блоки:**
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

**Однострочное условие:**
```java
//? if >=1.19
import net.minecraft.text.Text;
```

### Поддерживаемые операторы

| Оператор | Значение |
|----------|----------|
| `>=` | Больше или равно |
| `<=` | Меньше или равно |
| `>` | Больше |
| `<` | Меньше |
| `==` | Равно |
| `!=` | Не равно |

### Пример использования в ConfigScreen.java

```java
//? if >=1.21.11 {
this.sourceLangField = new TextFieldWidget(this.textRenderer, fieldX, currentY, FIELD_WIDTH, ROW_HEIGHT, this.sourceLangField, TextCompat.literal("Source Lang"));
//?} else {
/*this.sourceLangField = new TextFieldWidget(this.textRenderer, fieldX, currentY, FIELD_WIDTH, ROW_HEIGHT, TextCompat.literal("Source Lang"));
*///?}
```

Это обрабатывает изменение сигнатуры конструктора TextFieldWidget в версии 1.21.11.

### Переключение активной версии

Для переключения активной версии в IDE:

```bash
./gradlew switchVersion -PtargetVersion=1.21.4
```

Доступные версии: `1.16.5`, `1.18.2`, `1.19.2`, `1.20.1`, `1.21.1`, `1.21.4`, `1.21.5`, `1.21.11`

После переключения обновите IDE:
- IntelliJ IDEA: File → Reload All Gradle Projects
- Eclipse: Правый клик → Gradle → Refresh Gradle Project

---

## Слои совместимости

LinguaChat предоставляет обёртки для абстрагирования различий API между версиями.

### TextCompat

**Проблема:** `Text.literal()` (1.19+) против `new LiteralText()` (1.16-1.18)

**Решение:**
```java
import com.linguachat.compat.TextCompat;

Text text = TextCompat.literal("Hello");
Text translated = TextCompat.translatable("key.linguachat.message");
```

### ScreenCompat

**Проблема:** `client.setScreen()` (1.18+) против `client.openScreen()` (1.16-1.17), `DrawContext` (1.20+) против `MatrixStack` (1.16-1.19)

**Решение:**
```java
import com.linguachat.compat.ScreenCompat;

// Навигация по экранам
ScreenCompat.setScreen(client, new ConfigScreen(parent));

// Рендеринг (1.20+)
public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    ScreenCompat.drawCenteredText(context, textRenderer, text, x, y, color);
}
```

### ButtonCompat

**Проблема:** `ButtonWidget.builder()` (1.19+) против `new ButtonWidget()` (1.16-1.18)

**Решение:**
```java
import com.linguachat.compat.ButtonCompat;

ButtonWidget button = ButtonCompat.create(x, y, width, height, text, onPress);
```

### KeyBindingCompat

**Проблема:** API категории KeyBinding изменился в 1.21.9+

**Решение:**
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

**Проблема:** `Text.translatable()` (1.19+) против `new TranslatableText()` (1.16-1.18)

**Решение:**
```java
import com.linguachat.compat.I18nCompat;

// Перевод строки
String text = I18nCompat.translate("linguachat.config.title");

// С аргументами
String formatted = I18nCompat.translate("linguachat.config.enabled", "Enabled");

// Создать текстовый компонент
MutableText label = I18nCompat.translatableText("linguachat.hover.original", "Hello");
```

---

## Сборка проекта

### Требования

- **JDK 21+** для сборки всех версий
- **Git** для клонирования
- Интернет-соединение для зависимостей

### ⚠️ Важно: Java для запуска Gradle

Fabric Loom 1.14.10 требует **JVM 21+** для запуска самого Gradle. Если у вас установлено несколько версий Java, убедитесь что Gradle использует Java 21:

**Linux/macOS:**
```bash
export JAVA_HOME=/путь/к/java-21
./gradlew build
```

**Windows (PowerShell):**
```powershell
$env:JAVA_HOME="C:\путь\к\java-21"
.\gradlew.bat build
```

Или запустите одной командой:
```bash
JAVA_HOME=/путь/к/java-21 ./gradlew build
```

> **Примечание:** Gradle автоматически определяет JDK 8 и 17 через toolchain для компиляции старых версий Minecraft. Вам нужно только убедиться что сам Gradle запускается на Java 21+.

### Клонирование репозитория

```bash
git clone https://github.com/Lintech-1/LinguaChat.git
cd LinguaChat
```

### Сборка всех версий

```bash
./gradlew build
```

### Сборка одной версии

```bash
./gradlew 1.21.11:build
```

### Расположение результатов

JAR-файлы находятся по адресу:
```
versions/<версия-mc>/build/libs/linguachat-<версия-mc>-2.0.0.jar
```

### Полезные флаги Gradle

| Флаг | Назначение |
|------|------------|
| `--no-daemon` | Запуск без демона Gradle (CI/CD) |
| `--refresh-dependencies` | Принудительное обновление зависимостей |
| `--stacktrace` | Показать полный стек вызовов при ошибках |
| `--info` | Подробный вывод сборки |

### Очистка артефактов сборки

```bash
# Все версии
./gradlew clean

# Конкретная версия
./gradlew 1.21.11:clean
```

---

## Добавление или удаление версии Minecraft

### Добавление новой версии

1. **Добавить в `settings.gradle`:**
   ```groovy
   stonecutter {
       create(rootProject) {
           versions "1.16.5", "1.18.2", ..., "1.21.11", "1.22"  // Добавить новую версию
       }
   }
   ```

2. **Добавить зависимости в `build.gradle`:**
   ```groovy
   if (sc.current.parsed >= "1.22") {
       mappings "net.fabricmc:yarn:1.22+build.X:v2"
       modImplementation "net.fabricmc.fabric-api:fabric-api:0.XX.X+1.22"
   }
   ```

3. **Добавить версионно-специфичные блоки кода** с помощью директив Stonecutter

4. **Протестировать слои совместимости** с новой версией

5. **Обновить документацию** с новой версией

---

## Публичный API

LinguaChat предоставляет API для использования функционала перевода другими модами.

### Получение TranslationManager

```java
import com.linguachat.LinguaChatMod;
import com.linguachat.translation.TranslationManager;

TranslationManager manager = LinguaChatMod.getTranslationManager();
```

### Перевод текста (синхронно)

```java
import com.linguachat.translation.TranslationDirection;
import net.minecraft.text.Text;

Text original = Text.literal("Hello, world!");
Text translated = manager.translate(
    original,
    TranslationDirection.SERVER_TO_CLIENT
);
```

### Асинхронный перевод с callback

```java
manager.translateAsync(
    original,
    TranslationDirection.SERVER_TO_CLIENT,
    translatedText -> {
        // Вызывается на главном потоке при завершении
        System.out.println("Переведено: " + translatedText.getString());
    }
);
```

### TranslationDirection

| Направление | Назначение |
|-------------|------------|
| `SERVER_TO_CLIENT` | Входящие сообщения (сервер → клиент) |
| `CLIENT_TO_SERVER` | Исходящие сообщения (клиент → сервер) |

### MessageStore API

```java
import com.linguachat.translation.MessageStore;

// Сохранить оригинал
MessageStore.storeOriginalMessage("player:message", "Original text");

// Получить обратно
String original = MessageStore.getOriginalMessage("player:message");

// Связать оригинал и перевод
MessageStore.linkMessages("player", "Hello", "Привет");

// Проверить связь
boolean linked = MessageStore.isLinkedMessage("player", "Привет");
```

### Configuration API

```java
import com.linguachat.config.ModConfig;

ModConfig config = ModConfig.get();

// Чтение
boolean enabled = config.isEnabled();
String targetLang = config.getDefaultTargetLang();

// Изменение (автосохранение)
config.setTranslateIncoming(false);
config.setDefaultTargetLang("ru");
```

---

## Локализация

### Файлы языков

Расположение: `src/main/resources/assets/linguachat/lang/`

Файлы:
- `en_us.json` — Английский (основной, всегда доступен)
- `ru_ru.json` — Русский (полный перевод)

### Структура ключей

```
linguachat.<категория>.<ключ>

Категории:
- config  — Текст экрана конфигурации
- log     — Сообщения логов
- hover   — Текст всплывающих подсказок
- key     — Названия клавиш
```

### Добавление нового языка

1. Создать `assets/linguachat/lang/<lang>_<страна>.json`
2. Скопировать все ключи из `en_us.json`
3. Перевести значения
4. Протестировать в игре

Пример (`fr_fr.json`):
```json
{
  "linguachat.config.title": "Paramètres LinguaChat",
  "linguachat.config.enabled": "Mod: %s",
  "linguachat.config.enabled.on": "§aActivé",
  "linguachat.hover.original": "Original: %s"
}
```

### Использование I18nCompat в коде

```java
import com.linguachat.compat.I18nCompat;

// Перевод
String text = I18nCompat.translate("linguachat.config.title");

// С аргументами
String status = I18nCompat.translate("linguachat.config.enabled", "Enabled");

// Проверить наличие
boolean hasKey = I18nCompat.hasTranslation("linguachat.config.title");
```

### Логика fallback

Когда ключ отсутствует в текущем языке:
1. Возврат к `en_us.json`
2. Предупреждение в лог один раз (избегает спама)
3. Мод продолжает работать нормально

---

[↑ Наверх](#содержание)
