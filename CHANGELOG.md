# Changelog

## 2.1.0

- Fix translation returning English instead of the configured target language when auto-detection finds the text already in the target language
- Fix click events (links, commands) being lost on translated messages (#5)
- Fix system messages (achievements, /say, plugin notifications) being sent to translation providers
- Fix the mod translating its own feedback messages
- Fix doubled player-name prefix in translated tellraw-style messages
- Fix excessive "Using existing TranslationManager instance" log spam
- Add local error notifications when all translation providers fail: outgoing messages are not sent untranslated, incoming messages fall back to the original text
- Add actionable Kagi 401 error message; session links are rejected in favor of the kagi_session cookie value or an API token
- Fix Kagi authentication: the token is extracted from a pasted session link automatically, and provider bans are lifted when the token changes
- Complete localization of the config screen (provider names, Cancel button, field labels) and unify the log language; previously hardcoded strings now use translation files
- Clamp cache size to 64-4096 when saved from the config screen (bounds were only enforced at startup)

## 2.0.0

- Add Stonecutter plugin for building across 8 Minecraft versions (1.16.5 - 1.21.11)
- Configure version-specific dependencies (Fabric API, Mod Menu, Java toolchain)
- Add compatibility layers (TextCompat, ScreenCompat, ButtonCompat, I18nCompat, KeyBindingCompat)
- Add Kagi translation provider alongside existing Google Translate and DeepL
- Add configuration GUI with ConfigScreen and ModMenu integration
- Add keybind support for toggling translation and opening config screen
- Add translation caching, logging, and message store API
- Add GitHub Actions workflows for build and release with manual approval
- Add developer documentation (EN/RU)
- Update mod version to 2.0.0
- Remove unused Forge mods.toml and hardcoded Java path from gradle.properties
