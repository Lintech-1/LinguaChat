package com.linguachat.gui;

import com.linguachat.compat.ButtonCompat;
//? if <26.1 {
import org.lwjgl.glfw.GLFW;
//?}
//? if =1.21.11 {
import net.minecraft.client.gui.Click;
import net.minecraft.client.input.KeyInput;
//?}
//? if >=26.1 {
/*import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import com.mojang.blaze3d.platform.InputConstants;
*///?}
import com.linguachat.compat.I18nCompat;
import com.linguachat.compat.ScreenCompat;
import com.linguachat.compat.TextCompat;
import com.linguachat.config.ModConfig;

import java.util.List;
//? if >=26.1 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
*///?} else {
//? if >=1.20 || =1.21.1 || =1.21.4 || =1.21.5 || =1.21.11 {
import net.minecraft.client.gui.DrawContext;
//?} else {
/*import net.minecraft.client.util.math.MatrixStack;
*///?}
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
//?}

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

    //? if >=26.1 {
    //private Button enabledButton;
    //private Button translateIncomingButton;
    //private Button translateOutgoingButton;
    //private Button providerButton;
    //private Button debugModeButton;
    //
    //private Button sourceLangButton;
    //private Button targetLangButton;
    //private EditBox apiKeyField;
    //private EditBox sessionTokenField;
    //private EditBox cacheSizeField;
    //?} else {
    private ButtonWidget enabledButton;
    private ButtonWidget translateIncomingButton;
    private ButtonWidget translateOutgoingButton;
    private ButtonWidget providerButton;
    private ButtonWidget debugModeButton;

    private ButtonWidget sourceLangButton;
    private ButtonWidget targetLangButton;
    private TextFieldWidget apiKeyField;
    private TextFieldWidget sessionTokenField;
    private TextFieldWidget cacheSizeField;
    //?}

    private String currentProvider;

    // dropdown state (shared across versions)
    private String selectedSourceLang;
    private String selectedTargetLang;
    private static final int DROPDOWN_SOURCE = 0;
    private static final int DROPDOWN_TARGET = 1;
    private static final int DROPDOWN_ROW_HEIGHT = 12;
    private static final int DROPDOWN_VISIBLE_ROWS = 8;
    private static final int DROPDOWN_SEARCH_HEIGHT = 16;
    private int openDropdown = -1;
    private int scrollOffset = 0;
    private int dropdownX, dropdownY, dropdownW;
    //? if >=26.1 {
    //private EditBox searchBox;
    //?} else if >=1.21.11 {
    private TextFieldWidget searchBox;
    //?} else {
    private String searchQuery = "";
    //?}
    private boolean searchFieldActive = false;

    public ConfigScreen(Screen parent) {
        super(I18nCompat.translatableText("linguachat.config.title"));
        this.parent = parent;
        this.config = ModConfig.get();
        this.currentProvider = config.getPreferredTranslator().toLowerCase();
        this.selectedSourceLang = config.getDefaultSourceLang();
        this.selectedTargetLang = config.getDefaultTargetLang();
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
        //? if >=26.1 {
        //this.addRenderableWidget(enabledButton);
        //?} else if >=1.17 {
        this.addDrawableChild(enabledButton);
        //?} else {
        /*this.addButton(enabledButton);
        *///?}
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
        //? if >=26.1 {
        //this.addRenderableWidget(translateIncomingButton);
        //?} else if >=1.17 {
        this.addDrawableChild(translateIncomingButton);
        //?} else {
        /*this.addButton(translateIncomingButton);
        *///?}
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
        //? if >=26.1 {
        //this.addRenderableWidget(translateOutgoingButton);
        //?} else if >=1.17 {
        this.addDrawableChild(translateOutgoingButton);
        //?} else {
        /*this.addButton(translateOutgoingButton);
        *///?}
        //?} else {
        /*this.addButton(translateOutgoingButton);
        *///?}
        currentY += ROW_HEIGHT + ROW_SPACING;

        // Provider selection
        this.providerButton = ButtonCompat.create(
            centerX - BUTTON_WIDTH / 2, currentY, BUTTON_WIDTH, ROW_HEIGHT,
            I18nCompat.translatableText("linguachat.config.provider", I18nCompat.translate("linguachat.config.provider." + currentProvider)),
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
                button.setMessage(I18nCompat.translatableText("linguachat.config.provider", I18nCompat.translate("linguachat.config.provider." + currentProvider)));
                // Recreate screen to update API fields
                //? if >=26.1 {
                //ScreenCompat.setScreen(this.minecraft, new ConfigScreen(parent));
                //?} else {
                                ScreenCompat.setScreen(this.client, new ConfigScreen(parent));
                //?}
            }
        );
        //? if >=1.17 {
        //? if >=26.1 {
        //this.addRenderableWidget(providerButton);
        //?} else if >=1.17 {
        this.addDrawableChild(providerButton);
        //?} else {
        /*this.addButton(providerButton);
        *///?}
        //?} else {
        /*this.addButton(providerButton);
        *///?}
        currentY += ROW_HEIGHT + SECTION_GAP;

        // Source Language (dropdown button)
        this.sourceLangButton = ButtonCompat.create(
            centerX - BUTTON_WIDTH / 2, currentY, BUTTON_WIDTH, ROW_HEIGHT,
            TextCompat.literal(I18nCompat.translate("linguachat.config.source_lang") + ": " + LangList.display(selectedSourceLang)),
            button -> openDropdown(DROPDOWN_SOURCE, BUTTON_WIDTH)
        );
        //? if >=26.1 {
        //this.addRenderableWidget(sourceLangButton);
        //?} else if >=1.17 {
        this.addDrawableChild(sourceLangButton);
        //?} else {
        /*this.addButton(sourceLangButton);
        *///?}
        currentY += ROW_HEIGHT + ROW_SPACING;

        // Target Language (dropdown button)
        this.targetLangButton = ButtonCompat.create(
            centerX - BUTTON_WIDTH / 2, currentY, BUTTON_WIDTH, ROW_HEIGHT,
            TextCompat.literal(I18nCompat.translate("linguachat.config.target_lang") + ": " + LangList.display(selectedTargetLang)),
            button -> openDropdown(DROPDOWN_TARGET, BUTTON_WIDTH)
        );
        //? if >=26.1 {
        //this.addRenderableWidget(targetLangButton);
        //?} else if >=1.17 {
        this.addDrawableChild(targetLangButton);
        //?} else {
        /*this.addButton(targetLangButton);
        *///?}
        currentY += ROW_HEIGHT + SECTION_GAP;

        // API Key fields (depends on provider)
        if ("deepl".equals(currentProvider)) {
            //? if >=26.1 {
//            this.apiKeyField = new EditBox(this.font, fieldX, currentY, FIELD_WIDTH, ROW_HEIGHT, I18nCompat.translatableText("linguachat.config.api_key.deepl"));
            //?} else if >=1.21.11 {
            this.apiKeyField = new TextFieldWidget(this.textRenderer, fieldX, currentY, FIELD_WIDTH, ROW_HEIGHT, this.apiKeyField, I18nCompat.translatableText("linguachat.config.api_key.deepl"));
            //?} else {
            /*this.apiKeyField = new TextFieldWidget(this.textRenderer, fieldX, currentY, FIELD_WIDTH, ROW_HEIGHT, I18nCompat.translatableText("linguachat.config.api_key.deepl"));
            *///?}
            this.apiKeyField.setMaxLength(100);
            //? if >=26.1 {
//            this.apiKeyField.setValue(config.getDeeplApiKey());
            //?} else {
            this.apiKeyField.setText(config.getDeeplApiKey());
            //?}
            //? if >=1.17 {
            //? if >=26.1 {
            //this.addRenderableWidget(apiKeyField);
            //?} else if >=1.17 {
            this.addSelectableChild(apiKeyField);
            //?} else {
            /*this.children.add(apiKeyField);
            *///?}
            //?} else {
            /*this.children.add(apiKeyField);
            *///?}
            currentY += ROW_HEIGHT + SECTION_GAP;
        } else if ("kagi".equals(currentProvider)) {
            //? if >=26.1 {
//            this.apiKeyField = new EditBox(this.font, fieldX, currentY, FIELD_WIDTH, ROW_HEIGHT, I18nCompat.translatableText("linguachat.config.api_key.kagi"));
            //?} else if >=1.21.11 {
            this.apiKeyField = new TextFieldWidget(this.textRenderer, fieldX, currentY, FIELD_WIDTH, ROW_HEIGHT, this.apiKeyField, I18nCompat.translatableText("linguachat.config.api_key.kagi"));
            //?} else {
            /*this.apiKeyField = new TextFieldWidget(this.textRenderer, fieldX, currentY, FIELD_WIDTH, ROW_HEIGHT, I18nCompat.translatableText("linguachat.config.api_key.kagi"));
            *///?}
            this.apiKeyField.setMaxLength(100);
            //? if >=26.1 {
//            this.apiKeyField.setValue(config.getKagiApiKey());
            //?} else {
            this.apiKeyField.setText(config.getKagiApiKey());
            //?}
            //? if >=1.17 {
            //? if >=26.1 {
            //this.addRenderableWidget(apiKeyField);
            //?} else if >=1.17 {
            this.addSelectableChild(apiKeyField);
            //?} else {
            /*this.children.add(apiKeyField);
            *///?}
            //?} else {
            /*this.children.add(apiKeyField);
            *///?}
            currentY += ROW_HEIGHT + ROW_SPACING;
            
            //? if >=26.1 {
//            this.sessionTokenField = new EditBox(this.font, fieldX, currentY, FIELD_WIDTH, ROW_HEIGHT, I18nCompat.translatableText("linguachat.config.session_token"));
            //?} else if >=1.21.11 {
            this.sessionTokenField = new TextFieldWidget(this.textRenderer, fieldX, currentY, FIELD_WIDTH, ROW_HEIGHT, this.sessionTokenField, I18nCompat.translatableText("linguachat.config.session_token"));
            //?} else {
            /*this.sessionTokenField = new TextFieldWidget(this.textRenderer, fieldX, currentY, FIELD_WIDTH, ROW_HEIGHT, I18nCompat.translatableText("linguachat.config.session_token"));
            *///?}
            this.sessionTokenField.setMaxLength(200);
            //? if >=26.1 {
//            this.sessionTokenField.setValue(config.getKagiSessionToken());
            //?} else {
            this.sessionTokenField.setText(config.getKagiSessionToken());
            //?}
            //? if >=1.17 {
            //? if >=26.1 {
            //this.addRenderableWidget(sessionTokenField);
            //?} else if >=1.17 {
            this.addSelectableChild(sessionTokenField);
            //?} else {
            /*this.children.add(sessionTokenField);
            *///?}
            //?} else {
            /*this.children.add(sessionTokenField);
            *///?}
            currentY += ROW_HEIGHT + SECTION_GAP;
        } else {
            // Google needs no API key
            currentY += ROW_HEIGHT + SECTION_GAP;
        }

        // Cache Size
        //? if >=26.1 {
