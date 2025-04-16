package net.jingle.bells.views;

import jakarta.annotation.security.PermitAll;
import net.jingle.bells.data.entity.Product;
import net.jingle.bells.data.entity.ProductCategory;
import net.jingle.bells.data.entity.ProductDetail;
import net.jingle.bells.data.service.ProductCategoryService;
import net.jingle.bells.data.service.ProductService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.util.List;

@PageTitle("Hallitse tuotteita")
@Route(value = "products", layout = MainLayout.class)
@PermitAll
public class ProductAdminView extends VerticalLayout {

    private final ProductService productService;
    private final ProductCategoryService productCategoryService;

    Grid<Product> grid = new Grid<>(Product.class, false);

    TextField name = new TextField("Tuotteen nimi");
    NumberField price = new NumberField("Hinta");
    IntegerField stockQuantity = new IntegerField("Varastosaldo");
    Checkbox available = new Checkbox("Saatavilla");
    ComboBox<ProductCategory> productCategory = new ComboBox<>("Kategoria");

    TextArea warrantyInfo = new TextArea("Takuutiedot");
    TextArea manufacturerInfo = new TextArea("Valmistajan tiedot");

    Button saveButton = new Button("Tallenna");
    Button deleteButton = new Button("Poista");
    Button clearButton = new Button("Tyhjennä");
    TextField filterByName = new TextField();
    ComboBox<ProductCategory> filterByCategory = new ComboBox<>();

    Binder<Product> binder = new BeanValidationBinder<>(Product.class);

    Product currentProduct;

    public ProductAdminView(ProductService productService, ProductCategoryService productCategoryService) {
        this.productService = productService;
        this.productCategoryService = productCategoryService;

        setSizeFull();
        configureGrid();
        configureForm();

        HorizontalLayout filterLayout = createFilterLayout();

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, deleteButton, clearButton);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.setEnabled(false);

        FormLayout formLayout = new FormLayout(
                name, price, stockQuantity, productCategory, available,
                warrantyInfo, manufacturerInfo
        );
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.setColspan(warrantyInfo, 2);
        formLayout.setColspan(manufacturerInfo, 2);


        price.setPrefixComponent(new com.vaadin.flow.component.html.Span("€"));

        VerticalLayout formAndButtonsLayout = new VerticalLayout(formLayout, buttonLayout);
        formAndButtonsLayout.setWidth("500px");
        formAndButtonsLayout.getStyle().set("flex-shrink", "0");

        HorizontalLayout gridAndFormLayout = new HorizontalLayout(grid, formAndButtonsLayout);
        gridAndFormLayout.setSizeFull();

        add(filterLayout, gridAndFormLayout);

        refreshGrid();
        clearForm();
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addColumn(Product::getName).setHeader("Nimi").setSortable(true);
        grid.addColumn(p -> p.getProductCategory() != null ? p.getProductCategory().getName() : "-")
                .setHeader("Kategoria").setSortable(true);
        grid.addColumn(Product::getPrice).setHeader("Hinta").setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);
        grid.addColumn(Product::getStockQuantity).setHeader("Varastossa").setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);
        grid.addColumn(p -> p.isAvailable() ? "Kyllä" : "Ei").setHeader("Saatavilla");
        grid.asSingleSelect().addValueChangeListener(event -> editProduct(event.getValue()));
    }

    private void configureForm() {
        List<ProductCategory> categories = productCategoryService.findAllCategories();
        productCategory.setItems(categories);
        productCategory.setItemLabelGenerator(ProductCategory::getName);

        binder.bind(name, "name");
        binder.forField(price)
                .withConverter(
                        d -> d == null ? null : BigDecimal.valueOf(d),
                        bd -> bd == null ? null : bd.doubleValue(),
                        "Hinnan tulee olla kelvollinen numero"
                )
                .bind("price");
        binder.bind(stockQuantity, "stockQuantity");
        binder.bind(available, "available");
        binder.bind(productCategory, "productCategory");

        saveButton.addClickListener(event -> saveProduct());
        deleteButton.addClickListener(event -> deleteProduct());
        clearButton.addClickListener(event -> clearForm());
    }

    private void refreshGrid() {
        updateGrid();
    }
    private void updateGrid() {
        String nameFilter = filterByName.getValue();
        ProductCategory categoryFilter = filterByCategory.getValue();
        grid.setItems(productService.findProductsByFilter(nameFilter, categoryFilter));
    }

    private HorizontalLayout createFilterLayout() {
        filterByName.setPlaceholder("Suodata nimellä...");
        filterByName.setClearButtonVisible(true);
        filterByName.addValueChangeListener(e -> updateGrid());

        filterByCategory.setPlaceholder("Suodata kategorialla...");
        filterByCategory.setClearButtonVisible(true);
        List<ProductCategory> categories = productCategoryService.findAllCategories();
        filterByCategory.setItems(categories);
        filterByCategory.setItemLabelGenerator(ProductCategory::getName);
        filterByCategory.addValueChangeListener(e -> updateGrid());

        HorizontalLayout layout = new HorizontalLayout(filterByName, filterByCategory);
        layout.setVerticalComponentAlignment(Alignment.END);
        return layout;
    }

    private void saveProduct() {
        try {
            if (currentProduct == null) {
                currentProduct = new Product();
            }
            binder.writeBean(currentProduct);

            ProductDetail detail = currentProduct.getProductDetail();
            if (detail == null) {
                detail = new ProductDetail(currentProduct);
            }
            detail.setWarrantyInfo(warrantyInfo.getValue());
            detail.setManufacturerInfo(manufacturerInfo.getValue());

            currentProduct.setProductDetail(detail);

            productService.saveProduct(currentProduct);

            refreshGrid();
            clearForm();
            Notification.show("Tuote tallennettu.", 3000, Notification.Position.MIDDLE);

        } catch (ValidationException e) {
            Notification.show("Tallennus epäonnistui: Tarkista lomakkeen tiedot. " + e.getMessage(), 4000, Notification.Position.MIDDLE);
        } catch (Exception e) {
            Notification.show("Tallennus epäonnistui: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            e.printStackTrace();
        }
    }

    private void deleteProduct() {
        if (currentProduct != null) {
            try {
                productService.deleteProduct(currentProduct.getId());
                refreshGrid();
                clearForm();
                Notification.show("Tuote poistettu.", 3000, Notification.Position.MIDDLE);
            } catch (Exception e) {
                Notification.show("Poisto epäonnistui: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
                e.printStackTrace();
            }
        }
    }

    private void editProduct(Product product) {
        if (product == null) {
            clearForm();
        } else {
            currentProduct = product;

            binder.readBean(currentProduct);

            ProductDetail detail = currentProduct.getProductDetail();
            if (detail != null) {
                warrantyInfo.setValue(detail.getWarrantyInfo() != null ? detail.getWarrantyInfo() : "");
                manufacturerInfo.setValue(detail.getManufacturerInfo() != null ? detail.getManufacturerInfo() : "");
            } else {
                warrantyInfo.clear();
                manufacturerInfo.clear();
            }

            deleteButton.setEnabled(true);
        }
    }

    private void clearForm() {
        currentProduct = null;
        binder.readBean(new Product());
        warrantyInfo.clear();
        manufacturerInfo.clear();
        available.setValue(true);
        grid.asSingleSelect().clear();
        deleteButton.setEnabled(false);
        name.focus();
    }
}