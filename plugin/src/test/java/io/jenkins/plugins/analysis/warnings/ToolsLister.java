package io.jenkins.plugins.analysis.warnings;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.DomContentJoiner;
import j2html.tags.EmptyTag;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser.AnalysisModelParserDescriptor;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.Tool;
import io.jenkins.plugins.analysis.core.model.Tool.ToolDescriptor;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;

import static j2html.TagCreator.*;

/**
 * Utility to create a report with all available tools.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("PMD.ClassNamingConventions")
class ToolsLister extends IntegrationTestWithJenkinsPerSuite {
    private static final String BULB_EMOJI = ":bulb:";
    private static final String EMPTY = "-";

    /**
     * Creates the TOOLS.md file, that lists all registered tools.
     *
     * @throws IOException
     *         if hte file TOOLS.md cannot be written
     */
    @Test
    void shouldPrintAllRegisteredTools() throws IOException {
        ArrayList<ToolDescriptor> descriptors = new ArrayList<>(
                getJenkins().getInstance().getDescriptorList(Tool.class));
        descriptors.sort(Comparator.comparing(d -> d.getLabelProvider().getName().toLowerCase(Locale.ENGLISH)));

        try (PrintWriter file = new PrintWriter("../SUPPORTED-FORMATS.md", StandardCharsets.UTF_8)) {
            file.printf("<!--- DO NOT EDIT - Generated by %s at %s-->%n", getClass().getSimpleName(),
                    LocalDateTime.now());
            file.println("# Supported Report Formats\n"
                    + "\n"
                    + "Jenkins' Warnings Next Generation Plugin supports the following report formats. \n"
                    + "If your tool is supported, but has no custom icon yet, please file a pull request for the\n"
                    + "[Warnings Next Generation Plugin](https://github.com/jenkinsci/warnings-ng-plugin/pulls).\n"
                    + "\n"
                    + "If your tool is not yet supported you can\n"
                    + "1. define a new Groovy based parser in the user interface\n"
                    + "2. export the issues of your tool to the native XML format (or any other format)\n"
                    + "3. provide a parser within a new small plugin. \n"
                    + "\n"
                    + "If the parser is useful for \n"
                    + "other teams as well please share it and provide pull requests for the \n"
                    + "[Warnings Next Generation Plug-in](https://github.com/jenkinsci/warnings-ng-plugin/pulls) and \n"
                    + "the [Analysis Parsers Library](https://github.com/jenkinsci/analysis-model/). \n");

            List<ContainerTag> lines = descriptors.stream()
                    .map(ToolsLister::getTableRows)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            file.println(table().with(thead().with(tr().with(
                            th("ID"),
                            th("Pipeline Symbol"),
                            th("Icon"),
                            th("Name"),
                            th("Default Pattern"))),
                    tbody().with(lines)).renderFormatted());
        }
    }

    private static List<ContainerTag> getTableRows(final ToolDescriptor descriptor) {
        List<ContainerTag> rows = new ArrayList<>();
        rows.add(tr().with(
                td(descriptor.getId()),
                td(descriptor.getSymbolName() + "()"),
                td(getIcon(descriptor)),
                td(getName(descriptor)),
                td(getPattern(descriptor))));
        if (StringUtils.isNotBlank(descriptor.getHelp())) {
            rows.add(tr().with(td()
                    .attr("colspan", "5")
                    .with(DomContentJoiner.join(" ", true, BULB_EMOJI, rawHtml(descriptor.getHelp())))));
        }
        return rows;
    }

    private static String getPattern(final ToolDescriptor descriptor) {
        if (descriptor instanceof AnalysisModelParserDescriptor) {
            return StringUtils.defaultIfEmpty(((AnalysisModelParserDescriptor) descriptor).getPattern(), EMPTY);
        }
        return EMPTY;
    }

    private static DomContent getName(final ToolDescriptor descriptor) {
        String name = descriptor.getName();
        String url = descriptor.getUrl();
        if (url.isEmpty()) {
            return text(name);
        }
        return a(name).withHref(url);
    }

    private static DomContent getIcon(final ToolDescriptor descriptor) {
        StaticAnalysisLabelProvider labelProvider = descriptor.getLabelProvider();
        String url = labelProvider.getLargeIconUrl();
        if (url.isEmpty() || url.startsWith("symbol")) {
            if (url.endsWith("plugin-font-awesome-api")) {
                String fontAwesomeIcon = StringUtils.substringBefore(StringUtils.substringAfter(url, "symbol-"), " ");
                return asImg(descriptor, "https://raw.githubusercontent.com/FortAwesome/Font-Awesome/6.x/svgs/" + fontAwesomeIcon + ".svg");
            }
            if (url.endsWith("plugin-warnings-ng")) {
                String warningsIcon = StringUtils.substringBefore(StringUtils.substringAfter(url, "symbol-"), " ");
                return asImg(descriptor, "https://raw.githubusercontent.com/jenkinsci/warnings-ng-plugin/main/plugin/src/main/resources/images/symbols/" + warningsIcon + ".svg");
            }
            return text(EMPTY);
        }
        return asImg(descriptor, url.replace("/plugin/warnings-ng/", "plugin/src/main/webapp/"));
    }

    private static EmptyTag asImg(final ToolDescriptor descriptor, final String iconUrl) {
        return img().withSrc(iconUrl)
                .withAlt(descriptor.getName())
                .attr("height", "48")
                .attr("width", 48);
    }
}
