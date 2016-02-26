package org.motechproject.uitest.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * A class that represents data services page. Has methods which check functionality of
 * data browser, schema editor and data services settings.
 */

public class DataServicesPage extends AbstractBasePage {

    public static final By ENTITY_NAME_FIELD = By.name("inputEntityName");
    public static final By NEW_ENTITY_BUTTON = By.id("newEntityButton");
    public static final By SAVE_ENTITY_BUTTON = By.id("saveNewEntityButton");
    public static final By DATA_SERVICES_BUTTON = By.id("data-services");
    public static final By SCHEMA_EDITOR_BUTTON = By.id("schemaEditor");
    public static final By BROWSE_INSTANCES_BUTTON = By.id("browseInstancesButton");
    public static final By ADD_NEW_INSTANCE_BUTTON = By.id("addNewInstanceButton");
    public static final By ENTITY_SPAN = By.id("select2-chosen-2");

    public static final String HOME_PATH = "/module/server/home#";

    public DataServicesPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Method creates new entity in MDS schema editor.
     * @param entityName new entity name
     * @return method returns text that appears in schema editor entity input after creating new entity, should be the same as new entity name, should be checked in tests
     */
    public String createNewEntity(String entityName) throws InterruptedException {
        waitForElement(DATA_SERVICES_BUTTON);
        clickWhenVisible(DATA_SERVICES_BUTTON);
        waitForElement(SCHEMA_EDITOR_BUTTON);
        clickWhenVisible(SCHEMA_EDITOR_BUTTON);
        waitForElement(NEW_ENTITY_BUTTON);
        clickWhenVisible(NEW_ENTITY_BUTTON);
        waitForElement(ENTITY_NAME_FIELD);
        setTextToFieldNoEnter(ENTITY_NAME_FIELD, entityName);
        waitForElement(SAVE_ENTITY_BUTTON);
        clickOn(SAVE_ENTITY_BUTTON);
        waitForElement(BROWSE_INSTANCES_BUTTON);
        waitForElement(ENTITY_SPAN);
        return getText(ENTITY_SPAN);
    }

    /**
     * Method that goes to data services page and enters entity table.
     * @param entityName name of entity table that we want to enter
     */
    public void goToEntityTable(String entityName) throws InterruptedException {
        waitForElement(DATA_SERVICES_BUTTON);
        clickWhenVisible(DATA_SERVICES_BUTTON);
        waitForElement(By.id(String.format("entity_%s", entityName)));
        clickWhenVisible(By.id(String.format("entity_%s", entityName)));
        waitForElement(ADD_NEW_INSTANCE_BUTTON);
    }

    @Override
    public String expectedUrlPath() {
        return HOME_PATH;
    }

    @Override
    public void goToPage() {
        getDriver().get(getMotechUrl() + HOME_PATH);
    }
}
