<div align="center">
  <img src="https://raw.githubusercontent.com/Lintech-1/LinguaChat/refs/heads/main/src/main/resources/assets/linguachat/linguachat.png" alt="LinguaChat Logo" width="100">
  <h1>LinguaChat</h1>
  <p>Автоматический перевод чат-сообщений между игроками в Minecraft</p>

[![Loader](https://img.shields.io/badge/Доступно%20на-Fabric-dbd0b4?logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABoAAAAcBAMAAACNPbLgAAABhGlDQ1BJQ0MgcHJvZmlsZQAAKJF9kT1Iw0AcxV9TpX5UHMwgIpihOtlFRRxLFYtgobQVWnUwufQLmjQkKS6OgmvBwY/FqoOLs64OroIg+AHi6uKk6CIl/i8ptIj14Lgf7+497t4BQr3MNKsrAmi6bSZjUSmTXZUCr+iHiADG0Cszy4inFtPoOL7u4ePrXZhndT735xhQcxYDfBJxhBmmTbxBPLtpG5z3iUVWlFXic+JJky5I/Mh1xeM3zgWXBZ4pmunkPLFILBXaWGljVjQ14hnikKrplC9kPFY5b3HWylXWvCd/YTCnr6S4TnMUMSwhjgQkKKiihDJshGnVSbGQpP1oB/+I60+QSyFXCYwcC6hAg+z6wf/gd7dWfnrKSwpGge4Xx/kYBwK7QKPmON/HjtM4AfzPwJXe8lfqwNwn6bWWFjoCBreBi+uWpuwBlzvA8JMhm7Ir+WkK+TzwfkbflAWGboG+Na+35j5OH4A0dbV8AxwcAhMFyl7v8O6e9t7+PdPs7wd+dXKrd9SjeQAAAAlwSFlzAAAuIwAALiMBeKU/dgAAAAd0SU1FB+cLFAcgIbOcUjoAAAAbUExURQAAAB0tQTg0KoB6bZqSfq6mlLyynMa8pdvQtJRJT6UAAAABdFJOUwBA5thmAAAAAWJLR0QB/wIt3gAAAF5JREFUGNN10FENwCAMhOFqOQuzMAtYOAtYqGw6mkEvhL59yR9Ca5YDqyOC465eKYqQm6LoCkVwnwQOBYKdeA5l51zhFtrsnPmg6m3Z2akk15dFH1lWFQVxlUFv+2sAJlA9O7NwQRQAAAAASUVORK5CYII=)](https://fabricmc.net/)
[![Скачать на CurseForge](https://img.shields.io/curseforge/dt/1215804?label=Скачать%20на%20CurseForge&logo=curseforge)](https://www.curseforge.com/minecraft/mc-mods/linguachat)
[![Скачать на Modrinth](https://img.shields.io/modrinth/dt/linguachat?label=Скачать%20на%20Modrinth&logo=modrinth&logoColor=%2300AF5C)](https://modrinth.com/mod/linguachat)

**🇺🇸 [English Version](README.md)**

</div>

---

## 🌟 Возможности

- [x] Перевод сообщений в реальном времени
- [x] Три провайдера: Google Translate (бесплатно), DeepL, Kagi
- [x] Настраиваемые исходный и целевой языки
- [x] Раздельные настройки для входящих и исходящих сообщений
- [x] Оригинал сообщения доступен при наведении курсора
- [x] Автоопределение языка сообщения
- [x] Полная локализация интерфейса (английский и русский)
- [x] Поддержка Minecraft 1.16.5 — 1.21.11

---

## 📥 Установка

### Требования

| Minecraft | Java | Fabric Loader | Fabric API |
|-----------|------|---------------|------------|
| 1.16.5    | 8+   | 0.16.10+      | 0.42.0+    |
| 1.18.2    | 17+  | 0.16.10+      | 0.76.0+    |
| 1.19.2    | 17+  | 0.16.10+      | 0.76.1+    |
| 1.20.1    | 17+  | 0.16.10+      | 0.92.2+    |
| 1.21.1    | 21+  | 0.16.10+      | 0.102.0+   |
| 1.21.4    | 21+  | 0.16.10+      | 0.99.5+    |
| 1.21.5    | 21+  | 0.16.10+      | 0.122.0+   |
| 1.21.11   | 21+  | 0.16.10+      | 0.139.5+   |

### Шаги установки

1. Установите **Fabric Loader** для вашей версии Minecraft
2. Установите **Fabric API** подходящей версии
3. Скачайте LinguaChat для вашей версии

**Для официального лаунчера Minecraft:**

1. Поместите JAR в папку `mods`:
   - Windows: `%APPDATA%\.minecraft\mods`
   - Linux: `~/.minecraft/mods`
   - macOS: `~/Library/Application Support/minecraft/mods`

**Для Prism Launcher и других лаунчеров на основе инстансов:**

1. Откройте настройки инстанса → вкладка **Mods**
2. Нажмите **Add** / **Install Mods** и выберите JAR-файл
3. Убедитесь, что Fabric API установлен для этого инстанса

**Завершающий шаг:**

4. Запустите Minecraft с профилем Fabric

---

## 📝 Пример использования

При правильной настройке мод автоматически переводит сообщения:

```
Игрок1: привет
[Чат для вас]: hello

Игрок2: hello
[Чат для вас]: привет
```

> [!TIP]
> Оригинальный текст сообщения доступен при наведении курсора на переведенное сообщение!

---

## ⚙️ Конфигурация

Настройки мода доступны клавишей **K** в игре.

Также можно редактировать файл напрямую: `config/linguachat.json`

### Клавиши

| Клавиша | Действие |
|---------|----------|
| **K** | Открыть экран настроек |
| **N** | Включить/выключить перевод исходящих сообщений |

### Настройки

| Параметр | По умолчанию | Описание |
|----------|--------------|----------|
| `enabled` | `true` | Включить/выключить мод |
| `translateIncoming` | `true` | Переводить входящие сообщения |
| `translateOutgoing` | `true` | Переводить исходящие сообщения |
| `defaultSourceLang` | `"auto"` | Язык исходящих (`auto` для определения) |
| `defaultTargetLang` | `"en"` | Язык для входящих сообщений |
| `preferredTranslator` | `"google"` | Провайдер: `google`, `deepl`, `kagi` |
| `deeplApiKey` | `""` | API ключ DeepL |
| `kagiApiKey` | `""` | API токен Kagi |
| `kagiSessionToken` | `""` | Session token Kagi |
| `showOriginalOnHover` | `true` | Показывать оригинал при наведении |
| `cacheSize` | `512` | Размер кэша переводов |
| `debugMode` | `false` | Отладочное логирование |

### Провайдеры

| Провайдер | Стоимость | API ключ | Языки |
|-----------|-----------|----------|-------|
| **Google** | Бесплатно | Не требуется | 100+ |
| **DeepL** | Free/Paid | [Требуется](https://www.deepl.com/pro-api) | 30+ |
| **Kagi** | Kagi Pass | API токен ([settings/api](https://kagi.com/settings/api)) или Session token ([settings/user_details](https://kagi.com/settings/user_details)) | Множество |

---

## 🐛 Сообщение о проблемах

Нашли баг или есть предложение? Создайте issue:

👉 **https://github.com/Lintech-1/LinguaChat/issues**

Приложите:
- Версию Minecraft
- Версию мода
- Фрагменты логов
- Шаги для воспроизведения

---

## 📚 Для разработчиков

Полная документация для разработчиков, включая информацию о мульти-версионной разработке с Stonecutter, compatibility layers и публичном API, доступна в [`docs/en.md`](docs/en.md) (English) и [`docs/ru.md`](docs/ru.md) (Русский).

---

<div align="center">

*Сделано с ❤️ для сообщества Minecraft. Удачной игры и понятного общения на любом языке!*

</div>
