package net.jingle.bells.views;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.router.RouteAlias;

import jakarta.annotation.security.PermitAll;
import net.jingle.bells.data.entity.ProductCategory;
import net.jingle.bells.data.service.ProductCategoryService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainLayout.class)
@RouteAlias(value = "categories", layout = MainLayout.class)
@PageTitle("Hallitse kategorioita")
@PermitAll

public class CategoryAdminView extends VerticalLayout {

    private final ProductCategoryService categoryService;
    private Grid<ProductCategory> grid = new Grid<>(ProductCategory.class, false);
    private TextField nameField = new TextField("Kategorian nimi");
    private TextField descriptionField = new TextField("Kuvaus");
    private Button saveButton = new Button("Tallenna");
    private Button clearButton = new Button("Tyhjennä");
    private Button deleteButton = new Button("Poista");

    private Binder<ProductCategory> binder = new Binder<>(ProductCategory.class);

    private ProductCategory currentCategory;

    public CategoryAdminView(ProductCategoryService categoryService) {
        this.categoryService = categoryService;

        setSizeFull();
        configureGrid();
        configureForm();

        HorizontalLayout formLayout = new HorizontalLayout(nameField, descriptionField, saveButton, deleteButton, clearButton);
        formLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

        deleteButton.setEnabled(false);

        add(formLayout, grid);

        refreshGrid();
        clearForm();
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addColumn(ProductCategory::getName).setHeader("Nimi").setSortable(true);
        grid.addColumn(ProductCategory::getDescription).setHeader("Kuvaus");
        grid.asSingleSelect().addValueChangeListener(event -> editCategory(event.getValue()));
    }

    private void configureForm() {
        binder.bind(nameField, ProductCategory::getName, ProductCategory::setName);
        binder.bind(descriptionField, ProductCategory::getDescription, ProductCategory::setDescription);

        binder.forField(nameField)
                .asRequired("Kategorian nimi on pakollinen")
                .bind(ProductCategory::getName, ProductCategory::setName);

        saveButton.addClickListener(event -> saveCategory());

        clearButton.addClickListener(event -> clearForm());

        deleteButton.addClickListener(event -> deleteCategory());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
    }

    private void refreshGrid() {
        grid.setItems(categoryService.findAllCategories());
    }

    private void saveCategory() {
        try {
            if (currentCategory == null) {
                currentCategory = new ProductCategory();
            }
            binder.writeBean(currentCategory);
            categoryService.saveCategory(currentCategory);
            refreshGrid();
            clearForm();
            Notification.show("Kategoria tallennettu.", 3000, Notification.Position.MIDDLE);
        } catch (ValidationException e) {
            Notification.show("Tallennus epäonnistui: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
        } catch (Exception e) {
            Notification.show("Tallennus epäonnistui: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            e.printStackTrace();
        }
    }

    private void editCategory(ProductCategory category) {
        if (category == null) {
            clearForm();
        } else {
            currentCategory = category;
            binder.readBean(currentCategory);
            deleteButton.setEnabled(true);
        }
    }

    private void clearForm() {
        currentCategory = null;
        binder.readBean(new ProductCategory());
        nameField.clear();
        descriptionField.clear();
        grid.asSingleSelect().clear();
        deleteButton.setEnabled(false);
        nameField.focus();
    }
    private void deleteCategory() {
        if (currentCategory != null) {
            try {
                categoryService.deleteCategory(currentCategory.getId());
                refreshGrid();
                clearForm();
                Notification.show("Kategoria poistettu.", 3000, Notification.Position.MIDDLE);
            } catch (Exception e) {
                Notification.show("Poisto epäonnistui: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
                e.printStackTrace();
            }
        }
    }
}