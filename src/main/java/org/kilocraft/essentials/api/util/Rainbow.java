package org.kilocraft.essentials.api.util;

import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TextColor;

import java.awt.*;

public class Rainbow {

    public static String[] get(int length, float start){
        String[] colors = new String[length];
        Color cc;
        for(int i = 0; i < length; i++){
            float hue = (float) 1 + ((float) i / length) + start;
            cc = Color.getHSBColor( hue , 1.0f, 1.0f);
            String hex = String.format( "#%02x%02x%02x", cc.getRed(), cc.getGreen(), cc.getBlue());
            colors[i] = hex;
        }
        return colors;
    }


    public static MutableText formatRainbow(String text, float start, boolean ignoreSpace){
        String[] colors;
        if(ignoreSpace){
            String colored = text.replaceAll(" ", "");
            colors = Rainbow.get(colored.length(), start);
        } else {
            colors = Rainbow.get(text.length(), start);
        }

        char[] c = text.toCharArray();
        MutableText rainbow = new LiteralText("");
        int j = 0;
        for(int i = 0; i < text.length(); i++){
            if(c[i] == ' ' && ignoreSpace){
                rainbow.append(new LiteralText(String.valueOf(' ')));
            } else {
                int finalJ = j;
                rainbow.append(new LiteralText(String.valueOf(c[i])).styled(style -> {
                    return style.withColor(TextColor.parse(colors[finalJ]));
                }));
                j++;
            }
        }
        return rainbow;
    }


}
