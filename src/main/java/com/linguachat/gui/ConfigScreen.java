package com.linguachat.gui;

import com.linguachat.compat.ButtonCompat;
import com.linguachat.compat.I18nCompat;
import com.linguachat.compat.ScreenCompat;
import com.linguachat.compat.TextCompat;
import com.linguachat.config.ModConfig;
//? if >=1.20 || =1.21.1 || =1.21.4 || =1.21.5 || =1.21.11 {
import net.minecraft.client.gui.DrawContext;
//?} else {
/*import net.minecraft.client.util.math.MatrixStack;
*///?}
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
//? if >=1.19 {
import net.minecraft.screen.ScreenTexts;
//?}
import net.minecraft.text.Text;

public class ConfigScreen extends Screen {
    // Layout constants
    private static final int CONTAINER_WIDTH = 280;
    private static final int LABEL_COLUMN_WIDTH = 130;
    private static final int FIELD_WIDTH = 140;
    private static final int BUTTON_WIDTH = 200;
    private static final int ROW_HEIGHT = 20;
    private static final int ROW_SPACING = 6;
    private static final int SECTION_GAP = 12;
    private static final int TITLE_OFFSET = 30;
    private static final int BOTTOM_MARGIN = 35;
    
    private final Screen parent;
    private final ModConfig config;
    
    private ButtonWidget enabledButton;
    private ButtonWidget translateIncomingButton;
    private ButtonWidget translateOutgoingButton;
    private ButtonWidget providerButton;
    private ButtonWidget debugModeButton;
    
    private TextFieldWidget sourceLangField;
    private TextFieldWidget targetLangField;
    private TextFieldWidget apiKeyField;
    private TextFieldWidget sessionTokenField;
    private TextFieldWidget cacheSizeField;
    
    private String currentProvider;

