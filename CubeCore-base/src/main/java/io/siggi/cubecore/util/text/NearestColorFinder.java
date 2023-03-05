/*
 * This file contains code copied from adventure, licensed under the MIT License.
 *
 * Copyright (c) 2017-2023 KyoriPowered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.siggi.cubecore.util.text;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.md_5.bungee.api.ChatColor;

public class NearestColorFinder {
    private NearestColorFinder() {
    }

    private static final Set<ChatColor> standardColors = new HashSet<>();
    private static final Map<ChatColor, RGB> rgbValues = new HashMap<>();
    private static final Map<ChatColor, HSV> hsvValues = new HashMap<>();

    static {
        rgbValues.put(ChatColor.BLACK, new RGB(0x000000));
        rgbValues.put(ChatColor.DARK_BLUE, new RGB(0x0000aa));
        rgbValues.put(ChatColor.DARK_GREEN, new RGB(0x00aa00));
        rgbValues.put(ChatColor.DARK_AQUA, new RGB(0x00aaaa));
        rgbValues.put(ChatColor.DARK_RED, new RGB(0xaa0000));
        rgbValues.put(ChatColor.DARK_PURPLE, new RGB(0xaa00aa));
        rgbValues.put(ChatColor.GOLD, new RGB(0xffaa00));
        rgbValues.put(ChatColor.GRAY, new RGB(0xaaaaaa));
        rgbValues.put(ChatColor.DARK_GRAY, new RGB(0x555555));
        rgbValues.put(ChatColor.BLUE, new RGB(0x5555ff));
        rgbValues.put(ChatColor.GREEN, new RGB(0x55ff55));
        rgbValues.put(ChatColor.AQUA, new RGB(0x55ffff));
        rgbValues.put(ChatColor.RED, new RGB(0xff5555));
        rgbValues.put(ChatColor.LIGHT_PURPLE, new RGB(0xff55ff));
        rgbValues.put(ChatColor.YELLOW, new RGB(0xffff55));
        rgbValues.put(ChatColor.WHITE, new RGB(0xffffff));
        for (Map.Entry<ChatColor, RGB> entry : rgbValues.entrySet()) {
            hsvValues.put(entry.getKey(), entry.getValue().toHSV());
            standardColors.add(entry.getKey());
        }
    }

    public static ChatColor findNearest(ChatColor any) {
        if (standardColors.contains(any)) {
            return any;
        }

        String name;
        RGB anyRgb;
        try {
            name = any.getName();
            if (!name.startsWith("#")) {
                return any;
            }
            anyRgb = new RGB(Integer.parseInt(name.substring(1), 16));
        } catch (Throwable t) {
            return any;
        }

        HSV anyHsv = anyRgb.toHSV();

        float matchedDistance = Float.MAX_VALUE;
        ChatColor match = ChatColor.WHITE;
        for (Map.Entry<ChatColor,HSV> entry : hsvValues.entrySet()) {
            ChatColor potential = entry.getKey();
            float distance = distance(anyHsv, entry.getValue());
            if (distance < matchedDistance) {
                match = potential;
                matchedDistance = distance;
            }
            if (distance == 0) {
                break; // same colour! whoo!
            }
        }
        return match;
    }

    private static float distance(HSV self, HSV other) {
        // weight hue more heavily than saturation and brightness. kind of magic numbers, but is fine for our use case of downsampling to a set of colors
        float hueDistance = 3 * Math.min(Math.abs(self.h - other.h), 1f - Math.abs(self.h - other.h));
        float saturationDiff = self.s - other.s;
        float valueDiff = self.v - other.v;
        return hueDistance * hueDistance + saturationDiff * saturationDiff + valueDiff * valueDiff;
    }

    private static class RGB {
        public final int red;
        public final int green;
        public final int blue;

        public RGB(int rgb) {
            this((rgb >> 16) & 0xff, (rgb >> 8) & 0xff, rgb & 0xff);
        }

        public RGB(int red, int green, int blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        public HSV toHSV() {
            final float r = red / 255.0f;
            final float g = green / 255.0f;
            final float b = blue / 255.0f;

            final float min = Math.min(r, Math.min(g, b));
            final float max = Math.max(r, Math.max(g, b)); // v
            final float delta = max - min;

            final float s;
            if (max != 0) {
                s = delta / max; // s
            } else {
                // r = g = b = 0
                s = 0;
            }
            if (s == 0) { // s = 0, h is undefined
                return new HSV(0, s, max);
            }

            float h;
            if (r == max) {
                h = (g - b) / delta; // between yellow & magenta
            } else if (g == max) {
                h = 2 + (b - r) / delta; // between cyan & yellow
            } else {
                h = 4 + (r - g) / delta; // between magenta & cyan
            }
            h *= 60; // degrees
            if (h < 0) {
                h += 360;
            }

            return new HSV(h / 360.0f, s, max);
        }
    }

    private static class HSV {
        public final float h;
        public final float s;
        public final float v;
        public HSV(float h, float s, float v) {
            this.h = h;
            this.s = s;
            this.v = v;
        }
    }
}
