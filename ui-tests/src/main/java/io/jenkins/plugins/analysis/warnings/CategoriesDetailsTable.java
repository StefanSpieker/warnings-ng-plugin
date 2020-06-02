package io.jenkins.plugins.analysis.warnings;

import org.openqa.selenium.WebElement;


/**
 * Class representing an categories table on the {@link AnalysisResult}.
 *
 * @author Kevin Richter
 */
public class CategoriesDetailsTable extends AbstractDetailsTable {
    /**
     * Creates an CategoriesDetailsTable of a specific type.
     *
     * @param tab
     *          the WebElement representing the tab which belongs to the categories table
     * @param resultDetailsPage
     *          the AnalysisResult on which the categories table is displayed on
     */
    public CategoriesDetailsTable(final WebElement tab, final AnalysisResult resultDetailsPage) {
        super(tab, "category", resultDetailsPage);
        this.updateTableRows();
    }

    /**
     * Enum representing the headers which should be present in a {@link CategoriesDetailsTable}.
     */
    public enum Header {
        CATEGORY("Category"),
        TOTAL("Total"),
        DISTRIBUTION("Distribution");

        private final String title;

        Header(final String property) {
            title = property;
        }
    }
}
