# Changelog

## 3.0.0
- Add Minecraft 26.3 support (unobfuscated, Mojang naming, Java 25): new ChatListenerMixin, Mojang variants of compat layers, config screen ported to the new render extraction API
- Config screen: Source/Target language text fields replaced with searchable dropdowns (55 languages + auto for source); free-text codes from old configs still work and are shown as-is
- Error notifications in chat now show a localized error type instead of the raw provider message (raw details remain in the game log only; notifications are still client-side only, never sent to the server)
- Config auto-fix: language codes are normalized (trim/case/underscores) and API keys are trimmed on save; whitespace-only keys no longer pass as valid
- Update bundled libraries: deepl-java 1.17.0, gson 2.15.0
- All outgoing HTTP requests send a uniquely-identifying User-Agent (LinguaChat/<version> + repo link)
- Fix screen flickering when opening/closing the config screen on 26.3: setScreenAndShow() forces an extra immediate render frame on every open/close; the mod now uses gui.setScreen() (same as the 26.1-era setScreen) — same root cause as the FreeCursor mod fixed on 26.2
- Add GitHub issue templates (bug report with version/provider dropdowns, feature request)
- Gradle runs without a lingering daemon (org.gradle.daemon=false) so builds don't hold RAM

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
