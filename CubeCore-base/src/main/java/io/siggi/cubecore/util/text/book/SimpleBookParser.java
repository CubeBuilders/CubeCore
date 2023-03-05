package io.siggi.cubecore.util.text.book;

import io.siggi.cubecore.CubeCore;
import io.siggi.cubecore.io.LineReader;
import io.siggi.cubecore.util.CubeCoreUtil;
import io.siggi.cubecore.util.text.FormattedText;
import io.siggi.cubecore.util.text.processor.TextProcessor;
import io.siggi.nbt.NBTCompound;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.md_5.bungee.api.chat.TextComponent;

public class SimpleBookParser implements BookParser {
    private final TextProcessor textProcessor;

    public SimpleBookParser(TextProcessor textProcessor) {
        if (textProcessor == null) throw new NullPointerException();
        this.textProcessor = textProcessor;
    }

    @Override
    public NBTCompound parseBook(LineReader reader, boolean useFallbackColor) throws IOException {
        String title = "CubeCore Book";
        String author = "CubeCore";
        List<Page> pages = new ArrayList<>();
        Map<String,Integer> pageNumbersByName = new HashMap<>();
        Page page = null;
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("\\\\") || line.startsWith("\\#")) {
                line = line.substring(1);
            } else if (line.startsWith("#title ")) {
                title = line.substring(7);
                continue;
            } else if (line.startsWith("#author ")) {
                author = line.substring(8);
                continue;
            } else if (line.equals("#page")) {
                int pageNumber = pages.size() + 1;
                page = new Page(pageNumber, null);
                pages.add(page);
                continue;
            } else if (line.startsWith("#page ")) {
                int pageNumber = pages.size() + 1;
                String pageName = line.substring(6);
                page = new Page(pageNumber, pageName);
                pages.add(page);
                pageNumbersByName.put(line.substring(6), page.number);
                continue;
            }
            if (page != null) {
                page.builder.append(line).append("\n");
            }
        }
        List<TextComponent> pageComponents = new ArrayList<>(pages.size());
        for (Page bookPage : pages) {
            String rawText = bookPage.builder.toString();
            while (rawText.endsWith("\n")) rawText = rawText.substring(0, rawText.length() - 1);
            for (Map.Entry<String,Integer> entry : pageNumbersByName.entrySet()) {
                String pageName = entry.getKey();
                int pageNumber = entry.getValue();
                rawText = rawText.replace("${" + pageName + "}", Integer.toString(pageNumber));
            }
            FormattedText formattedText = textProcessor.process(rawText, null, null);
            TextComponent pageComponent = CubeCoreUtil.glueComponents(formattedText.toTextComponents(false));
            pageComponents.add(pageComponent);
        }
        return CubeCore.createBook(title, author, pageComponents);
    }

    private static class Page {
        final StringBuilder builder = new StringBuilder();
        final int number;
        final String name;
        Page(int number, String name) {
            this.number = number;
            this.name = name;
        }
    }
}