    public ConfigScreen(Screen parent) {
        super(I18nCompat.translatableText("linguachat.config.title"));
        this.parent = parent;
        this.config = ModConfig.get();
        this.currentProvider = config.getPreferredTranslator().toLowerCase();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        // Calculate container positioning
        int containerLeft = centerX - CONTAINER_WIDTH / 2;
        int labelX = containerLeft + 10;
        int fieldX = containerLeft + LABEL_COLUMN_WIDTH + 10;
        
        // Calculate total height for vertical centering
        int totalRows = 4 + 1 + 2 + 1 + 1 + 1 + 1;
        if ("kagi".equals(currentProvider)) {
            totalRows += 1;
        }
        int totalHeight = totalRows * ROW_HEIGHT + (totalRows - 1) * ROW_SPACING + 3 * SECTION_GAP;
        int startY = centerY - totalHeight / 2;
        
        int currentY = startY;

        // Main toggles (full width)
        this.enabledButton = ButtonCompat.create(
            centerX - BUTTON_WIDTH / 2, currentY, BUTTON_WIDTH, ROW_HEIGHT,
            I18nCompat.translatableText("linguachat.config.enabled", 
                config.isEnabled() ? I18nCompat.translate("linguachat.config.enabled.on") : I18nCompat.translate("linguachat.config.enabled.off")),
            button -> {
                config.setEnabled(!config.isEnabled());
                button.setMessage(I18nCompat.translatableText("linguachat.config.enabled", 
                    config.isEnabled() ? I18nCompat.translate("linguachat.config.enabled.on") : I18nCompat.translate("linguachat.config.enabled.off")));
            }
        );
        //? if >=1.17 {
        this.addDrawableChild(enabledButton);
        //?} else {
        /*this.addButton(enabledButton);
        *///?}
        currentY += ROW_HEIGHT + ROW_SPACING;

        this.translateIncomingButton = ButtonCompat.create(
            centerX - BUTTON_WIDTH / 2, currentY, BUTTON_WIDTH, ROW_HEIGHT,
            I18nCompat.translatableText("linguachat.config.translate_incoming", 
                config.isTranslateIncoming() ? I18nCompat.translate("linguachat.config.toggle.on") : I18nCompat.translate("linguachat.config.toggle.off")),
            button -> {
                config.setTranslateIncoming(!config.isTranslateIncoming());
                button.setMessage(I18nCompat.translatableText("linguachat.config.translate_incoming", 
                    config.isTranslateIncoming() ? I18nCompat.translate("linguachat.config.toggle.on") : I18nCompat.translate("linguachat.config.toggle.off")));
            }
        );
        //? if >=1.17 {
        this.addDrawableChild(translateIncomingButton);
        //?} else {
        /*this.addButton(translateIncomingButton);
        *///?}
        currentY += ROW_HEIGHT + ROW_SPACING;

        this.translateOutgoingButton = ButtonCompat.create(
            centerX - BUTTON_WIDTH / 2, currentY, BUTTON_WIDTH, ROW_HEIGHT,
            I18nCompat.translatableText("linguachat.config.translate_outgoing", 
                config.isTranslateOutgoing() ? I18nCompat.translate("linguachat.config.toggle.on") : I18nCompat.translate("linguachat.config.toggle.off")),
            button -> {
                config.setTranslateOutgoing(!config.isTranslateOutgoing());
                button.setMessage(I18nCompat.translatableText("linguachat.config.translate_outgoing", 
                    config.isTranslateOutgoing() ? I18nCompat.translate("linguachat.config.toggle.on") : I18nCompat.translate("linguachat.config.toggle.off")));
            }
        );
        //? if >=1.17 {
        this.addDrawableChild(translateOutgoingButton);
        //?} else {
        /*this.addButton(translateOutgoingButton);
        *///?}
        currentY += ROW_HEIGHT + ROW_SPACING;

        // Provider selection
        this.providerButton = ButtonCompat.create(
            centerX - BUTTON_WIDTH / 2, currentY, BUTTON_WIDTH, ROW_HEIGHT,
            I18nCompat.translatableText("linguachat.config.provider", currentProvider.toUpperCase()),
            button -> {
                if ("google".equals(currentProvider)) {
                    currentProvider = "deepl";
                } else if ("deepl".equals(currentProvider)) {
                    currentProvider = "kagi";
                } else if ("kagi".equals(currentProvider)) {
                    currentProvider = "google";
                } else {
                    currentProvider = "google";
                }
                config.setPreferredTranslator(currentProvider);
                button.setMessage(I18nCompat.translatableText("linguachat.config.provider", currentProvider.toUpperCase()));
                // Recreate screen to update API fields
                ScreenCompat.setScreen(this.client, new ConfigScreen(parent));
            }
        );
        //? if >=1.17 {
        this.addDrawableChild(providerButton);
        //?} else {
        /*this.addButton(providerButton);
        *///?}
        currentY += ROW_HEIGHT + SECTION_GAP;

        // Language fields (tabular layout)
        //? if >=1.21.11 {
        this.sourceLangField = new TextFieldWidget(this.textRenderer, fieldX, currentY, FIELD_WIDTH, ROW_HEIGHT, this.sourceLangField, TextCompat.literal("Source Lang"));
        //?} else {
        /*this.sourceLangField = new TextFieldWidget(this.textRenderer, fieldX, currentY, FIELD_WIDTH, ROW_HEIGHT, TextCompat.literal("Source Lang"));
        *///?}
        this.sourceLangField.setMaxLength(10);
        this.sourceLangField.setText(config.getDefaultSourceLang());
        //? if >=1.17 {
        this.addSelectableChild(sourceLangField);
        //?} else {
        /*this.children.add(sourceLangField);
        *///?}
        currentY += ROW_HEIGHT + ROW_SPACING;

        //? if >=1.21.11 {
        this.targetLangField = new TextFieldWidget(this.textRenderer, fieldX, currentY, FIELD_WIDTH, ROW_HEIGHT, this.targetLangField, TextCompat.literal("Target Lang"));
        //?} else {
        /*this.targetLangField = new TextFieldWidget(this.textRenderer, fieldX, currentY, FIELD_WIDTH, ROW_HEIGHT, TextCompat.literal("Target Lang"));
        *///?}
        this.targetLangField.setMaxLength(10);
        this.targetLangField.setText(config.getDefaultTargetLang());
        //? if >=1.17 {
        this.addSelectableChild(targetLangField);
        //?} else {
        /*this.children.add(targetLangField);
        *///?}
        currentY += ROW_HEIGHT + SECTION_GAP;

        // API Key fields (depends on provider)
        if ("deepl".equals(currentProvider)) {
            //? if >=1.21.11 {
            this.apiKeyField = new TextFieldWidget(this.textRenderer, fieldX, currentY, FIELD_WIDTH, ROW_HEIGHT, this.apiKeyField, TextCompat.literal("DeepL API Key"));
            //?} else {
            /*this.apiKeyField = new TextFieldWidget(this.textRenderer, fieldX, currentY, FIELD_WIDTH, ROW_HEIGHT, TextCompat.literal("DeepL API Key"));
            *///?}
            this.apiKeyField.setMaxLength(100);
            this.apiKeyField.setText(config.getDeeplApiKey());
            //? if >=1.17 {
            this.addSelectableChild(apiKeyField);
            //?} else {
            /*this.children.add(apiKeyField);
            *///?}
            currentY += ROW_HEIGHT + SECTION_GAP;
        } else if ("kagi".equals(currentProvider)) {
            //? if >=1.21.11 {
            this.apiKeyField = new TextFieldWidget(this.textRenderer, fieldX, currentY, FIELD_WIDTH, ROW_HEIGHT, this.apiKeyField, TextCompat.literal("Kagi API Key"));
            //?} else {
            /*this.apiKeyField = new TextFieldWidget(this.textRenderer, fieldX, currentY, FIELD_WIDTH, ROW_HEIGHT, TextCompat.literal("Kagi API Key"));
            *///?}
            this.apiKeyField.setMaxLength(100);
            this.apiKeyField.setText(config.getKagiApiKey());
            //? if >=1.17 {
            this.addSelectableChild(apiKeyField);
            //?} else {
            /*this.children.add(apiKeyField);
            *///?}
            currentY += ROW_HEIGHT + ROW_SPACING;
            
            //? if >=1.21.11 {
            this.sessionTokenField = new TextFieldWidget(this.textRenderer, fieldX, currentY, FIELD_WIDTH, ROW_HEIGHT, this.sessionTokenField, TextCompat.literal("Kagi Session Token"));
            //?} else {
            /*this.sessionTokenField = new TextFieldWidget(this.textRenderer, fieldX, currentY, FIELD_WIDTH, ROW_HEIGHT, TextCompat.literal("Kagi Session Token"));
            *///?}
            this.sessionTokenField.setMaxLength(200);
            this.sessionTokenField.setText(config.getKagiSessionToken());
            //? if >=1.17 {
            this.addSelectableChild(sessionTokenField);
            //?} else {
            /*this.children.add(sessionTokenField);
            *///?}
            currentY += ROW_HEIGHT + SECTION_GAP;
        } else {
            // Google needs no API key
            currentY += ROW_HEIGHT + SECTION_GAP;
        }

        // Cache Size
        //? if >=1.21.11 {
        this.cacheSizeField = new TextFieldWidget(this.textRenderer, fieldX, currentY, FIELD_WIDTH, ROW_HEIGHT, this.cacheSizeField, TextCompat.literal("Cache Size"));
        //?} else {
        /*this.cacheSizeField = new TextFieldWidget(this.textRenderer, fieldX, currentY, FIELD_WIDTH, ROW_HEIGHT, TextCompat.literal("Cache Size"));
        *///?}
        this.cacheSizeField.setMaxLength(5);
        this.cacheSizeField.setText(String.valueOf(config.getCacheSize()));
        //? if >=1.17 {
        this.addSelectableChild(cacheSizeField);
        //?} else {
        /*this.children.add(cacheSizeField);
        *///?}
        currentY += ROW_HEIGHT + ROW_SPACING;

        // Debug Mode
        this.debugModeButton = ButtonCompat.create(
            centerX - BUTTON_WIDTH / 2, currentY, BUTTON_WIDTH, ROW_HEIGHT,
            I18nCompat.translatableText("linguachat.config.debug_mode", 
                config.isDebugMode() ? I18nCompat.translate("linguachat.config.toggle.on") : I18nCompat.translate("linguachat.config.toggle.off")),
            button -> {
                config.setDebugMode(!config.isDebugMode());
                button.setMessage(I18nCompat.translatableText("linguachat.config.debug_mode", 
                    config.isDebugMode() ? I18nCompat.translate("linguachat.config.toggle.on") : I18nCompat.translate("linguachat.config.toggle.off")));
            }
        );
        //? if >=1.17 {
        this.addDrawableChild(debugModeButton);
        //?} else {
        /*this.addButton(debugModeButton);
        *///?}

        // Save/Cancel at bottom
        int bottomY = this.height - BOTTOM_MARGIN;
        ButtonWidget saveButton = ButtonCompat.create(
            centerX - BUTTON_WIDTH / 2 - 5, bottomY, BUTTON_WIDTH / 2 - 2, ROW_HEIGHT,
            I18nCompat.translatableText("linguachat.config.save"),
            button -> {
                saveConfig();
                //? if >=1.18 {
                this.close();
                //?} else {
                /*this.onClose();
                *///?}
            }
        );
        //? if >=1.17 {
        this.addDrawableChild(saveButton);
        //?} else {
        /*this.addButton(saveButton);
        *///?}

        ButtonWidget cancelButton = ButtonCompat.create(
            centerX + 5, bottomY, BUTTON_WIDTH / 2 - 2, ROW_HEIGHT,
            //? if >=1.19 {
            ScreenTexts.CANCEL,
            //?} else {
            /*TextCompat.literal("Cancel"),
            *///?}
            //? if >=1.18 {
            button -> this.close()
            //?} else {
            /*button -> this.onClose()
            *///?}
        );
        //? if >=1.17 {
        this.addDrawableChild(cancelButton);
        //?} else {
        /*this.addButton(cancelButton);
        *///?}
    }

