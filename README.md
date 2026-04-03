<div align="center">
  <img src="https://raw.githubusercontent.com/Lintech-1/LinguaChat/refs/heads/main/src/main/resources/assets/linguachat/linguachat.png" alt="LinguaChat Logo" width="100">
  <h1>LinguaChat</h1>
  <p>Automatic chat message translation between Minecraft players</p>

[![Loader](https://img.shields.io/badge/Available%20for-Fabric-dbd0b4?logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABoAAAAcBAMAAACNPbLgAAABhGlDQ1BJQ0MgcHJvZmlsZQAAKJF9kT1Iw0AcxV9TpX5UHMwgIpihOtlFRRxLFYtgobQVWnUwufQLmjQkKS6OgmvBwY/FqoOLs64OroIg+AHi6uKk6CIl/i8ptIj14Lgf7+497t4BQr3MNKsrAmi6bSZjUSmTXZUCr+iHiADG0Cszy4inFtPoOL7u4ePrXZhndT735xhQcxYDfBJxhBmmTbxBPLtpG5z3iUVWlFXic+JJky5I/Mh1xeM3zgWXBZ4pmunkPLFILBXaWGljVjQ14hnikKrplC9kPFY5b3HWylXWvCd/YTCnr6S4TnMUMSwhjgQkKKiihDJshGnVSbGQpP1oB/+I60+QSyFXCYwcC6hAg+z6wf/gd7dWfnrKSwpGge4Xx/kYBwK7QKPmON/HjtM4AfzPwJXe8lfqwNwn6bWWFjoCBreBi+uWpuwBlzvA8JMhm7Ir+WkK+TzwfkbflAWGboG+Na+35j5OH4A0dbV8AxwcAhMFyl7v8O6e9t7+PdPs7wd+dXKrd9SjeQAAAAlwSFlzAAAuIwAALiMBeKU/dgAAAAd0SU1FB+cLFAcgIbOcUjoAAAAbUExURQAAAB0tQTg0KoB6bZqSfq6mlLyynMa8pdvQtJRJT6UAAAABdFJOUwBA5thmAAAAAWJLR0QB/wIt3gAAAF5JREFUGNN10FENwCAMhOFqOQuzMAtYOAtYqGw6mkEvhL59yR9Ca5YDqyOC465eKYqQm6LoCkVwnwQOBYKdeA5l51zhFtrsnPmg6m3Z2akk15dFH1lWFQVxlUFv+2sAJlA9O7NwQRQAAAAASUVORK5CYII=)](https://fabricmc.net/)
[![Download on CurseForge](https://img.shields.io/curseforge/dt/1215804?label=Download%20on%20CurseForge&logo=curseforge)](https://www.curseforge.com/minecraft/mc-mods/linguachat)
[![Download on Modrinth](https://img.shields.io/modrinth/dt/linguachat?label=Download%20on%20Modrinth&logo=modrinth&logoColor=%2300AF5C)](https://modrinth.com/mod/linguachat)

**🇷🇺 [Русская версия](README-RU.md)**

</div>

---

## 🌟 Features

- [x] Real-time message translation
- [x] Three translation providers: Google Translate (free), DeepL, Kagi
- [x] Customizable source and target languages
- [x] Separate settings for incoming and outgoing messages
- [x] Original text available on hover
- [x] Automatic language detection
- [x] Full UI localization (English and Russian)
- [x] Supports Minecraft 1.16.5 — 1.21.11

---

## 📥 Installation

### Requirements

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

### Installation Steps

1. Install **Fabric Loader** for your Minecraft version
2. Install **Fabric API** matching your version
3. Download LinguaChat for your version

**For official Minecraft Launcher:**

1. Place the JAR file in your `mods` folder:
   - Windows: `%APPDATA%\.minecraft\mods`
   - Linux: `~/.minecraft/mods`
   - macOS: `~/Library/Application Support/minecraft/mods`

**For Prism Launcher and other instance-based launchers:**

1. Open instance settings → **Mods** tab
2. Click **Add** / **Install Mods** and select the JAR file
3. Make sure Fabric API is installed for this instance

**Final step:**

4. Launch Minecraft with Fabric profile

---

## 📝 Usage Example

When properly configured, the mod automatically translates messages:

```
Player1: привет
[Chat for you]: hello

Player2: hello
[Chat for you]: привет
```

> [!TIP]
> The original text of the message is available by hovering over the translated message!

---

## ⚙️ Configuration

Mod settings are accessible via the **K** key in-game.

You can also edit the file directly: `config/linguachat.json`

### Keybinds

| Key | Action |
|-----|--------|
| **K** | Open settings screen |
| **N** | Toggle outgoing message translation |

### Settings

| Parameter | Default | Description |
|-----------|---------|-------------|
| `enabled` | `true` | Enable/disable the mod |
| `translateIncoming` | `true` | Translate incoming messages |
| `translateOutgoing` | `true` | Translate outgoing messages |
| `defaultSourceLang` | `"auto"` | Source language (`auto` for detection) |
| `defaultTargetLang` | `"en"` | Target language for both incoming messages (messages from other players) and outgoing messages (your messages sent to server) |
| `preferredTranslator` | `"google"` | Provider: `google`, `deepl`, `kagi` |
| `deeplApiKey` | `""` | DeepL API key |
| `kagiApiKey` | `""` | Kagi API token |
| `kagiSessionToken` | `""` | Kagi session token |
| `showOriginalOnHover` | `true` | Show original on hover |
| `cacheSize` | `512` | Translation cache size |
| `debugMode` | `false` | Debug logging |

### Translation Providers

| Provider | Cost | API Key | Languages |
|----------|------|---------|-----------|
| **Google** | Free | Not required | 100+ |
| **DeepL** | Free/Paid | [Get API Key](https://www.deepl.com/pro-api) | 30+ |
| **Kagi** | Kagi Pass | API Token ([settings/api](https://kagi.com/settings/api)) or Session Token ([settings/user_details](https://kagi.com/settings/user_details)) | Multiple |

---

## 🐛 Reporting Issues

Found a bug or have a suggestion? Create an issue:

👉 **https://github.com/Lintech-1/LinguaChat/issues**

Please include:
- Minecraft version
- Mod version
- Log excerpts
- Steps to reproduce

---

## 📚 For Developers

Full developer documentation including Stonecutter multi-version development, compatibility layers, and public API is available in [`docs/en.md`](docs/en.md) (English) and [`docs/ru.md`](docs/ru.md) (Русский).

---

<div align="center">

*Made with ❤️ for the Minecraft community. Happy gaming and clear communication in any language!*

</div>