//        this.cacheSizeField = new EditBox(this.font, fieldX, currentY, FIELD_WIDTH, ROW_HEIGHT, I18nCompat.translatableText("linguachat.config.cache_size"));
        //?} else if >=1.21.11 {
        this.cacheSizeField = new TextFieldWidget(this.textRenderer, fieldX, currentY, FIELD_WIDTH, ROW_HEIGHT, this.cacheSizeField, I18nCompat.translatableText("linguachat.config.cache_size"));
        //?} else {
        /*this.cacheSizeField = new TextFieldWidget(this.textRenderer, fieldX, currentY, FIELD_WIDTH, ROW_HEIGHT, I18nCompat.translatableText("linguachat.config.cache_size"));
        *///?}
        this.cacheSizeField.setMaxLength(5);
        //? if >=26.1 {
//        this.cacheSizeField.setValue(String.valueOf(config.getCacheSize()));
        //?} else {
        this.cacheSizeField.setText(String.valueOf(config.getCacheSize()));
        //?}
        //? if >=1.17 {
        //? if >=26.1 {
        //this.addRenderableWidget(cacheSizeField);
        //?} else if >=1.17 {
        this.addSelectableChild(cacheSizeField);
        //?} else {
        /*this.children.add(cacheSizeField);
        *///?}
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
        //? if >=26.1 {
        //this.addRenderableWidget(debugModeButton);
        //?} else if >=1.17 {
        this.addDrawableChild(debugModeButton);
        //?} else {
        /*this.addButton(debugModeButton);
        *///?}
        //?} else {
        /*this.addButton(debugModeButton);
        *///?}

        // Save/Cancel at bottom
        int bottomY = this.height - BOTTOM_MARGIN;
        //? if >=26.1 {
        //Button saveButton = ButtonCompat.create(
        //?} else {
        ButtonWidget saveButton = ButtonCompat.create(
        //?}
            centerX - BUTTON_WIDTH / 2 - 5, bottomY, BUTTON_WIDTH / 2 - 2, ROW_HEIGHT,
            I18nCompat.translatableText("linguachat.config.save"),
            button -> {
                saveConfig();
                //? if >=26.1 {
                //this.onClose();
                //?} else if >=1.18 {
                this.close();
                //?} else {
                /*this.onClose();
                *///?}
            }
        );
        //? if >=1.17 {
        //? if >=26.1 {
        //this.addRenderableWidget(saveButton);
        //?} else if >=1.17 {
        this.addDrawableChild(saveButton);
        //?} else {
        /*this.addButton(saveButton);
        *///?}
        //?} else {
        /*this.addButton(saveButton);
        *///?}

        //? if >=26.1 {
        //Button cancelButton = ButtonCompat.create(
        //?} else {
        ButtonWidget cancelButton = ButtonCompat.create(
        //?}
            centerX + 5, bottomY, BUTTON_WIDTH / 2 - 2, ROW_HEIGHT,
            I18nCompat.translatableText("linguachat.config.cancel"),
            //? if >=26.1 {
            //button -> this.onClose()
            //?} else if >=1.18 {
            button -> this.close()
            //?} else {
            /*button -> this.onClose()
            *///?}
        );
        //? if >=1.17 {
        //? if >=26.1 {
        //this.addRenderableWidget(cancelButton);
        //?} else if >=1.17 {
        this.addDrawableChild(cancelButton);
        //?} else {
        /*this.addButton(cancelButton);
        *///?}
        //?} else {
        /*this.addButton(cancelButton);
        *///?}
    }


    // ===== Language dropdown =====

    private void openDropdown(int which, int width) {
        openDropdown = which;
        dropdownW = width;
        // panel is centered in the menu
        dropdownX = (this.width - width) / 2;
        dropdownY = (this.height - (DROPDOWN_SEARCH_HEIGHT + 4 + DROPDOWN_VISIBLE_ROWS * DROPDOWN_ROW_HEIGHT)) / 2;
        scrollOffset = 0;
        searchFieldActive = false;
        //? if >=26.1 {
        //searchBox = new EditBox(this.font, dropdownX, dropdownY, width, DROPDOWN_SEARCH_HEIGHT, TextCompat.literal(""));
        //this.addWidget(searchBox);
        //this.setFocused(searchBox);
        //?} else if >=1.21.11 {
        searchBox = new TextFieldWidget(this.textRenderer, dropdownX, dropdownY, width, DROPDOWN_SEARCH_HEIGHT, TextCompat.literal(""));
        this.addSelectableChild(searchBox);
        this.setFocused(searchBox);
        //?} else {
        searchQuery = "";
        //?}
    }

    private void closeDropdown() {
        openDropdown = -1;
        searchFieldActive = false;
        //? if >=26.1 {
        //this.removeWidget(searchBox);
        //searchBox = null;
        //?} else if >=1.21.11 {
        this.remove(searchBox);
        searchBox = null;
        //?} else {
        searchQuery = "";
        //?}
    }

    private void updateButtonLabels() {
        //? if >=26.1 {
        //sourceLangButton.setMessage(TextCompat.literal(I18nCompat.translate("linguachat.config.source_lang") + ": " + LangList.display(selectedSourceLang)));
        //targetLangButton.setMessage(TextCompat.literal(I18nCompat.translate("linguachat.config.target_lang") + ": " + LangList.display(selectedTargetLang)));
        //?} else {
        sourceLangButton.setMessage(TextCompat.literal(I18nCompat.translate("linguachat.config.source_lang") + ": " + LangList.display(selectedSourceLang)));
        targetLangButton.setMessage(TextCompat.literal(I18nCompat.translate("linguachat.config.target_lang") + ": " + LangList.display(selectedTargetLang)));
        //?}
    }

    /** true = click consumed by the dropdown */
    private boolean handleDropdownClick(double mx, double my) {
        if (openDropdown < 0) {
            return false;
        }
        // click on the search box highlights its frame
        if (mx >= dropdownX && mx <= dropdownX + dropdownW && my >= dropdownY && my < dropdownY + DROPDOWN_SEARCH_HEIGHT) {
            searchFieldActive = true;
            //? if >=1.21.11 {
            this.setFocused(searchBox);
            //?}
            return true;
        }
        List<Integer> filtered = currentFilter();
        int listTop = dropdownY + DROPDOWN_SEARCH_HEIGHT;
        int visibleRows = Math.min(DROPDOWN_VISIBLE_ROWS, filtered.size());
        if (mx >= dropdownX && mx <= dropdownX + dropdownW && my >= listTop && my < listTop + visibleRows * DROPDOWN_ROW_HEIGHT) {
            int row = (int) ((my - listTop) / DROPDOWN_ROW_HEIGHT);
            String code = LangList.code(filtered.get(scrollOffset + row));
            if (openDropdown == DROPDOWN_SOURCE) {
                selectedSourceLang = code;
            } else {
                selectedTargetLang = code;
            }
            updateButtonLabels();
            closeDropdown();
            return true;
        }
        // click anywhere else just closes the dropdown
        closeDropdown();
        return true;
    }

    private List<Integer> currentFilter() {
        String query;
        //? if >=26.1 {
        //query = searchBox == null ? "" : searchBox.getValue();
        //?} else if >=1.21.11 {
        query = searchBox == null ? "" : searchBox.getText();
        //?} else {
        query = searchQuery;
        //?}
        List<Integer> filtered = LangList.search(query, openDropdown == DROPDOWN_SOURCE);
        // the list shrinks as the user types - keep the offset inside bounds
        scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, filtered.size() - DROPDOWN_VISIBLE_ROWS)));
        return filtered;
    }

    /** true = scroll consumed by the dropdown */
    private boolean scrollDropdown(int direction) {
        if (openDropdown < 0) {
            return false;
        }
        int maxScroll = Math.max(0, currentFilter().size() - DROPDOWN_VISIBLE_ROWS);
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - direction));
        return true;
    }

    //? if >=26.1 {
    //private void drawDropdown(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    //    if (openDropdown < 0) {
    //        return;
    //    }
    //    searchBox.extractRenderState(graphics, mouseX, mouseY, delta);
    //    if (searchBox.getValue().isEmpty()) {
    //        ScreenCompat.drawTextWithShadow(graphics, this.font, TextCompat.literal(I18nCompat.translate("linguachat.config.search")), dropdownX + 4, dropdownY + 4, 0x80808080);
    //    }
    //    List<Integer> filtered = currentFilter();
    //    int listTop = dropdownY + DROPDOWN_SEARCH_HEIGHT;
    //    int visibleRows = Math.min(DROPDOWN_VISIBLE_ROWS, filtered.size());
    //    ScreenCompat.fill(graphics, dropdownX, listTop, dropdownX + dropdownW, listTop + visibleRows * DROPDOWN_ROW_HEIGHT, 0xC0101010);
    //    int selected = LangList.indexOf(openDropdown == DROPDOWN_SOURCE ? selectedSourceLang : selectedTargetLang);
    //    for (int row = 0; row < visibleRows; row++) {
    //        int idx = filtered.get(scrollOffset + row);
    //        int rowY = listTop + row * DROPDOWN_ROW_HEIGHT;
    //        boolean hovered = mouseX >= dropdownX && mouseX < dropdownX + dropdownW && mouseY >= rowY && mouseY < rowY + DROPDOWN_ROW_HEIGHT;
    //        if (idx == selected) {
    //            ScreenCompat.fill(graphics, dropdownX, rowY, dropdownX + dropdownW, rowY + DROPDOWN_ROW_HEIGHT, 0xFF2E4A2E);
    //        } else if (hovered) {
    //            ScreenCompat.fill(graphics, dropdownX, rowY, dropdownX + dropdownW, rowY + DROPDOWN_ROW_HEIGHT, 0xFF2A2A2A);
    //        }
    //        ScreenCompat.drawTextWithShadow(graphics, this.font, TextCompat.literal(LangList.label(idx)), dropdownX + 4, rowY + 2, 0xFFFFFFFF);
    //    }
    //    if (filtered.size() > DROPDOWN_VISIBLE_ROWS) {
    //        int trackH = visibleRows * DROPDOWN_ROW_HEIGHT;
    //        int barY = listTop + (int) ((double) scrollOffset / Math.max(1, filtered.size() - DROPDOWN_VISIBLE_ROWS) * (trackH - 20));
    //        ScreenCompat.fill(graphics, dropdownX + dropdownW - 2, listTop, dropdownX + dropdownW - 1, listTop + trackH, 0xFF333333);
    //        ScreenCompat.fill(graphics, dropdownX + dropdownW - 2, barY, dropdownX + dropdownW - 1, barY + 20, 0xFF888888);
    //    }
    //}
    //?} else if >=1.20 {
    private void drawDropdown(DrawContext context, int mouseX, int mouseY, float delta) {
        if (openDropdown < 0) {
            return;
        }
        // hand-rolled search row: vanilla EditBox focus routing is unreliable on old versions
        ScreenCompat.fill(context, dropdownX - 1, dropdownY - 1, dropdownX + dropdownW + 1, dropdownY + DROPDOWN_SEARCH_HEIGHT + 1, searchFieldActive ? 0xFFFFFFFF : 0xFF555555);
        ScreenCompat.fill(context, dropdownX, dropdownY, dropdownX + dropdownW, dropdownY + DROPDOWN_SEARCH_HEIGHT, 0xFF000000);
        if (searchQuery.isEmpty()) {
            ScreenCompat.drawTextWithShadow(context, this.textRenderer, TextCompat.literal(I18nCompat.translate("linguachat.config.search")), dropdownX + 4, dropdownY + 4, 0x80808080);
        } else {
            ScreenCompat.drawTextWithShadow(context, this.textRenderer, TextCompat.literal(searchQuery + "_"), dropdownX + 4, dropdownY + 4, 0xFFFFFFFF);
        }
        List<Integer> filtered = currentFilter();
        int listTop = dropdownY + DROPDOWN_SEARCH_HEIGHT;
        int visibleRows = Math.min(DROPDOWN_VISIBLE_ROWS, filtered.size());
        ScreenCompat.fill(context, dropdownX, listTop, dropdownX + dropdownW, listTop + visibleRows * DROPDOWN_ROW_HEIGHT, 0xC0101010);
        int selected = LangList.indexOf(openDropdown == DROPDOWN_SOURCE ? selectedSourceLang : selectedTargetLang);
        for (int row = 0; row < visibleRows; row++) {
            int idx = filtered.get(scrollOffset + row);
            int rowY = listTop + row * DROPDOWN_ROW_HEIGHT;
            boolean hovered = mouseX >= dropdownX && mouseX < dropdownX + dropdownW && mouseY >= rowY && mouseY < rowY + DROPDOWN_ROW_HEIGHT;
            if (idx == selected) {
                ScreenCompat.fill(context, dropdownX, rowY, dropdownX + dropdownW, rowY + DROPDOWN_ROW_HEIGHT, 0xFF2E4A2E);
            } else if (hovered) {
                ScreenCompat.fill(context, dropdownX, rowY, dropdownX + dropdownW, rowY + DROPDOWN_ROW_HEIGHT, 0xFF2A2A2A);
            }
            ScreenCompat.drawTextWithShadow(context, this.textRenderer, TextCompat.literal(LangList.label(idx)), dropdownX + 4, rowY + 2, 0xFFFFFFFF);
        }
        if (filtered.size() > DROPDOWN_VISIBLE_ROWS) {
            int trackH = visibleRows * DROPDOWN_ROW_HEIGHT;
            int barY = listTop + (int) ((double) scrollOffset / Math.max(1, filtered.size() - DROPDOWN_VISIBLE_ROWS) * (trackH - 20));
            ScreenCompat.fill(context, dropdownX + dropdownW - 2, listTop, dropdownX + dropdownW - 1, listTop + trackH, 0xFF333333);
            ScreenCompat.fill(context, dropdownX + dropdownW - 2, barY, dropdownX + dropdownW - 1, barY + 20, 0xFF888888);
        }
    }
    //?} else {
    /*private void drawDropdown(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (openDropdown < 0) {
            return;
        }
        ScreenCompat.fill(matrices, dropdownX - 1, dropdownY - 1, dropdownX + dropdownW + 1, dropdownY + DROPDOWN_SEARCH_HEIGHT + 1, searchFieldActive ? 0xFFFFFFFF : 0xFF555555);
        ScreenCompat.fill(matrices, dropdownX, dropdownY, dropdownX + dropdownW, dropdownY + DROPDOWN_SEARCH_HEIGHT, 0xFF000000);
        if (searchQuery.isEmpty()) {
            ScreenCompat.drawTextWithShadow(matrices, this.textRenderer, I18nCompat.translate("linguachat.config.search"), dropdownX + 4, dropdownY + 4, 0x80808080);
        } else {
            ScreenCompat.drawTextWithShadow(matrices, this.textRenderer, searchQuery + "_", dropdownX + 4, dropdownY + 4, 0xFFFFFFFF);
        }
        List<Integer> filtered = currentFilter();
        int listTop = dropdownY + DROPDOWN_SEARCH_HEIGHT;
        int visibleRows = Math.min(DROPDOWN_VISIBLE_ROWS, filtered.size());
        ScreenCompat.fill(matrices, dropdownX, listTop, dropdownX + dropdownW, listTop + visibleRows * DROPDOWN_ROW_HEIGHT, 0xC0101010);
        int selected = LangList.indexOf(openDropdown == DROPDOWN_SOURCE ? selectedSourceLang : selectedTargetLang);
        for (int row = 0; row < visibleRows; row++) {
            int idx = filtered.get(scrollOffset + row);
            int rowY = listTop + row * DROPDOWN_ROW_HEIGHT;
            boolean hovered = mouseX >= dropdownX && mouseX < dropdownX + dropdownW && mouseY >= rowY && mouseY < rowY + DROPDOWN_ROW_HEIGHT;
            if (idx == selected) {
                ScreenCompat.fill(matrices, dropdownX, rowY, dropdownX + dropdownW, rowY + DROPDOWN_ROW_HEIGHT, 0xFF2E4A2E);
            } else if (hovered) {
                ScreenCompat.fill(matrices, dropdownX, rowY, dropdownX + dropdownW, rowY + DROPDOWN_ROW_HEIGHT, 0xFF2A2A2A);
            }
            ScreenCompat.drawTextWithShadow(matrices, this.textRenderer, LangList.label(idx), dropdownX + 4, rowY + 2, 0xFFFFFFFF);
        }
        if (filtered.size() > DROPDOWN_VISIBLE_ROWS) {
            int trackH = visibleRows * DROPDOWN_ROW_HEIGHT;
            int barY = listTop + (int) ((double) scrollOffset / Math.max(1, filtered.size() - DROPDOWN_VISIBLE_ROWS) * (trackH - 20));
            ScreenCompat.fill(matrices, dropdownX + dropdownW - 2, listTop, dropdownX + dropdownW - 1, listTop + trackH, 0xFF333333);
            ScreenCompat.fill(matrices, dropdownX + dropdownW - 2, barY, dropdownX + dropdownW - 1, barY + 20, 0xFF888888);
        }
    }
    *///?}

    //? if <1.21.1 {
    /*public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (scrollDropdown((int) amount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
    *///?} else {
    public boolean mouseScrolled(double mouseX, double mouseY, double amountX, double amountY) {
        if (scrollDropdown((int) amountY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amountX, amountY);
    }
    //?}

    //? if <1.21.11 {
    /*public boolean charTyped(char chr, int modifiers) {
        if (openDropdown >= 0 && chr >= 32 && chr != 127) {
            // hand-rolled input: vanilla EditBox focus routing is unreliable on <=1.19.2
            searchQuery += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }
    *///?}
    //? if <1.21.11 {
    /*public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (handleDropdownClick(mouseX, mouseY)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    *///?} else if >=26.1 {
    /*public boolean mouseClicked(MouseButtonEvent event, boolean started) {
        if (handleDropdownClick(event.x(), event.y())) {
            return true;
        }
        return super.mouseClicked(event, started);
    }
    *///?} else {
    public boolean mouseClicked(Click event, boolean started) {
        if (handleDropdownClick(event.x(), event.y())) {
            return true;
        }
        return super.mouseClicked(event, started);
    }
    //?}

    //? if <1.21.11 {
    /*public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (openDropdown >= 0 && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            closeDropdown();
            return true;
        }
        if (openDropdown >= 0) {
            // shortcut: no clipboard paste on <=1.19.2, typed input only
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    *///?} else if >=26.1 {
    /*public boolean keyPressed(KeyEvent event) {
        if (openDropdown >= 0 && event.key() == InputConstants.KEY_ESCAPE) {
            closeDropdown();
            return true;
        }
        return super.keyPressed(event);
    }
    *///?} else {
    public boolean keyPressed(KeyInput event) {
        if (openDropdown >= 0 && event.key() == GLFW.GLFW_KEY_ESCAPE) {
            closeDropdown();
            return true;
        }
        return super.keyPressed(event);
    }
    //?}

    private void saveConfig() {
        config.setDefaultSourceLang(selectedSourceLang);
        config.setDefaultTargetLang(selectedTargetLang);
        
        if ("deepl".equals(currentProvider) && apiKeyField != null) {
            //? if >=26.1 {
//            config.setDeeplApiKey(apiKeyField.getValue().trim());
            //?} else {
            config.setDeeplApiKey(apiKeyField.getText().trim());
            //?}
        } else if ("kagi".equals(currentProvider)) {
            if (apiKeyField != null) {
                //? if >=26.1 {
//                config.setKagiApiKey(apiKeyField.getValue().trim());
                //?} else {
                config.setKagiApiKey(apiKeyField.getText().trim());
                //?}
            }
            if (sessionTokenField != null) {
                //? if >=26.1 {
//                config.setKagiSessionToken(sessionTokenField.getValue().trim());
                //?} else {
                config.setKagiSessionToken(sessionTokenField.getText().trim());
                //?}
            }
        }
        
        try {
            //? if >=26.1 {
//            int cacheSize = Integer.parseInt(cacheSizeField.getValue());
            //?} else {
            int cacheSize = Integer.parseInt(cacheSizeField.getText());
            //?}
            config.setCacheSize(cacheSize);
        } catch (NumberFormatException e) {
            // Invalid input, ignore
        }

        // one disk write per Save click instead of one per setter
        ModConfig.save();
    }

    @Override
    //? if >=26.1 {
    //public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    //    super.extractRenderState(graphics, mouseX, mouseY, delta);
    //
    //    int centerX = this.width / 2;
    //    int centerY = this.height / 2;
    //
    //    // Calculate positioning for title
    //    int totalRows = 4 + 1 + 2 + 1 + 1 + 1 + 1;
    //    if ("kagi".equals(currentProvider)) {
    //        totalRows += 1;
    //    }
    //    int totalHeight = totalRows * ROW_HEIGHT + (totalRows - 1) * ROW_SPACING + 3 * SECTION_GAP;
    //    int startY = centerY - totalHeight / 2;
    //
    //    ScreenCompat.drawCenteredText(graphics, this.font, this.title, centerX, startY - TITLE_OFFSET, 0xFFFFFFFF);
    //
    //    // Draw labels after super: widgets are already in the render state, labels go on top
    //    if (apiKeyField != null) {
    //        String labelKey = "deepl".equals(currentProvider) ? "linguachat.config.api_key.deepl" : "linguachat.config.api_key.kagi";
    //        ScreenCompat.drawTextWithShadow(graphics, this.font, I18nCompat.translatableText(labelKey),
    //            apiKeyField.getX() - LABEL_COLUMN_WIDTH, apiKeyField.getY() + 6, 0xFFFFFFFF);
    //    }
    //
    //    if (sessionTokenField != null) {
    //        ScreenCompat.drawTextWithShadow(graphics, this.font, I18nCompat.translatableText("linguachat.config.session_token"),
    //            sessionTokenField.getX() - LABEL_COLUMN_WIDTH, sessionTokenField.getY() + 6, 0xFFFFFFFF);
    //    }
    //
    //    if (cacheSizeField != null) {
    //        ScreenCompat.drawTextWithShadow(graphics, this.font, I18nCompat.translatableText("linguachat.config.cache_size"),
    //            cacheSizeField.getX() - LABEL_COLUMN_WIDTH, cacheSizeField.getY() + 6, 0xFFFFFFFF);
    //    }
    //
    //    // Google notice (gray text when no API field needed)
    //    if ("google".equals(currentProvider)) {
    //        int containerLeft = centerX - CONTAINER_WIDTH / 2;
    //        int labelX = containerLeft + 10;
    //        int noticeY = startY + (ROW_HEIGHT + ROW_SPACING) * 4 + SECTION_GAP + (ROW_HEIGHT + ROW_SPACING) * 2 + SECTION_GAP;
    //        ScreenCompat.drawTextWithShadow(graphics, this.font, I18nCompat.translatableText("linguachat.config.google_notice"),
    //            labelX, noticeY + 6, 0xFF888888);
    //    }
    //
    //    drawDropdown(graphics, mouseX, mouseY, delta);
    //}
    //?} else if >=1.20 || =1.21.1 || =1.21.4 || =1.21.5 || =1.21.11 {
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
        // Draw labels after super.render() to avoid z-fighting
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
        if (apiKeyField != null) {
            apiKeyField.render(context, mouseX, mouseY, delta);
        }
        if (sessionTokenField != null) {
            sessionTokenField.render(context, mouseX, mouseY, delta);
        }
        if (cacheSizeField != null) {
            cacheSizeField.render(context, mouseX, mouseY, delta);
        }

        drawDropdown(context, mouseX, mouseY, delta);
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
        
        // Language rows are dropdown buttons now (label lives on the button); keep the Y cascade
        currentY += ROW_HEIGHT + ROW_SPACING;
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
        if (apiKeyField != null) {
            apiKeyField.render(matrices, mouseX, mouseY, delta);
        }
        if (sessionTokenField != null) {
            sessionTokenField.render(matrices, mouseX, mouseY, delta);
        }
        if (cacheSizeField != null) {
            cacheSizeField.render(matrices, mouseX, mouseY, delta);
        }

        drawDropdown(matrices, mouseX, mouseY, delta);
    }
    *///?}

    @Override
    //? if >=26.1 {
    //public void onClose() {
    //    if (this.minecraft != null) {
    //        ScreenCompat.setScreen(this.minecraft, parent);
    //    }
    //}
    //?} else if >=1.18 {
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