    private void saveConfig() {
        config.setDefaultSourceLang(sourceLangField.getText());
        config.setDefaultTargetLang(targetLangField.getText());
        
        if ("deepl".equals(currentProvider) && apiKeyField != null) {
            config.setDeeplApiKey(apiKeyField.getText());
        } else if ("kagi".equals(currentProvider)) {
            if (apiKeyField != null) {
                config.setKagiApiKey(apiKeyField.getText());
            }
            if (sessionTokenField != null) {
                config.setKagiSessionToken(sessionTokenField.getText());
            }
        }
        
        try {
            int cacheSize = Integer.parseInt(cacheSizeField.getText());
            config.setCacheSize(cacheSize);
        } catch (NumberFormatException e) {
            // Invalid input, ignore
        }
    }

    @Override
    //? if >=1.20 || =1.21.1 || =1.21.4 || =1.21.5 || =1.21.11 {
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        //? if =1.20.1 {
        /*this.renderBackground(context);
        *///?}
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        // Calculate positioning for title
        int totalRows = 4 + 1 + 2 + 1 + 1 + 1 + 1;
        if ("kagi".equals(currentProvider)) {
            totalRows += 1;
        }
        int totalHeight = totalRows * ROW_HEIGHT + (totalRows - 1) * ROW_SPACING + 3 * SECTION_GAP;
        int startY = centerY - totalHeight / 2;
        
