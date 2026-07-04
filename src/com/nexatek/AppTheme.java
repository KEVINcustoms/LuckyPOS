package com.nexatek;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import java.awt.Font;
import javax.swing.UIManager;

final class AppTheme {

    static final String ACCENT = "#f97316";

    private AppTheme() {
    }

    static void install() {
        FlatRobotoFont.install();
        FlatLaf.registerCustomDefaultsSource("com.nexatek.themes");
        UIManager.put("defaultFont", new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.arc", 10);
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        FlatMacLightLaf.setup();
    }

    static String roundedFieldStyle() {
        return ""
                + "arc:999;"
                + "margin:6,10,6,10;"
                + "focusWidth:3;"
                + "focusedBorderColor:fade(" + ACCENT + ",70%);"
                + "focusColor:fade(" + ACCENT + ",25%);";
    }

    static String primaryButtonStyle() {
        return ""
                + "arc:999;"
                + "margin:8,12,8,12;"
                + "borderWidth:0;"
                + "focusWidth:1;"
                + "innerFocusWidth:0;"
                + "background:" + ACCENT + ";"
                + "foreground:#FFFFFF;";
    }

    static String iconButtonStyle() {
        return ""
                + "arc:999;"
                + "margin:1,1,1,1;"
                + "borderWidth:0;"
                + "focusWidth:0;"
                + "innerFocusWidth:0;"
                + "background:null;";
    }

    static void putTextFieldPlaceholder(javax.swing.JComponent component, String placeholder) {
        component.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
    }
}
