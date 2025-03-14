<img alt="Icon" width=100 src="https://raw.githubusercontent.com/Lintech-1/LinguaChat/refs/heads/main/src/main/resources/assets/linguachat/linguachat.png">

# LinguaChat

Мод майнкрафт для автоматического перевода сообщений в чате между игроками с использованием Google Translate и DeepL.

> [!NOTE]
> LinguaChat сделает ваше общение в Minecraft доступным на любом языке, автоматически переводя сообщения в чате без прерывания игрового процесса!

[![Loader](https://img.shields.io/badge/Доступно%20на-Fabric-dbd0b4?logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABoAAAAcBAMAAACNPbLgAAABhGlDQ1BJQ0MgcHJvZmlsZQAAKJF9kT1Iw0AcxV9TpX5UHMwgIpihOtlFRRxLFYtgobQVWnUwufQLmjQkKS6OgmvBwY/FqoOLs64OroIg+AHi6uKk6CIl/i8ptIj14Lgf7+497t4BQr3MNKsrAmi6bSZjUSmTXZUCr+iHiADG0Cszy4inFtPoOL7u4ePrXZhndT735xhQcxYDfBJxhBmmTbxBPLtpG5z3iUVWlFXic+JJky5I/Mh1xeM3zgWXBZ4pmunkPLFILBXaWGljVjQ14hnikKrplC9kPFY5b3HWylXWvCd/YTCnr6S4TnMUMSwhjgQkKKiihDJshGnVSbGQpP1oB/+I60+QSyFXCYwcC6hAg+z6wf/gd7dWfnrKSwpGge4Xx/kYBwK7QKPmON/HjtM4AfzPwJXe8lfqwNwn6bWWFjoCBreBi+uWpuwBlzvA8JMhm7Ir+WkK+TzwfkbflAWGboG+Na+35j5OH4A0dbV8AxwcAhMFyl7v8O6e9t7+PdPs7wd+dXKrd9SjeQAAAAlwSFlzAAAuIwAALiMBeKU/dgAAAAd0SU1FB+cLFAcgIbOcUjoAAAAbUExURQAAAB0tQTg0KoB6bZqSfq6mlLyynMa8pdvQtJRJT6UAAAABdFJOUwBA5thmAAAAAWJLR0QB/wIt3gAAAF5JREFUGNN10FENwCAMhOFqOQuzMAtYOAtYqGw6mkEvhL59yR9Ca5YDqyOC465eKYqQm6LoCkVwnwQOBYKdeA5l51zhFtrsnPmg6m3Z2akk15dFH1lWFQVxlUFv+2sAJlA9O7NwQRQAAAAASUVORK5CYII=)](https://fabricmc.net/)

[![Скачать на CurseForge](https://img.shields.io/curseforge/dt/1215804?label=Скачать%20на%20CurseForge&logo=curseforge)](https://www.curseforge.com/minecraft/mc-mods/linguachat)
[![Скачать на Modrinth](https://img.shields.io/modrinth/dt/linguachat?label=Скачать%20на%20Modrinth&logo=modrinth&logoColor=%2300AF5C)](https://modrinth.com/mod/linguachat)

## 🌟 Возможности

- [x] Перевод сообщений в реальном времени
- [x] Поддержка двух сервисов перевода: Google Translate и DeepL
- [x] Настраиваемые исходный и целевой языки
- [x] Раздельные настройки для входящих и исходящих сообщений
- [x] Сохранение оригинального текста (доступен при наведении курсора)
- [x] Автоматическое определение языка сообщений
- [x] Поддержка всплывающих подсказок для просмотра оригинального текста

## 📥 Установка

1. Убедитесь, что у вас установлен Minecraft с Fabric Loader 0.16.10 или новее для версии игры 1.21.4
2. Скачайте последнюю версию мода из раздела релизов
3. Поместите JAR-файл в папку `mods` вашего клиента Minecraft
4. Запустите Minecraft с профилем Fabric

## ⚙️ Конфигурация

Мод можно настроить через файл `linguachat.json` в директории `config` вашего клиента Minecraft. Доступные настройки:

```json
{
    "enabled": true,
    "translateIncoming": true,
    "translateOutgoing": true,
    "defaultSourceLang": "auto",
    "defaultTargetLang": "ru",
    "preferredTranslator": "google",
    "deeplApiKey": ""
}
```

### Описание настроек:

| Параметр | Описание |
| --- | --- |
| `enabled` | Включение/отключение функций мода |
| `translateIncoming` | Переводить ли входящие сообщения от других игроков |
| `translateOutgoing` | Переводить ли исходящие сообщения на общий язык (обычно английский) |
| `defaultSourceLang` | Язык по умолчанию для исходящих сообщений (рекомендуется "auto" для автоопределения) |
| `defaultTargetLang` | Язык для перевода входящих сообщений |
| `preferredTranslator` | Предпочитаемая служба перевода ("google" или "deepl") |
| `deeplApiKey` | Ваш ключ API для DeepL (если вы используете DeepL) |

### Коды языков

Часто используемые коды языков:
- Английский: `en` (или `en-US` для американского английского, `en-GB` для британского)
- Русский: `ru`
- Испанский: `es`
- Французский: `fr`
- Немецкий: `de`
- Итальянский: `it`
- Японский: `ja`
- Китайский (упрощенный): `zh`

## 🔄 Сервисы перевода

### Google Translate
- Не требует API ключа
- Поддерживает широкий спектр языков
- Бесплатный для использования (с некоторыми ограничениями)
- Используется по умолчанию

### DeepL
- Требует API ключ
- Обычно обеспечивает более качественный перевод
- Поддерживает меньше языков, но с лучшей точностью
- Получите ключ API на [сайте DeepL](https://www.deepl.com/pro-api)

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

## 🖥️ Системные требования

- Minecraft 1.21.4
- Fabric Loader 0.16.10 или новее
- Java 17 или выше
- Интернет-соединение для сервисов перевода
- API ключ DeepL (опционально, только если используете DeepL)

## 🛠️ Устранение неполадок

> [!IMPORTANT]
> Если у вас возникли проблемы с работой мода, проверьте следующие моменты:

1. Если перевод не работает:
   - [ ] Проверьте подключение к интернету
   - [ ] Убедитесь в правильности API ключа DeepL (если используете DeepL)
   - [ ] Проверьте логи игры на наличие сообщений об ошибках

2. Если сообщения не переводятся:
   - [ ] Убедитесь, что коды языков в конфигурационном файле указаны правильно
   - [ ] Проверьте, что сервис перевода настроен корректно
   - [ ] Убедитесь, что направление перевода настроено правильно

3. Проблемы с DeepL API:
   - [ ] В настройках используйте `"defaultSourceLang": "auto"` для автоопределения языка
   - [ ] Проверьте, что ваш API ключ действителен и не истек
   - [ ] Обратите внимание на ограничения бесплатного плана DeepL

> [!WARNING]
> При использовании устаревших версий DeepL API могут возникать ошибки с параметром `source_lang` при значении "auto". Мы рекомендуем обновиться до последней версии мода.

### Проверка логов

Проверьте логи для сообщений (при условии включенном `"debugMode": true` в конфигурационном файле `linguachat.json`):
- `!!! МИКСИН ChatHudMixin ЗАГРУЖЕН !!!` - подтверждает, что миксин загружен
- `Преобразование языка для DeepL` - отслеживает преобразование кодов языков
- `DeepL перевод: ... -> ...` - подтверждает успешный перевод через DeepL

### Решенные проблемы:

- Исправлена проблема с DeepL API, когда параметр `source_lang` со значением "auto" вызывал ошибку
- Обновлена библиотека DeepL API до версии 1.9.0
- Улучшена обработка ошибок и логирование
- Добавлены дополнительные алиасы языков для более удобного использования

## 📢 Поддержка

Если у вас возникли проблемы или вопросы:
- Проверьте раздел issues на GitHub
- Создайте новый issue с подробной информацией о вашей проблеме
- Прикрепите ваш конфигурационный файл и latest.log при сообщении о проблемах

---

*Сделано с ❤️ для сообщества Minecraft. Удачной игры и приятного общения на любом языке!* :video_game: :speech_balloon: 