        ScreenCompat.drawCenteredText(context, this.textRenderer, this.title, centerX, startY - TITLE_OFFSET, 0xFFFFFFFF);
        
        // Render vanilla UI first (buttons + background)
        super.render(context, mouseX, mouseY, delta);
        
        // Draw labels after super.render() to avoid z-fighting
        if (sourceLangField != null) {
            context.drawText(this.textRenderer, I18nCompat.translatableText("linguachat.config.source_lang"), 
                sourceLangField.getX() - LABEL_COLUMN_WIDTH, sourceLangField.getY() + 6, 0xFFFFFFFF, true);
        }
        
        if (targetLangField != null) {
            context.drawText(this.textRenderer, I18nCompat.translatableText("linguachat.config.target_lang"), 
                targetLangField.getX() - LABEL_COLUMN_WIDTH, targetLangField.getY() + 6, 0xFFFFFFFF, true);
        }
        
        if (apiKeyField != null) {
            String labelKey = "deepl".equals(currentProvider) ? "linguachat.config.api_key.deepl" : "linguachat.config.api_key.kagi";
            context.drawText(this.textRenderer, I18nCompat.translatableText(labelKey), 
                apiKeyField.getX() - LABEL_COLUMN_WIDTH, apiKeyField.getY() + 6, 0xFFFFFFFF, true);
        }
        
        if (sessionTokenField != null) {
            context.drawText(this.textRenderer, I18nCompat.translatableText("linguachat.config.session_token"), 
                sessionTokenField.getX() - LABEL_COLUMN_WIDTH, sessionTokenField.getY() + 6, 0xFFFFFFFF, true);
        }
        
        if (cacheSizeField != null) {
            context.drawText(this.textRenderer, I18nCompat.translatableText("linguachat.config.cache_size"), 
                cacheSizeField.getX() - LABEL_COLUMN_WIDTH, cacheSizeField.getY() + 6, 0xFFFFFFFF, true);
        }
        
        // Google notice (gray text when no API field needed)
        if ("google".equals(currentProvider)) {
            int containerLeft = centerX - CONTAINER_WIDTH / 2;
            int labelX = containerLeft + 10;
            int noticeY = startY + (ROW_HEIGHT + ROW_SPACING) * 4 + SECTION_GAP + (ROW_HEIGHT + ROW_SPACING) * 2 + SECTION_GAP;
            context.drawText(this.textRenderer, I18nCompat.translatableText("linguachat.config.google_notice"), 
                labelX, noticeY + 6, 0xFF888888, true);
        }
        
