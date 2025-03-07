# LinguaChat

A minecraft mod for automatic chat message translation between players using Google Translate and DeepL.

> [!NOTE]
> LinguaChat makes your communication in Minecraft accessible in any language, automatically translating chat messages without interrupting gameplay!

## 🌟 Features

- [x] Real-time message translation
- [x] Support for two translation services: Google Translate and DeepL
- [x] Customizable source and target languages
- [x] Separate settings for incoming and outgoing messages
- [x] Preservation of original text (available on hover)
- [x] Automatic language detection
- [x] Tooltip support for viewing original text

## 📥 Installation

1. Make sure you have Minecraft with Fabric Loader 0.16.10 or newer for game version 1.21.4
2. Download the latest version of the mod from the releases section
3. Place the JAR file in the `mods` folder of your Minecraft client
4. Launch Minecraft with the Fabric profile

## ⚙️ Configuration

The mod can be configured through the `linguachat.json` file in the `config` directory of your Minecraft client. Available settings:

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

### Settings Description:

| Parameter | Description |
| --- | --- |
| `enabled` | Enable/disable mod features |
| `translateIncoming` | Whether to translate incoming messages from other players |
| `translateOutgoing` | Whether to translate outgoing messages to a common language (usually English) |
| `defaultSourceLang` | Default language for outgoing messages (recommended "auto" for auto-detection) |
| `defaultTargetLang` | Language for translating incoming messages |
| `preferredTranslator` | Preferred translation service ("google" or "deepl") |
| `deeplApiKey` | Your API key for DeepL (if you use DeepL) |

### Language Codes

Commonly used language codes:
- English: `en` (or `en-US` for American English, `en-GB` for British English)
- Russian: `ru`
- Spanish: `es`
- French: `fr`
- German: `de`
- Italian: `it`
- Japanese: `ja`
- Chinese (Simplified): `zh`

## 🔄 Translation Services

### Google Translate
- No API key required
- Supports a wide range of languages
- Free to use (with some limitations)
- Used by default

### DeepL
- Requires an API key
- Usually provides higher quality translations
- Supports fewer languages but with better accuracy
- Get your API key at [DeepL website](https://www.deepl.com/pro-api)

## 📝 Usage Example

When properly configured, the mod automatically translates messages:

```
Player1: привет
[Chat for you]: hello

Player2: hello
[Chat for you]: привет
```

> [!TIP]
> The original message text is available when hovering over the translated message!

## 🖥️ System Requirements

- Minecraft 1.21.4
- Fabric Loader 0.16.10 or newer
- Java 17 or higher
- Internet connection for translation services
- DeepL API key (optional, only if you use DeepL)

## 🛠️ Troubleshooting

> [!IMPORTANT]
> If you encounter problems with the mod, check the following points:

1. If translation doesn't work:
   - [ ] Check your internet connection
   - [ ] Make sure your DeepL API key is correct (if using DeepL)
   - [ ] Check game logs for error messages

2. If messages aren't being translated:
   - [ ] Make sure language codes in the configuration file are set correctly
   - [ ] Verify that the translation service is properly configured
   - [ ] Check that the translation direction is set correctly

3. DeepL API problems:
   - [ ] In settings, use `"defaultSourceLang": "auto"` for language auto-detection
   - [ ] Check that your API key is valid and hasn't expired
   - [ ] Be aware of the limitations of the free DeepL plan

> [!WARNING]
> When using older versions of the DeepL API, errors may occur with the `source_lang` parameter when set to "auto". We recommend updating to the latest version of the mod.

### Log Checking

Check logs for messages:
- `!!! MIXIN ChatHudMixin LOADED !!!` - confirms that the mixin is loaded
- `Converting language for DeepL` - tracks language code conversion
- `DeepL translation: ... -> ...` - confirms successful translation via DeepL

### Resolved Issues:

- Fixed an issue with DeepL API where the `source_lang` parameter with "auto" value caused an error
- Updated DeepL API library to version 1.9.0
- Improved error handling and logging
- Added additional language aliases for more convenient use

## 📢 Support

If you have problems or questions:
- Check the issues section on GitHub
- Create a new issue with detailed information about your problem
- Attach your configuration file and latest.log when reporting issues

---

*Made with ❤️ for the Minecraft community. Happy gaming and pleasant communication in any language!* :video_game: :speech_balloon:

<div align="right">
<a href="README-RU.md">Русская версия</a>
</div> 