        // Render text fields after labels
        if (sourceLangField != null) {
            sourceLangField.render(context, mouseX, mouseY, delta);
        }
        if (targetLangField != null) {
            targetLangField.render(context, mouseX, mouseY, delta);
        }
        if (apiKeyField != null) {
            apiKeyField.render(context, mouseX, mouseY, delta);
        }
        if (sessionTokenField != null) {
            sessionTokenField.render(context, mouseX, mouseY, delta);
        }
        if (cacheSizeField != null) {
            cacheSizeField.render(context, mouseX, mouseY, delta);
        }
    }
    //?} else {
    /*public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        // Calculate container positioning
        int containerLeft = centerX - CONTAINER_WIDTH / 2;
        int labelX = containerLeft + 10;
        
        // Calculate total height (same as init)
        int totalRows = 4 + 1 + 2 + 1 + 1 + 1 + 1;
        if ("kagi".equals(currentProvider)) {
            totalRows += 1;
        }
        int totalHeight = totalRows * ROW_HEIGHT + (totalRows - 1) * ROW_SPACING + 3 * SECTION_GAP;
        int startY = centerY - totalHeight / 2;
        
        ScreenCompat.drawCenteredText(matrices, this.textRenderer, this.title, centerX, startY - TITLE_OFFSET, 0xFFFFFF);
        
        // Calculate Y positions (matching init logic)
        int currentY = startY;
        currentY += ROW_HEIGHT + ROW_SPACING; // Skip enabled button
        currentY += ROW_HEIGHT + ROW_SPACING; // Skip incoming button
        currentY += ROW_HEIGHT + ROW_SPACING; // Skip outgoing button
        currentY += ROW_HEIGHT + SECTION_GAP; // Skip provider button
        
        // Language field labels (same Y as fields, left column)
        int sourceLangY = currentY;
        ScreenCompat.drawTextWithShadow(matrices, this.textRenderer, I18nCompat.translate("linguachat.config.source_lang"), labelX, sourceLangY + 6, 0xAAAAAA);
        currentY += ROW_HEIGHT + ROW_SPACING;
        
        int targetLangY = currentY;
        ScreenCompat.drawTextWithShadow(matrices, this.textRenderer, I18nCompat.translate("linguachat.config.target_lang"), labelX, targetLangY + 6, 0xAAAAAA);
        currentY += ROW_HEIGHT + SECTION_GAP;
        
        // API field labels (provider-specific)
        int apiFieldY = currentY;
        if ("deepl".equals(currentProvider)) {
            ScreenCompat.drawTextWithShadow(matrices, this.textRenderer, I18nCompat.translate("linguachat.config.api_key.deepl"), labelX, apiFieldY + 6, 0xAAAAAA);
            currentY += ROW_HEIGHT + SECTION_GAP;
        } else if ("kagi".equals(currentProvider)) {
            ScreenCompat.drawTextWithShadow(matrices, this.textRenderer, I18nCompat.translate("linguachat.config.api_key.kagi"), labelX, apiFieldY + 6, 0xAAAAAA);
            currentY += ROW_HEIGHT + ROW_SPACING;
            
            int sessionTokenY = currentY;
            ScreenCompat.drawTextWithShadow(matrices, this.textRenderer, I18nCompat.translate("linguachat.config.session_token"), labelX, sessionTokenY + 6, 0xAAAAAA);
            currentY += ROW_HEIGHT + SECTION_GAP;
        } else {
            // Google - no API key, show notice
            ScreenCompat.drawTextWithShadow(matrices, this.textRenderer, I18nCompat.translate("linguachat.config.google_notice"), labelX, apiFieldY + 6, 0x888888);
            currentY += ROW_HEIGHT + SECTION_GAP;
        }
        
        // Cache size label
        int cacheSizeY = currentY;
        ScreenCompat.drawTextWithShadow(matrices, this.textRenderer, I18nCompat.translate("linguachat.config.cache_size"), labelX, cacheSizeY + 6, 0xAAAAAA);
        
        super.render(matrices, mouseX, mouseY, delta);
        
        // Explicitly render text fields for older MC versions
        if (sourceLangField != null) {
            sourceLangField.render(matrices, mouseX, mouseY, delta);
        }
        if (targetLangField != null) {
            targetLangField.render(matrices, mouseX, mouseY, delta);
        }
        if (apiKeyField != null) {
            apiKeyField.render(matrices, mouseX, mouseY, delta);
        }
        if (sessionTokenField != null) {
            sessionTokenField.render(matrices, mouseX, mouseY, delta);
        }
        if (cacheSizeField != null) {
            cacheSizeField.render(matrices, mouseX, mouseY, delta);
        }
    }
    *///?}

    @Override
    //? if >=1.18 {
    public void close() {
        if (this.client != null) {
            ScreenCompat.setScreen(this.client, parent);
        }
    }
    //?} else {
    /*public void onClose() {
        if (this.client != null) {
            ScreenCompat.setScreen(this.client, parent);
        }
    }
    *///?}
}
